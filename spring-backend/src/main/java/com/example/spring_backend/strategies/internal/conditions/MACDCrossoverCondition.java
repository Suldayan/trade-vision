package com.example.spring_backend.strategies.internal.conditions;

import com.example.spring_backend.market.MarketData;
import com.example.spring_backend.indicators.IndicatorUtils;
import com.example.spring_backend.strategies.Condition;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class MACDCrossoverCondition implements Condition {
    private final int fastPeriod;
    private final int slowPeriod;
    private final int signalPeriod;
    private final boolean crossAbove;

    @Override
    public boolean evaluate(@Nonnull MarketData data, int currentIndex) {
        if (currentIndex < 1) return false;

        Map<String, double[]> macd = IndicatorUtils.macd(data.close(), fastPeriod, slowPeriod, signalPeriod);
        double[] macdLine = macd.get("macdLine");
        double[] signalLine = macd.get("signalLine");

        if (Double.isNaN(macdLine[currentIndex]) || Double.isNaN(signalLine[currentIndex]) ||
                Double.isNaN(macdLine[currentIndex-1]) || Double.isNaN(signalLine[currentIndex-1])) {
            return false;
        }

        return evaluateCrossover(macdLine, signalLine, currentIndex);
    }

    @Override
    public boolean[] evaluateVector(@Nonnull MarketData data) {
        int length = data.close().length;
        boolean[] signals = new boolean[length];

        Map<String, double[]> macd = IndicatorUtils.macd(data.close(), fastPeriod, slowPeriod, signalPeriod);
        double[] macdLine = macd.get("macdLine");
        double[] signalLine = macd.get("signalLine");

        // Start from index 1 since we need to compare with previous value
        for (int i = 1; i < length; i++) {
            // Check for NaN values to avoid false signals
            if (Double.isNaN(macdLine[i]) || Double.isNaN(signalLine[i]) ||
                    Double.isNaN(macdLine[i-1]) || Double.isNaN(signalLine[i-1])) {
                signals[i] = false;
                continue;
            }

            signals[i] = evaluateCrossover(macdLine, signalLine, i);
        }

        return signals;
    }

    private boolean evaluateCrossover(double[] macdLine, double[] signalLine, int currentIndex) {
        if (crossAbove) {
            // MACD crosses above signal line
            return macdLine[currentIndex-1] <= signalLine[currentIndex-1] &&
                    macdLine[currentIndex] > signalLine[currentIndex];
        } else {
            // MACD crosses below signal line
            return macdLine[currentIndex-1] >= signalLine[currentIndex-1] &&
                    macdLine[currentIndex] < signalLine[currentIndex];
        }
    }
}