package com.example.trade_vision_backend.backtester.internal;

import com.example.trade_vision_backend.backtester.BackTestEvent;
import com.example.trade_vision_backend.backtester.BackTesterManagement;
import com.example.trade_vision_backend.datastore.DataStoreService;
import jakarta.annotation.Nonnull;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/api/v1/test")
@Validated
@RequiredArgsConstructor
public class BackTesterController {
    private final BackTesterManagement management;
    private final DataStoreService store;

    @PostMapping("/ema")
    public ResponseEntity<String> executeEmaStrategy(@Valid @Nonnull @RequestBody BackTestRequest request) {
        final String baseId = request.baseId();
        final String quoteId = request.quoteId();
        final String exchangeId = request.exchangeId();
        final String strategy = request.strategy();
        final Long window = request.window();

        log.info("Publishing event with ids: {}, {}, {} with strategy: {} over {} days",
                baseId, quoteId, exchangeId, strategy, window);

        management.complete(
                strategy,
                baseId,
                quoteId,
                exchangeId,
                window
        );

        return ResponseEntity.ok("");
    }
}
