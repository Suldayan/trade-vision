package com.example.trade_vision_backend.backtester;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.jmolecules.event.types.DomainEvent;

import java.util.UUID;

public record BackTestEvent(
        @Nonnull UUID id,
        @Nonnull @NotBlank String strategy,
        @Nonnull @NotBlank String baseId,
        @Nonnull @NotBlank String quoteId,
        @Nonnull @NotBlank String exchangeId,
        @Min(1) int period
) implements DomainEvent {
}
