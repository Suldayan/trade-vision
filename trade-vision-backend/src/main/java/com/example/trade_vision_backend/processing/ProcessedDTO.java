package com.example.trade_vision_backend.processing;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;

@Builder
public record ProcessedDTO(@Nonnull String baseId,
                           @Nonnull String quoteId,
                           @Nonnull String exchangeId,
                           @Nonnull BigDecimal priceUsd,
                           @Nullable BigDecimal closingPriceUsd,
                           @Nonnull Long updated,
                           @Nonnull ZonedDateTime timestamp,
                           @Nonnull Instant createdAt) {
}
