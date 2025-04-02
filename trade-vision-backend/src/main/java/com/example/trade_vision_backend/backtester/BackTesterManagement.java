package com.example.trade_vision_backend.backtester;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class BackTesterManagement {
    private final ApplicationEventPublisher eventPublisher;

    public void complete(
            @Nonnull String strategy,
            @Nonnull String baseId,
            @Nonnull String quoteId,
            @Nonnull String exchangeId,
            @Nonnull Long window
    ) {
        eventPublisher.publishEvent(new BackTestEvent(
                UUID.randomUUID(),
                strategy,
                baseId,
                quoteId,
                exchangeId,
                window
        ));
    }
}
