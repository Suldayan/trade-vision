package com.example.trade_vision_backend.strategies.internal.conditions;

import com.example.trade_vision_backend.market.MarketData;
import com.example.trade_vision_backend.indicators.IndicatorUtils;
import com.example.trade_vision_backend.strategies.Condition;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class MACDCrossoverCondition implements Condition {
    private final int fastPeriod;
    private final int slowPeriod;
    private final int signalPeriod;
    private final boolean crossAbove;

    @Override
    public boolean evaluate(MarketData data, int currentIndex) {
        if (currentIndex < 1) return false;

        Map<String, double[]> macd = IndicatorUtils.macd(data.close(), fastPeriod, slowPeriod, signalPeriod);
        double[] macdLine = macd.get("macdLine");
        double[] signalLine = macd.get("signalLine");

        if (Double.isNaN(macdLine[currentIndex]) || Double.isNaN(signalLine[currentIndex]) ||
                Double.isNaN(macdLine[currentIndex-1]) || Double.isNaN(signalLine[currentIndex-1])) {
            return false;
        }

        if (crossAbove) {
            return macdLine[currentIndex-1] <= signalLine[currentIndex-1] &&
                    macdLine[currentIndex] > signalLine[currentIndex];
        } else {
            return macdLine[currentIndex-1] >= signalLine[currentIndex-1] &&
                    macdLine[currentIndex] < signalLine[currentIndex];
        }
    }
}
