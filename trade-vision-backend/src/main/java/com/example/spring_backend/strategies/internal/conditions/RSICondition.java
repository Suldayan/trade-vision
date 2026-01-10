package com.example.spring_backend.strategies.internal.conditions;

import com.example.spring_backend.market.MarketData;
import com.example.spring_backend.indicators.IndicatorUtils;
import com.example.spring_backend.strategies.Condition;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class RSICondition implements Condition {
    private final int period;
    private final double upperThreshold;
    private final double lowerThreshold;
    private final boolean checkOverbought;

    @Override
    public boolean evaluate(@Nonnull MarketData data, int currentIndex) {
        // Keep original implementation for single-point evaluation
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

    @Override
    public boolean[] evaluateVector(@Nonnull MarketData data) {
        long startTime = System.currentTimeMillis();

        double[] prices = data.close();
        int length = prices.length;
        boolean[] signals = new boolean[length];

        // Calculate RSI once for the entire dataset
        double[] rsi = IndicatorUtils.rsi(prices, period);

        // Vectorized threshold comparison
        if (checkOverbought) {
            // Check for overbought condition (RSI > upperThreshold)
            for (int i = 0; i < length; i++) {
                signals[i] = !Double.isNaN(rsi[i]) && rsi[i] > upperThreshold;
            }
        } else {
            // Check for oversold condition (RSI < lowerThreshold)
            for (int i = 0; i < length; i++) {
                signals[i] = !Double.isNaN(rsi[i]) && rsi[i] < lowerThreshold;
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        log.debug("RSICondition ({}) vectorized evaluation completed in {}ms",
                checkOverbought ? "overbought" : "oversold", duration);

        return signals;
    }

    // Factory methods for common RSI conditions
    public static RSICondition overbought(int period, double threshold) {
        return new RSICondition(period, threshold, 0, true);
    }

    public static RSICondition oversold(int period, double threshold) {
        return new RSICondition(period, 0, threshold, false);
    }

    public static RSICondition overbought(int period) {
        return overbought(period, 70.0);
    }

    public static RSICondition oversold(int period) {
        return oversold(period, 30.0);
    }
}