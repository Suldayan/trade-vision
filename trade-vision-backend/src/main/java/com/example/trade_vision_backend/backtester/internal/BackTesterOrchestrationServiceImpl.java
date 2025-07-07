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
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

@Service
@Slf4j
public class BackTesterOrchestrationServiceImpl implements BackTesterOrchestrationService {
    private final CsvImporterService csvImporterService;
    private final StrategyService strategyService;
    private final BackTesterService backTesterService;
    private final Executor backtestExecutor;

    private static final int MAX_BACKTEST_REQUESTS = 5;

    public BackTesterOrchestrationServiceImpl(
            CsvImporterService csvImporterService,
            StrategyService strategyService,
            BackTesterService backTesterService,
            @Qualifier("backtestExecutor") Executor backtestExecutor) {
        this.csvImporterService = csvImporterService;
        this.strategyService = strategyService;
        this.backTesterService = backTesterService;
        this.backtestExecutor = backtestExecutor;
    }

    @Nonnull
    @Override
    public CompletableFuture<List<BackTestResult>> runOrchestration(
            @Nonnull MultipartFile file,
            @Nonnull List<BackTestRequest> requests) {

        try {
            validateRequests(requests);
            if (requests.size() > MAX_BACKTEST_REQUESTS) {
                return CompletableFuture.failedFuture(
                        new BackTesterExceptions.InvalidRequestException(
                                "Number of requests exceeds maximum for concurrent backtests: " + MAX_BACKTEST_REQUESTS));
            }
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }

        return CompletableFuture
                .supplyAsync(() -> {
                    try {
                        return file.getBytes();
                    } catch (IOException e) {
                        throw new CompletionException(
                                new BackTesterExceptions.InvalidRequestException("Failed to process market data file", e));
                    }
                }, backtestExecutor)
                .thenCompose(fileBytes -> runBacktests(fileBytes, requests))
                .whenComplete((results, throwable) -> {
                    if (throwable != null) {
                        log.error("Error completing backtests for {} requests", requests.size(), throwable);
                    } else {
                        log.info("Successfully completed {} backtests", results.size());
                    }
                });
    }

    @Nonnull
    private CompletableFuture<List<BackTestResult>> runBacktests(
            byte[] fileBytes,
            @Nonnull List<BackTestRequest> requests) {
        List<CompletableFuture<BackTestResult>> backtestFutures = requests.stream()
                .map(request -> runSingleBacktest(fileBytes, request))
                .toList();

        return CompletableFuture.allOf(backtestFutures.toArray(CompletableFuture[]::new))
                .thenApply(ignored -> backtestFutures.stream()
                        .map(CompletableFuture::join)
                        .toList())
                .exceptionally(throwable -> {
                    backtestFutures.forEach(future -> future.cancel(true));
                    throw new CompletionException(extractMeaningfulException(throwable));
                });
    }

    @Nonnull
    private CompletableFuture<BackTestResult> runSingleBacktest(
            byte[] fileBytes,
            @Nonnull BackTestRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            if (fileBytes == null) {
                throw new CompletionException(
                        new BackTesterExceptions.InvalidRequestException("Failed to process market data file"));
            }

            log.debug("Starting backtest on thread: {} with strategy containing {} entry and {} exit conditions",
                    Thread.currentThread().getName(),
                    request.getEntryConditions().size(),
                    request.getExitConditions().size());

            try (InputStream inputStream = new ByteArrayInputStream(fileBytes)) {
                MarketData marketData = csvImporterService.importCsvFromStream(inputStream);
                Strategy strategy = strategyService.buildStrategyFromRequest(request);
                BackTestResult result = backTesterService.runBackTest(strategy, marketData, request);

                log.debug("Backtest completed with {} trades and total return of {}%",
                        result.tradeCount(), String.format("%.2f", result.totalReturn()));

                return result;

            } catch (IOException e) {
                log.error("CSV parsing error in backtest", e);
                throw new CompletionException(
                        new BackTesterExceptions.InvalidRequestException("Invalid CSV format", e));
            } catch (IllegalArgumentException e) {
                log.error("Validation error in backtest", e);
                throw new CompletionException(
                        new BackTesterExceptions.InvalidRequestException("Invalid request parameters", e));
            } catch (Exception e) {
                log.error("Unexpected error running individual backtest", e);
                throw new CompletionException(
                        new BackTesterExceptions.BackTestOrchestrationException("Internal server error during backtest", e));
            }
        }, backtestExecutor);
    }

    private void validateRequests(List<BackTestRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new BackTesterExceptions.InvalidRequestException("Requests cannot be null or empty");
        }
    }

    private Throwable extractMeaningfulException(Throwable throwable) {
        Throwable current = throwable;

        while (current instanceof CompletionException && current.getCause() != null) {
            current = current.getCause();
        }

        while (current != null) {
            if (current instanceof BackTesterExceptions.BackTestOrchestrationException ||
                    current instanceof BackTesterExceptions.InvalidRequestException) {
                return current;
            }
            current = current.getCause();
        }

        return throwable;
    }
}