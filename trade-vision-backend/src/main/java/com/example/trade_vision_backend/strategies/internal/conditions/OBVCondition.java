package com.example.trade_vision_backend.strategies.internal.conditions;

import com.example.trade_vision_backend.market.MarketData;
import com.example.trade_vision_backend.indicators.IndicatorUtils;
import com.example.trade_vision_backend.strategies.Condition;
import com.example.trade_vision_backend.strategies.internal.enums.ConditionType;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.Getter;

@RequiredArgsConstructor
public class OBVCondition implements Condition {
    private final int period;
    private final ConditionType conditionType;

    public OBVCondition(int period, boolean checkCrossAbove) {
        this.period = period;
        this.conditionType = checkCrossAbove ? ConditionType.CROSS_ABOVE_MA : ConditionType.CROSS_BELOW_MA;
    }

    public OBVCondition(int period, boolean checkAbove, boolean checkCrossing) {
        this.period = period;
        if (checkCrossing) {
            this.conditionType = checkAbove ? ConditionType.CROSS_ABOVE_MA : ConditionType.CROSS_BELOW_MA;
        } else {
            this.conditionType = checkAbove ? ConditionType.ABOVE_MA : ConditionType.BELOW_MA;
        }
    }

    @Override
    public boolean evaluate(@Nonnull MarketData data, int currentIndex) {
        if (currentIndex < 1) {
            return false;
        }

        double[] obv = IndicatorUtils.obv(data.close(), data.volume());

        if (conditionType.name().contains("MA")) {
            double[] obvMA = IndicatorUtils.sma(obv, period);

            if (currentIndex >= obvMA.length || Double.isNaN(obvMA[currentIndex])) {
                return false;
            }

            return evaluateWithMA(obv, obvMA, currentIndex);
        } else {
            return evaluateWithoutMA(obv, currentIndex);
        }
    }

    @Override
    public boolean[] evaluateVector(@Nonnull MarketData data) {
        int length = data.close().length;
        boolean[] signals = new boolean[length];

        double[] obv = IndicatorUtils.obv(data.close(), data.volume());
        double[] obvMA = null;

        if (conditionType.name().contains("MA")) {
            obvMA = IndicatorUtils.sma(obv, period);
        }

        // Start from index 1 since we need to compare with previous value
        for (int i = 1; i < length; i++) {
            if (conditionType.name().contains("MA")) {
                // MA-based conditions
                if (i >= obvMA.length || Double.isNaN(obvMA[i])) {
                    signals[i] = false;
                    continue;
                }

                signals[i] = evaluateWithMA(obv, obvMA, i);
            } else {
                // Non-MA conditions (INCREASING/DECREASING)
                signals[i] = evaluateWithoutMA(obv, i);
            }
        }

        return signals;
    }

    private boolean evaluateWithMA(double[] obv, double[] obvMA, int currentIndex) {
        return switch (conditionType) {
            case ABOVE_MA -> obv[currentIndex] > obvMA[currentIndex];
            case BELOW_MA -> obv[currentIndex] < obvMA[currentIndex];
            case CROSS_ABOVE_MA -> obv[currentIndex] > obvMA[currentIndex] &&
                    obv[currentIndex - 1] <= obvMA[currentIndex - 1];
            case CROSS_BELOW_MA -> obv[currentIndex] < obvMA[currentIndex] &&
                    obv[currentIndex - 1] >= obvMA[currentIndex - 1];
            default -> false;
        };
    }

    private boolean evaluateWithoutMA(double[] obv, int currentIndex) {
        return switch (conditionType) {
            case INCREASING -> obv[currentIndex] > obv[currentIndex - 1];
            case DECREASING -> obv[currentIndex] < obv[currentIndex - 1];
            default -> false;
        };
    }
}