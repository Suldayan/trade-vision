package com.example.trade_vision_backend.strategies.internal.conditions;

import com.example.trade_vision_backend.market.MarketData;
import com.example.trade_vision_backend.indicators.IndicatorUtils;
import com.example.trade_vision_backend.strategies.Condition;
import lombok.RequiredArgsConstructor;

/**
 * Condition that evaluates if the Rate of Change (ROC) is above or below a specified threshold.
 */
@RequiredArgsConstructor
public class ROCCondition implements Condition {
    /**
     * Direction enum to represent the comparison type
     */
    public enum Direction {
        ABOVE,
        BELOW,
        EQUAL,
        CROSSING_ABOVE,
        CROSSING_BELOW
    }

    private final int period;       // Period for ROC calculation
    private final double threshold; // Value to compare ROC against
    private final Direction direction; // Direction for comparison

    /**
     * Constructor with boolean parameter for backward compatibility
     */
    public ROCCondition(int period, double threshold, boolean isAbove) {
        this(period, threshold, isAbove ? Direction.ABOVE : Direction.BELOW);
    }

    @Override
    public boolean evaluate(MarketData data, int currentIndex) {
        // Ensure we have enough data points
        if (currentIndex < period) {
            return false;
        }

        double[] prices = data.close(); // Using close prices for ROC calculation
        double[] rocValues = IndicatorUtils.roc(prices, period);

        // Check if ROC value is valid at the current index
        if (Double.isNaN(rocValues[currentIndex])) {
            return false;
        }

        // Get current ROC value
        double currentROC = rocValues[currentIndex];

        // For crossing conditions, we need to check the previous value as well
        if ((direction == Direction.CROSSING_ABOVE || direction == Direction.CROSSING_BELOW) && currentIndex > 0) {
            if (Double.isNaN(rocValues[currentIndex - 1])) {
                return false;
            }
            double previousROC = rocValues[currentIndex - 1];

            switch (direction) {
                case CROSSING_ABOVE:
                    return previousROC <= threshold && currentROC > threshold;
                case CROSSING_BELOW:
                    return previousROC >= threshold && currentROC < threshold;
                default:
                    // This shouldn't happen, but just in case
                    return false;
            }
        }

        // For non-crossing conditions
        switch (direction) {
            case ABOVE:
                return currentROC > threshold;
            case BELOW:
                return currentROC < threshold;
            case EQUAL:
                // Using a small epsilon for floating-point comparison
                return Math.abs(currentROC - threshold) < 0.0001;
            default:
                return false;
        }
    }

    /**
     * Factory method to create a condition that checks if ROC is above a threshold.
     *
     * @param period    The period for ROC calculation
     * @param threshold The threshold value to compare against
     * @return A new ROCCondition instance
     */
    public static ROCCondition above(int period, double threshold) {
        return new ROCCondition(period, threshold, Direction.ABOVE);
    }

    /**
     * Factory method to create a condition that checks if ROC is below a threshold.
     *
     * @param period    The period for ROC calculation
     * @param threshold The threshold value to compare against
     * @return A new ROCCondition instance
     */
    public static ROCCondition below(int period, double threshold) {
        return new ROCCondition(period, threshold, Direction.BELOW);
    }

    /**
     * Factory method for crossing above condition
     */
    public static ROCCondition crossingAbove(int period, double threshold) {
        return new ROCCondition(period, threshold, Direction.CROSSING_ABOVE);
    }

    /**
     * Factory method for crossing below condition
     */
    public static ROCCondition crossingBelow(int period, double threshold) {
        return new ROCCondition(period, threshold, Direction.CROSSING_BELOW);
    }

    /**
     * Create a ROC condition from a direction string and parameters
     */
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