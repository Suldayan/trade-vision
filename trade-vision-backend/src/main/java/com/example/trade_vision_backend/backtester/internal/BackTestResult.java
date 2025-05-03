package com.example.trade_vision_backend.backtester.internal;

import lombok.Builder;

import java.util.List;

@Builder
public record BackTestResult(
        double totalReturn,
        double finalCapital,
        int tradeCount,
        double winRatio,
        double maxDrawdown,
        List<Trade> trades,
        List<Double> equityCurve) {
}
