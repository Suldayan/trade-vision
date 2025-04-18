package com.example.trade_vision_backend.indicators.internal;

import com.example.trade_vision_backend.data.MarketData;
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
                data.getHigh(), data.getLow(), data.getClose(), kPeriod, dPeriod);

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
