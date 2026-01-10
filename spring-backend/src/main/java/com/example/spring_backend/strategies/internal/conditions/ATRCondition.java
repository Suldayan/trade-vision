package com.example.spring_backend.strategies.internal.conditions;

import com.example.spring_backend.market.MarketData;
import com.example.spring_backend.indicators.IndicatorUtils;
import com.example.spring_backend.strategies.Condition;
import jakarta.annotation.Nonnull;
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

    @Override
    public boolean[] evaluateVector(@Nonnull MarketData data) {
        final int length = data.close().length;
        boolean[] signals = new boolean[length];

        if (length < period) {
            return signals;
        }

        double[] high = data.high();
        double[] low = data.low();
        double[] close = data.close();
        double[] atr = IndicatorUtils.atr(high, low, close, period);

        if (compareWithPrice) {
            // Compare price difference with ATR threshold
            for (int i = period; i < length; i++) {
                // Skip if ATR is invalid
                if (Double.isNaN(atr[i])) {
                    signals[i] = false;
                    continue;
                }

                double currentPrice = close[i];
                double previousPrice = close[i - 1];
                double priceDifference = Math.abs(currentPrice - previousPrice);
                double atrThreshold = atr[i] * multiplier;

                if (isAbove) {
                    signals[i] = priceDifference > atrThreshold;
                } else {
                    signals[i] = priceDifference < atrThreshold;
                }
            }
        } else {
            // Compare ATR value directly with threshold
            double threshold = multiplier;

            for (int i = period; i < length; i++) {
                // Skip if ATR is invalid
                if (Double.isNaN(atr[i])) {
                    signals[i] = false;
                    continue;
                }

                if (isAbove) {
                    signals[i] = atr[i] > threshold;
                } else {
                    signals[i] = atr[i] < threshold;
                }
            }
        }

        return signals;
    }
}