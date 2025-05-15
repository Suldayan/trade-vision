package com.example.trade_vision_backend.backtester.internal;

import com.example.trade_vision_backend.domain.BackTestRequest;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@Slf4j
@Validated
@RequiredArgsConstructor
@RequestMapping("/backtest")
public class BackTesterController {
    private final CsvImporterService csvImporterService;
    private final StrategyService strategyService;
    private final BackTesterService backTesterService;

    @PostMapping(value = "/execute", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BackTestResult> executeBacktest(
            @RequestPart("file") @Valid @Nonnull MultipartFile file,
            @RequestPart("request") @Valid @Nonnull BackTestRequest request) {

        try {
            log.info("Starting backtest with strategy containing {} entry and {} exit conditions",
                    request.getEntryConditions().size(), request.getExitConditions().size());

            MarketData marketData = csvImporterService.importCsvFromStream(file.getInputStream());
            Strategy strategy = strategyService.buildStrategyFromRequest(request);
            BackTestResult result = backTesterService.runBackTest(strategy, marketData, request);

            log.info("Backtest completed with {} trades and total return of {}%",
                    result.tradeCount(), String.format("%.2f", result.totalReturn()));
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            log.error("Failed to process market data file", e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error running backtest", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}