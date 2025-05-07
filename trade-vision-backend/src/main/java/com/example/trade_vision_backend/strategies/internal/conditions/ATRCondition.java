package com.example.trade_vision_backend.strategies.internal.conditions;

import com.example.trade_vision_backend.market.MarketData;
import com.example.trade_vision_backend.indicators.IndicatorUtils;
import com.example.trade_vision_backend.strategies.Condition;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ATRCondition implements Condition {
    private final int period;
    private final double multiplier;
    private final boolean isAbove;
    private final boolean compareWithPrice;

    @Override
    public boolean evaluate(MarketData data, int currentIndex) {
        if (currentIndex < period) return false;

        double[] high = data.high();
        double[] low = data.low();
        double[] close = data.close();
        double[] atr = IndicatorUtils.atr(high, low, close, period);

        if (Double.isNaN(atr[currentIndex])) {
            return false;
        }

        if (compareWithPrice) {
            double currentPrice = close[currentIndex];
            double previousPrice = close[currentIndex - 1];
            double priceDifference = Math.abs(currentPrice - previousPrice);
            double atrThreshold = atr[currentIndex] * multiplier;

            if (isAbove) {
                return priceDifference > atrThreshold;
            } else {
                return priceDifference < atrThreshold;
            }
        } else {
            // Compare ATR value directly with a threshold
            double threshold = multiplier; // In this case, multiplier is used as a direct threshold

            if (isAbove) {
                return atr[currentIndex] > threshold;
            } else {
                return atr[currentIndex] < threshold;
            }
        }
    }
}