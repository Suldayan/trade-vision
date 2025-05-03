package com.example.trade_vision_backend.strategies.internal.conditions;

import com.example.trade_vision_backend.market.MarketData;
import com.example.trade_vision_backend.indicators.IndicatorUtils;
import com.example.trade_vision_backend.strategies.Condition;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RSICondition implements Condition {
    private final int period;
    private final double upperThreshold;
    private final double lowerThreshold;
    private final boolean checkOverbought;

    @Override
    public boolean evaluate(MarketData data, int currentIndex) {
        double[] rsi = IndicatorUtils.rsi(data.close(), period);

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
