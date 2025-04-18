package com.example.trade_vision_backend.indicators.internal;

import com.example.trade_vision_backend.data.MarketData;
import com.example.trade_vision_backend.indicators.IndicatorUtils;
import com.example.trade_vision_backend.strategies.Condition;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SMACrossoverCondition implements Condition {
    private final int fastPeriod;
    private final int slowPeriod;
    private final boolean crossAbove; // true for cross above, false for cross below

    @Override
    public boolean evaluate(MarketData data, int currentIndex) {
        if (currentIndex < 1) return false;

        double[] fastSMA = IndicatorUtils.sma(data.getClose(), fastPeriod);
        double[] slowSMA = IndicatorUtils.sma(data.getClose(), slowPeriod);

        // Check for valid data points
        if (Double.isNaN(fastSMA[currentIndex]) || Double.isNaN(slowSMA[currentIndex]) ||
                Double.isNaN(fastSMA[currentIndex-1]) || Double.isNaN(slowSMA[currentIndex-1])) {
            return false;
        }

        if (crossAbove) {
            return fastSMA[currentIndex-1] <= slowSMA[currentIndex-1] &&
                    fastSMA[currentIndex] > slowSMA[currentIndex];
        } else {
            return fastSMA[currentIndex-1] >= slowSMA[currentIndex-1] &&
                    fastSMA[currentIndex] < slowSMA[currentIndex];
        }
    }
}