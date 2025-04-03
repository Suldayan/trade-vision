package com.example.trade_vision_backend.backtester.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.Min;

public record BackTestRequest(
        @Nonnull @JsonProperty String strategy,
        @Nonnull @JsonProperty String baseId,
        @Nonnull @JsonProperty String quoteId,
        @Nonnull @JsonProperty String exchangeId,
        @Min(1) @JsonProperty int period
) {
}
