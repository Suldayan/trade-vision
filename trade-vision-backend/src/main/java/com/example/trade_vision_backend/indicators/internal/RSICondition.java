package com.example.trade_vision_backend.indicators.internal;

import com.example.trade_vision_backend.data.MarketData;
import com.example.trade_vision_backend.indicators.IndicatorUtils;
import com.example.trade_vision_backend.strategies.Condition;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RSICondition implements Condition {
    private final int period;
    private final double upperThreshold;
    private final double lowerThreshold;
    private final boolean checkOverbought; // true for overbought, false for oversold

    @Override
    public boolean evaluate(MarketData data, int currentIndex) {
        double[] rsi = IndicatorUtils.rsi(data.getClose(), period);

        if (Double.isNaN(rsi[currentIndex])) {
            return false;
        }

        if (checkOverbought) {
            return rsi[currentIndex] > upperThreshold;
        } else {
            return rsi[currentIndex] < lowerThreshold;
        }
    }
}
