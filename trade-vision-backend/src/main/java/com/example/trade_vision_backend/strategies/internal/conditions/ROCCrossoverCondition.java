package com.example.trade_vision_backend.strategies.internal.conditions;

import com.example.trade_vision_backend.market.MarketData;
import com.example.trade_vision_backend.strategies.Condition;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ROCCrossoverCondition implements Condition {
    private final int period;
    private final double threshold;
    private final boolean crossAbove;

    @Override
    public boolean evaluate(MarketData data, int currentIndex) {
        if (currentIndex <= period) {
            return false;
        }

        double[] prices = data.close();

        double currentROC = calculateROC(prices, currentIndex, period);
        double previousROC = calculateROC(prices, currentIndex - 1, period);

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