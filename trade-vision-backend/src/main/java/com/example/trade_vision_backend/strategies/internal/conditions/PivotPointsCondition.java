package com.example.trade_vision_backend.strategies.internal.conditions;

import com.example.trade_vision_backend.indicators.PivotType;
import com.example.trade_vision_backend.market.MarketData;
import com.example.trade_vision_backend.indicators.IndicatorUtils;
import com.example.trade_vision_backend.strategies.Condition;
import com.example.trade_vision_backend.strategies.internal.enums.PivotLevel;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class PivotPointsCondition implements Condition {
    private final PivotType pivotType;
    private final PivotLevel pivotLevel;
    private final boolean crossAbove; // true for crossing above, false for crossing below
    private final boolean useClose; // true to use close price, false to use low/high

    @Override
    public boolean evaluate(MarketData data, int currentIndex) {
        if (currentIndex < 1) return false;

        double[] high = data.high();
        double[] low = data.low();
        double[] close = data.close();
        double[] open = data.open();

        Map<String, double[]> pivotPoints = IndicatorUtils.pivotPoints(high, low, close, open, pivotType);
        String pivotLevelKey = pivotLevel.name();

        if (!pivotPoints.containsKey(pivotLevelKey)) {
            throw new IllegalArgumentException("Invalid pivot level: " + pivotLevel);
        }

        double[] pivotLevelValues = pivotPoints.get(pivotLevelKey);

        if (Double.isNaN(pivotLevelValues[currentIndex]) ||
                Double.isNaN(pivotLevelValues[currentIndex - 1])) {
            return false;
        }

        double currentPrice, previousPrice;

        if (useClose) {
            currentPrice = close[currentIndex];
            previousPrice = close[currentIndex - 1];
        } else {
            if (crossAbove) {
                previousPrice = low[currentIndex - 1];
                currentPrice = high[currentIndex];
            } else {
                previousPrice = high[currentIndex - 1];
                currentPrice = low[currentIndex];
            }
        }

        double currentPivotLevel = pivotLevelValues[currentIndex];
        double previousPivotLevel = pivotLevelValues[currentIndex - 1];

        if (crossAbove) {
            return previousPrice < previousPivotLevel && currentPrice > currentPivotLevel;
        } else {
            return previousPrice > previousPivotLevel && currentPrice < currentPivotLevel;
        }
    }
    
    @Override
    public String toString() {
        String direction = crossAbove ? "crosses above" : "crosses below";
        String priceType = useClose ? "Close price" : "High/Low price";
        return String.format("%s %s %s pivot level (%s)",
                priceType, direction, pivotLevel.name(), pivotType);
    }
}