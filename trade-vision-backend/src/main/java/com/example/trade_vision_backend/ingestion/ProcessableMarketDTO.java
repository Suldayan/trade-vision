package com.example.trade_vision_backend.ingestion;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ProcessableMarketDTO(@Nonnull String exchangeId,
                                   @Nonnull Integer rank,
                                   @Nonnull String baseSymbol,
                                   @Nonnull String baseId,
                                   @Nonnull String quoteSymbol,
                                   @Nonnull String quoteId,
                                   @Nonnull BigDecimal priceQuote,
                                   @Nonnull BigDecimal priceUsd,
                                   @Nonnull BigDecimal volumeUsd24Hr,
                                   @Nonnull BigDecimal percentExchangeVolume,
                                   @Nullable Integer tradesCount24Hr,
                                   @Nonnull Long updated,
                                   @Nonnull Long timestamp)
{}
