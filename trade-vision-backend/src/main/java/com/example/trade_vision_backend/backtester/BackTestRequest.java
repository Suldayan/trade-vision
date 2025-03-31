package com.example.trade_vision_backend.backtester;

import jakarta.annotation.Nonnull;

public record BackTestRequest(
        @Nonnull String baseId,
        @Nonnull String quoteId,
        @Nonnull String exchangeId,
        @Nonnull Integer window,
        @Nonnull String strategy
) {
}
