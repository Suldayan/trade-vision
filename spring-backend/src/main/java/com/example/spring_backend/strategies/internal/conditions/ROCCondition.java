package com.example.spring_backend.strategies.internal.conditions;

import com.example.spring_backend.market.MarketData;
import com.example.spring_backend.indicators.IndicatorUtils;
import com.example.spring_backend.strategies.Condition;
import com.example.spring_backend.strategies.internal.enums.Direction;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ROCCondition implements Condition {
    private final int period;       // Period for ROC calculation
    private final double threshold; // Value to compare ROC against
    private final Direction direction; // Direction for comparison

    @Override
    public boolean evaluate(MarketData data, int currentIndex) {
        // Ensure we have enough data points
        if (currentIndex < period) {
            return false;
        }

        double[] prices = data.close(); // Using close prices for ROC calculation
        double[] rocValues = IndicatorUtils.roc(prices, period);

        if (Double.isNaN(rocValues[currentIndex])) {
            return false;
        }

        return evaluateROC(rocValues, currentIndex);
    }

    @Override
    public boolean[] evaluateVector(@Nonnull MarketData data) {
        int length = data.close().length;
        boolean[] signals = new boolean[length];

        double[] rocValues = IndicatorUtils.roc(data.close(), period);

        // Start from period index since we need enough data for ROC calculation
        for (int i = period; i < length; i++) {
            if (Double.isNaN(rocValues[i])) {
                signals[i] = false;
                continue;
            }

            signals[i] = evaluateROC(rocValues, i);
        }

        return signals;
    }

    private boolean evaluateROC(double[] rocValues, int currentIndex) {
        double currentROC = rocValues[currentIndex];

        // For crossing conditions, we need to check the previous value as well
        if ((direction == Direction.CROSSING_ABOVE || direction == Direction.CROSSING_BELOW) && currentIndex > 0) {
            if (Double.isNaN(rocValues[currentIndex - 1])) {
                return false;
            }
            double previousROC = rocValues[currentIndex - 1];

            return switch (direction) {
                case CROSSING_ABOVE -> previousROC <= threshold && currentROC > threshold;
                case CROSSING_BELOW -> previousROC >= threshold && currentROC < threshold;
                default ->
                    // This shouldn't happen, but just in case
                        false;
            };
        }

        // For non-crossing conditions
        return switch (direction) {
            case ABOVE -> currentROC > threshold;
            case BELOW -> currentROC < threshold;
            case EQUAL ->
                // Using a small epsilon for floating-point comparison
                    Math.abs(currentROC - threshold) < 0.0001;
            default -> false;
        };
    }

    public static ROCCondition above(int period, double threshold) {
        return new ROCCondition(period, threshold, Direction.ABOVE);
    }

    public static ROCCondition below(int period, double threshold) {
        return new ROCCondition(period, threshold, Direction.BELOW);
    }

    public static ROCCondition crossingAbove(int period, double threshold) {
        return new ROCCondition(period, threshold, Direction.CROSSING_ABOVE);
    }

    public static ROCCondition crossingBelow(int period, double threshold) {
        return new ROCCondition(period, threshold, Direction.CROSSING_BELOW);
    }

    public static ROCCondition fromDirectionString(int period, double threshold, String directionStr) {
        try {
            Direction direction = Direction.valueOf(directionStr.toUpperCase());
            return new ROCCondition(period, threshold, direction);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid direction: " + directionStr +
                    ". Valid values are ABOVE, BELOW, EQUAL, CROSSING_ABOVE, CROSSING_BELOW");
        }
    }
}