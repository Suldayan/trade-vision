package com.example.trade_vision_backend.backtester.internal;

import com.example.trade_vision_backend.backtester.BackTesterService;
import com.example.trade_vision_backend.data.CsvImporterService;
import com.example.trade_vision_backend.data.MarketData;
import com.example.trade_vision_backend.strategies.StrategyService;
import com.example.trade_vision_backend.strategies.internal.Strategy;
import jakarta.annotation.Nonnull;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;

@RestController
@Slf4j
@RequestMapping("/api/v1/strategy")
@Validated
@RequiredArgsConstructor
public class BackTesterController {
    private final CsvImporterService csvImporterService;
    private final StrategyService strategyService;
    private final BackTesterService backTesterService;

    @PostMapping("/create")
    public ResponseEntity<BackTestResult> executeUserStrategy(
            @Valid @Nonnull InputStream stream,
            @Valid @RequestBody @Nonnull BackTestRequest request) throws IOException {
        MarketData marketData = csvImporterService.importCsvFromStream(stream);
        Strategy strategy = strategyService.buildStrategyFromRequest(request);
        BackTestResult result = backTesterService.runBackTest(strategy, marketData, request);

        return ResponseEntity.ok(result);
    }
}
