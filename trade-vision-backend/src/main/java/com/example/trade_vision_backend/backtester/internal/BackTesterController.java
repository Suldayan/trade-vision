package com.example.trade_vision_backend.backtester.internal;

import com.example.trade_vision_backend.common.BackTestRequest;
import com.example.trade_vision_backend.backtester.BackTesterService;
import com.example.trade_vision_backend.market.CsvImporterService;
import com.example.trade_vision_backend.market.MarketData;
import com.example.trade_vision_backend.strategies.StrategyService;
import com.example.trade_vision_backend.strategies.Strategy;
import jakarta.annotation.Nonnull;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@Slf4j
@Validated
@RequiredArgsConstructor
@RequestMapping("/backtest")
public class BackTesterController {
    private final CsvImporterService csvImporterService;
    private final StrategyService strategyService;
    private final BackTesterService backTesterService;

    private static final Integer MAX_BACKTEST_REQUESTS = 5;

    @PostMapping(value = "/execute", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DeferredResult<ResponseEntity<List<BackTestResult>>> executeBacktest(
            @RequestPart("file") @Valid @Nonnull MultipartFile file,
            @RequestPart("request") @Valid @Nonnull List<BackTestRequest> requests) {

        DeferredResult<ResponseEntity<List<BackTestResult>>> deferredResult = new DeferredResult<>();

        if (requests.size() > MAX_BACKTEST_REQUESTS) {
            deferredResult.setErrorResult(ResponseEntity.badRequest()
                    .body("Number of requests exceeds maximum for concurrent backtests"));
            return deferredResult;
        }

        try {
            // Read file once and cache the data (We do this because of the way InputStream behaves)
            byte[] fileBytes = file.getBytes();

            // Create CompletableFutures for each backtest
            List<CompletableFuture<BackTestResult>> backtestFutures = requests.stream()
                    .map(request -> CompletableFuture.supplyAsync(() -> {
                        try {
                            log.info("Starting backtest with strategy containing {} entry and {} exit conditions",
                                    request.getEntryConditions().size(), request.getExitConditions().size());

                            // Create new input stream from cached bytes for each request
                            InputStream inputStream = new ByteArrayInputStream(fileBytes);
                            MarketData marketData = csvImporterService.importCsvFromStream(inputStream);
                            Strategy strategy = strategyService.buildStrategyFromRequest(request);

                            BackTestResult result = backTesterService.runBackTest(strategy, marketData, request);

                            log.info("Backtest completed with {} trades and total return of {}%",
                                    result.tradeCount(), String.format("%.2f", result.totalReturn()));

                            return result;
                        } catch (Exception e) {
                            log.error("Error running individual backtest", e);
                            throw new RuntimeException(e);
                        }
                    }))
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
                    log.error("Error completing backtests", throwable);
                    deferredResult.setErrorResult(ResponseEntity.internalServerError().build());
                } else {
                    deferredResult.setResult(ResponseEntity.ok(results));
                }
            });

        } catch (IOException e) {
            log.error("Failed to process market data file", e);
            deferredResult.setErrorResult(ResponseEntity.badRequest().build());
        } catch (Exception e) {
            log.error("Error setting up backtest", e);
            deferredResult.setErrorResult(ResponseEntity.internalServerError().build());
        }

        return deferredResult;
    }
}