package com.example.trade_vision_backend.strategies.internal.conditions;

import com.example.trade_vision_backend.market.MarketData;
import com.example.trade_vision_backend.indicators.IndicatorUtils;
import com.example.trade_vision_backend.strategies.Condition;
import com.example.trade_vision_backend.strategies.internal.enums.ConditionType;
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
    public boolean evaluate(MarketData data, int currentIndex) {
        if (currentIndex < 1) {
            return false;
        }

        double[] obv = IndicatorUtils.obv(data.close(), data.volume());

        // If we need to compare with moving average
        if (conditionType.name().contains("MA")) {
            double[] obvMA = IndicatorUtils.sma(obv, period);

            if (currentIndex >= obvMA.length || Double.isNaN(obvMA[currentIndex])) {
                return false;
            }

            return switch (conditionType) {
                case ABOVE_MA -> obv[currentIndex] > obvMA[currentIndex];
                case BELOW_MA -> obv[currentIndex] < obvMA[currentIndex];
                case CROSS_ABOVE_MA -> obv[currentIndex] > obvMA[currentIndex] && obv[currentIndex - 1] <= obvMA[currentIndex - 1];
                case CROSS_BELOW_MA -> obv[currentIndex] < obvMA[currentIndex] && obv[currentIndex - 1] >= obvMA[currentIndex - 1];
                default -> false;
            };
        } else {
            return switch (conditionType) {
                case INCREASING -> obv[currentIndex] > obv[currentIndex - 1];
                case DECREASING -> obv[currentIndex] < obv[currentIndex - 1];
                default -> false;
            };
        }
    }
}