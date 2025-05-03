package com.example.trade_vision_backend.strategies.internal.conditions;

import com.example.trade_vision_backend.market.MarketData;
import com.example.trade_vision_backend.strategies.Condition;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FibonacciRetracementCondition implements Condition {
    private final int lookbackPeriod;
    private final double level;
    private final boolean isBullish;
    private final double tolerance;

    @Override
    public boolean evaluate(MarketData data, int currentIndex) {
        if (currentIndex < lookbackPeriod) {
            return false;
        }

        double[] high = data.high();
        double[] low = data.low();
        double[] close = data.close();

        // Find swing high and swing low in the lookback period
        double swingHigh = Double.MIN_VALUE;
        double swingLow = Double.MAX_VALUE;
        int swingHighIndex = -1;
        int swingLowIndex = -1;

        for (int i = currentIndex - lookbackPeriod; i < currentIndex; i++) {
            if (high[i] > swingHigh) {
                swingHigh = high[i];
                swingHighIndex = i;
            }
            if (low[i] < swingLow) {
                swingLow = low[i];
                swingLowIndex = i;
            }
        }

        if (swingHighIndex == -1 || swingLowIndex == -1) {
            return false;
        }

        double currentPrice = close[currentIndex];
        double retracementLevel;

        // Determine the most recent trend
        boolean recentTrendIsBullish = swingHighIndex > swingLowIndex;

        // If we're looking for a bullish condition but recent trend is bearish (or vice versa),
        // we might want to ignore this condition
        if (isBullish != recentTrendIsBullish) {
            return false;
        }

        // Calculate the retracement level
        if (isBullish) {
            retracementLevel = swingHigh - (swingHigh - swingLow) * level;

            // Check if price is near the retracement level and bouncing up from it (support)
            double lowerBound = retracementLevel * (1 - tolerance);
            double upperBound = retracementLevel * (1 + tolerance);

            return currentPrice >= lowerBound && currentPrice <= upperBound &&
                    close[currentIndex] > close[currentIndex - 1]; // Showing some upward movement
        } else {
            // For bearish trends, we calculate retracement up from low to high
            retracementLevel = swingLow + (swingHigh - swingLow) * level;

            // Check if price is near the retracement level and bouncing down from it (resistance)
            double lowerBound = retracementLevel * (1 - tolerance);
            double upperBound = retracementLevel * (1 + tolerance);

            return currentPrice >= lowerBound && currentPrice <= upperBound &&
                    close[currentIndex] < close[currentIndex - 1];
        }
    }
}