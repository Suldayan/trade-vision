package com.example.trade_vision_backend.strategies.internal.conditions;

import com.example.trade_vision_backend.market.MarketData;
import com.example.trade_vision_backend.indicators.IndicatorUtils;
import com.example.trade_vision_backend.strategies.Condition;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class StochasticCondition implements Condition {
    private final int kPeriod;
    private final int dPeriod;
    private final double upperThreshold;
    private final double lowerThreshold;
    private final boolean checkOverbought;

    @Override
    public boolean evaluate(MarketData data, int currentIndex) {
        Map<String, double[]> stoch = IndicatorUtils.stochastic(
                data.high(), data.low(), data.close(), kPeriod, dPeriod);

        double[] k = stoch.get("%K");
        double[] d = stoch.get("%D");

        if (Double.isNaN(k[currentIndex]) || Double.isNaN(d[currentIndex])) {
            return false;
        }

        if (checkOverbought) {
            return k[currentIndex] > upperThreshold && d[currentIndex] > upperThreshold;
        } else {
            return k[currentIndex] < lowerThreshold && d[currentIndex] < lowerThreshold;
        }
    }
}
