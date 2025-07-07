package com.example.trade_vision_backend.strategies.internal.conditions;

import com.example.trade_vision_backend.market.MarketData;
import com.example.trade_vision_backend.strategies.Condition;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ROCCrossoverCondition implements Condition {
    private final int period;
    private final double threshold;
    private final boolean crossAbove;

    @Override
    public boolean evaluate(@Nonnull MarketData data, int currentIndex) {
        if (currentIndex <= period) {
            return false;
        }

        double[] prices = data.close();

        double currentROC = calculateROC(prices, currentIndex, period);
        double previousROC = calculateROC(prices, currentIndex - 1, period);

        return evaluateCrossover(currentROC, previousROC);
    }

    @Override
    public boolean[] evaluateVector(@Nonnull MarketData data) {
        int length = data.close().length;
        boolean[] signals = new boolean[length];
        double[] prices = data.close();

        double[] rocValues = new double[length];

        for (int i = period; i < length; i++) {
            rocValues[i] = calculateROC(prices, i, period);
        }

        // Start from period + 1 since we need both current and previous ROC values
        for (int i = period + 1; i < length; i++) {
            double currentROC = rocValues[i];
            double previousROC = rocValues[i - 1];

            signals[i] = evaluateCrossover(currentROC, previousROC);
        }

        return signals;
    }

    private boolean evaluateCrossover(double currentROC, double previousROC) {
        if (crossAbove) {
            return currentROC > threshold && previousROC <= threshold;
        } else {
            return currentROC < threshold && previousROC >= threshold;
        }
    }

    private double calculateROC(double[] prices, int index, int period) {
        double currentPrice = prices[index];
        double pastPrice = prices[index - period];

        return ((currentPrice - pastPrice) / pastPrice) * 100;
    }

    public static ROCCrossoverCondition crossesAbove(int period, double threshold) {
        return new ROCCrossoverCondition(period, threshold, true);
    }

    public static ROCCrossoverCondition crossesBelow(int period, double threshold) {
        return new ROCCrossoverCondition(period, threshold, false);
    }

    public static ROCCrossoverCondition bullishCrossover(int period) {
        return new ROCCrossoverCondition(period, 0.0, true);
    }

    public static ROCCrossoverCondition bearishCrossover(int period) {
        return new ROCCrossoverCondition(period, 0.0, false);
    }
}