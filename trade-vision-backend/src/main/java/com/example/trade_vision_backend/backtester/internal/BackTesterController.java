package com.example.trade_vision_backend.backtester.internal;

import com.example.trade_vision_backend.common.BackTestRequest;
import com.example.trade_vision_backend.backtester.BackTesterOrchestrationService;
import jakarta.annotation.Nonnull;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@Slf4j
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/backtest")
public class BackTesterController {
    private final BackTesterOrchestrationService orchestrationService;

    @PostMapping(value = "/execute", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CompletableFuture<ResponseEntity<List<BackTestResult>>> executeBacktest(
            @RequestPart("file") @Valid @Nonnull MultipartFile file,
            @RequestPart("request") @Valid @Nonnull List<BackTestRequest> requests) {

        log.info("Starting backtest execution with {} requests", requests.size());

        return orchestrationService.runOrchestration(file, requests)
                .thenApply(results -> {
                    log.info("Backtest execution completed with {} results", results.size());
                    return ResponseEntity.ok(results);
                })
                .exceptionally(throwable -> {
                    log.error("Backtest execution failed", throwable);
                    return ResponseEntity.internalServerError().build();
                });
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "timestamp", String.valueOf(System.currentTimeMillis())
        ));
    }
}