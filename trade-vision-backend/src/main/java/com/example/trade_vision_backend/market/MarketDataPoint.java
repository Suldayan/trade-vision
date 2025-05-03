package com.example.trade_vision_backend.market;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record MarketDataPoint(
        LocalDateTime timestamp,
        double open,
        double high,
        double low,
        double close,
        double adjustedClose,
        long volume,
        double dividendAmount,
        double splitCoefficient
) {
}
