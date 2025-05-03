package com.example.trade_vision_backend.strategies.internal.conditions;

import com.example.trade_vision_backend.market.MarketData;
import com.example.trade_vision_backend.indicators.IndicatorUtils;
import com.example.trade_vision_backend.strategies.Condition;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class BollingerBandsCondition implements Condition {
    private final int period;
    private final double numStd;
    private final boolean checkUpper;

    @Override
    public boolean evaluate(MarketData data, int currentIndex) {
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
}
