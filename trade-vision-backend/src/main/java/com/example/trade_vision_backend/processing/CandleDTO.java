package com.example.trade_vision_backend.processing;

import jakarta.annotation.Nonnull;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Builder
public record CandleDTO(
        @Nonnull String baseId,
        @Nonnull String quoteId,
        @Nonnull String exchangeId,
        @Nonnull BigDecimal closingPrice,
        @Nonnull ZonedDateTime timestamp
        ) {
}
