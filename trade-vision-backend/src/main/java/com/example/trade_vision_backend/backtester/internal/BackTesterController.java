package com.example.trade_vision_backend.backtester.internal;

import com.example.trade_vision_backend.backtester.BackTestEvent;
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
    private final ApplicationEventPublisher eventPublisher;
    private final DataStoreService store;

    @PostMapping("/ema")
    public ResponseEntity<String> ema(@Valid @Nonnull @RequestBody BackTestRequest request) {
        log.info("Publishing event with ids: {}, {}, {} with strategy: {} over {} days",
                request.baseId(), request.quoteId(), request.exchangeId(), request.strategy(), request.window());

        eventPublisher.publishEvent(new BackTestEvent(
                UUID.randomUUID(),
                request.strategy(),
                request.baseId(),
                request.quoteId(),
                request.exchangeId(),
                request.window()
        ));

        return ResponseEntity.ok("");
    }
}
