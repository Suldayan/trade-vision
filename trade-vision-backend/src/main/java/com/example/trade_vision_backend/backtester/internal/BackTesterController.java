package com.example.trade_vision_backend.backtester.internal;

import com.example.trade_vision_backend.backtester.BackTesterManagement;
import jakarta.annotation.Nonnull;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/v1/test")
@Validated
@RequiredArgsConstructor
public class BackTesterController {
    private final BackTesterManagement management;

    @PostMapping("/ema")
    public ResponseEntity<String> executeEmaStrategy(@Valid @Nonnull @RequestBody BackTestRequest request) {
        final String baseId = request.baseId();
        final String quoteId = request.quoteId();
        final String exchangeId = request.exchangeId();
        final String strategy = request.strategy();
        final int period = request.period();

        log.info("Publishing event with ids: {}, {}, {} with strategy: {} over {} days",
                baseId, quoteId, exchangeId, strategy, period);

        management.complete(
                strategy,
                baseId,
                quoteId,
                exchangeId,
                period
        );

        return ResponseEntity.ok("");
    }
}
