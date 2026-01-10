package com.example.spring_backend.strategies.internal.conditions;

import com.example.spring_backend.market.MarketData;
import com.example.spring_backend.indicators.IndicatorUtils;
import com.example.spring_backend.strategies.Condition;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class BollingerBandsCondition implements Condition {
    private final int period;
    private final double numStd;
    private final boolean checkUpper;

    @Override
    public boolean evaluate(@Nonnull MarketData data, int currentIndex) {
        Map<String, double[]> bb = IndicatorUtils.bollingerBands(data.close(), period, numStd);
        double[] upper = bb.get("upper");
        double[] lower = bb.get("lower");
        double price = data.close()[currentIndex];

        if (Double.isNaN(upper[currentIndex]) || Double.isNaN(lower[currentIndex])) {
            return false;
        }

        if (checkUpper) {
            return price > upper[currentIndex];
        } else {
            return price < lower[currentIndex];
        }
    }

    @Override
    public boolean[] evaluateVector(@Nonnull MarketData data) {
        final int length = data.close().length;
        boolean[] signals = new boolean[length];

        // Early exit if not enough data
        if (length < period) {
            return signals; // All false
        }

        double[] close = data.close();

        Map<String, double[]> bb = IndicatorUtils.bollingerBands(close, period, numStd);
        double[] upper = bb.get("upper");
        double[] lower = bb.get("lower");

        for (int i = period - 1; i < length; i++) {
            // Skip if Bollinger Bands values are invalid
            if (Double.isNaN(upper[i]) || Double.isNaN(lower[i])) {
                signals[i] = false;
                continue;
            }

            double price = close[i];

            if (checkUpper) {
                signals[i] = price > upper[i];
            } else {
                signals[i] = price < lower[i];
            }
        }

        return signals;
    }
}