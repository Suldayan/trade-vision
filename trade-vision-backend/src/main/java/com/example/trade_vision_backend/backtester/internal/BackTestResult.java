package com.example.trade_vision_backend.backtester.internal;

import lombok.Builder;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Builder
@RequiredArgsConstructor
public class BackTestResult {
    private final double totalReturn;
    private final double finalCapital;
    private final int tradeCount;
    private final double winRatio;
    private final double maxDrawdown;
    private final List<Trade> trades;
    private final List<Double> equityCurve;
}
