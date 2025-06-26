package com.example.trade_vision_backend.backtester.internal;

import com.example.trade_vision_backend.backtester.BackTesterOrchestrationService;
import com.example.trade_vision_backend.backtester.BackTesterService;
import com.example.trade_vision_backend.common.BackTestRequest;
import com.example.trade_vision_backend.market.CsvImporterService;
import com.example.trade_vision_backend.market.MarketData;
import com.example.trade_vision_backend.strategies.Strategy;
import com.example.trade_vision_backend.strategies.StrategyService;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@Slf4j
public class BackTesterOrchestrationServiceImpl implements BackTesterOrchestrationService {
    private final CsvImporterService csvImporterService;
    private final StrategyService strategyService;
    private final BackTesterService backTesterService;
    @Qualifier("backtestExecutor")
    private final Executor backtestExecutor;

    public BackTesterOrchestrationServiceImpl(
            CsvImporterService csvImporterService, StrategyService strategyService, BackTesterService backTesterService, Executor backtestExecutor) {
        this.csvImporterService = csvImporterService;
        this.strategyService = strategyService;
        this.backTesterService = backTesterService;
        this.backtestExecutor = backtestExecutor;
    }

    private static final Integer MAX_BACKTEST_REQUESTS = 5;

    @Nonnull
    @Override
    public DeferredResult<List<BackTestResult>> runOrchestration(
            @Nonnull MultipartFile file,
            @Nonnull List<BackTestRequest> requests) {
        validateRequests(requests);
        DeferredResult<List<BackTestResult>> deferredResult = new DeferredResult<>(Duration.ofMinutes(10).toMillis());

        if (requests.size() > MAX_BACKTEST_REQUESTS) {
            deferredResult.setErrorResult(new BackTesterExceptions.InvalidRequestException(
                    "Number of requests exceeds maximum for concurrent backtests: " + MAX_BACKTEST_REQUESTS));
            return deferredResult;
        }

        try {
            // Read file once and cache the data
            byte[] fileBytes = file.getBytes();

            List<CompletableFuture<BackTestResult>> backtestFutures = requests.stream()
                    .map(request -> CompletableFuture.supplyAsync(() ->
                            runSingleBacktest(fileBytes, request), backtestExecutor))
                    .toList();

            CompletableFuture<List<BackTestResult>> allResults = CompletableFuture.allOf(
                    backtestFutures.toArray(new CompletableFuture[0])
            ).thenApply(v ->
                    backtestFutures.stream()
                            .map(CompletableFuture::join)
                            .toList()
            );

            allResults.whenComplete((results, throwable) -> {
                if (throwable != null) {
                    Throwable rootCause = getRootCause(throwable);
                    log.error("Error completing backtests", throwable);
                    deferredResult.setErrorResult(rootCause);
                } else {
                    log.info("Successfully completed {} backtests", results.size());
                    deferredResult.setResult(results);
                }
            });

        } catch (IOException e) {
            log.error("Failed to process market data file", e);
            deferredResult.setErrorResult(new BackTesterExceptions.InvalidRequestException("Failed to process market data file", e));
        } catch (Exception e) {
            log.error("Error setting up backtest", e);
            deferredResult.setErrorResult(new BackTesterExceptions.BackTestOrchestrationException("Error setting up backtest", e));
        }

        return deferredResult;
    }

    @Nonnull
    private BackTestResult runSingleBacktest(byte[] fileBytes, BackTestRequest request) {
        try {
            log.info("Starting backtest on thread: {} with strategy containing {} entry and {} exit conditions",
                    Thread.currentThread().getName(),
                    request.getEntryConditions().size(),
                    request.getExitConditions().size());

            try (InputStream inputStream = new ByteArrayInputStream(fileBytes)) {
                MarketData marketData = csvImporterService.importCsvFromStream(inputStream);
                Strategy strategy = strategyService.buildStrategyFromRequest(request);
                BackTestResult result = backTesterService.runBackTest(strategy, marketData, request);

                log.info("Backtest completed with {} trades and total return of {}%",
                        result.tradeCount(), String.format("%.2f", result.totalReturn()));

                return result;
            }

        } catch (IOException e) {
            log.error("CSV parsing error in backtest", e);
            throw new BackTesterExceptions.InvalidRequestException("Invalid CSV format", e);
        } catch (IllegalArgumentException e) {
            log.error("Validation error in backtest", e);
            throw new BackTesterExceptions.InvalidRequestException("Invalid request parameters", e);
        } catch (Exception e) {
            log.error("Unexpected error running individual backtest", e);
            throw new BackTesterExceptions.BackTestOrchestrationException("Internal server error during backtest", e);
        }
    }

    private void validateRequests(List<BackTestRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new BackTesterExceptions.InvalidRequestException("Requests cannot be null or empty");
        }
    }

    private Throwable getRootCause(Throwable throwable) {
        Throwable rootCause = throwable;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }
        return rootCause;
    }
}