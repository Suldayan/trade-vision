package com.example.trade_vision_backend.backtester;

import jakarta.annotation.Nonnull;
import org.jmolecules.event.types.DomainEvent;

import java.util.UUID;

public record StrategyEvent(
        UUID id,
        @Nonnull String strategy,
        @Nonnull String baseId,
        @Nonnull String quoteId,
        @Nonnull String exchangeId,
        @Nonnull Long window
) implements DomainEvent {
}
