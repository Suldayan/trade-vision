package com.example.trade_vision_backend.strategies.internal.conditions;

import com.example.trade_vision_backend.market.MarketData;
import com.example.trade_vision_backend.strategies.Condition;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ROCDivergenceCondition implements Condition {
    private final int period;           // Period for ROC calculation
    private final int divergencePeriod; // Period to look for divergence
    private final boolean bullish;      // true for bullish divergence, false for bearish

    @Override
    public boolean evaluate(MarketData data, int currentIndex) {
        // Need enough data for calculations
        if (currentIndex < period + divergencePeriod) {
            return false;
        }

        double[] prices = data.close();
        double[] roc = calculateROCArray(prices, period, currentIndex - divergencePeriod, currentIndex);

        if (bullish) {
            double lowestPrice = Double.MAX_VALUE;
            int lowestPriceIdx = -1;
            double previousLowestPrice = Double.MAX_VALUE;
            int previousLowestPriceIdx = -1;

            // Find two most recent price lows
            for (int i = currentIndex - divergencePeriod; i <= currentIndex; i++) {
                // Simple low detection (could be improved with local minima detection)
                if (i > 0 && i < prices.length - 1 &&
                        prices[i] < prices[i-1] && prices[i] < prices[i+1]) {

                    if (prices[i] < lowestPrice) {
                        previousLowestPrice = lowestPrice;
                        previousLowestPriceIdx = lowestPriceIdx;
                        lowestPrice = prices[i];
                        lowestPriceIdx = i;
                    } else if (prices[i] < previousLowestPrice) {
                        previousLowestPrice = prices[i];
                        previousLowestPriceIdx = i;
                    }
                }
            }

            // We need two distinct low points
            if (lowestPriceIdx == -1 || previousLowestPriceIdx == -1) {
                return false;
            }

            // Ensure we're processing them in chronological order
            if (lowestPriceIdx < previousLowestPriceIdx) {
                int tempIdx = lowestPriceIdx;
                lowestPriceIdx = previousLowestPriceIdx;
                previousLowestPriceIdx = tempIdx;

                double tempPrice = lowestPrice;
                lowestPrice = previousLowestPrice;
                previousLowestPrice = tempPrice;
            }

            // Check if price made lower low but ROC made higher low (bullish divergence)
            boolean priceMadeLowerLow = lowestPrice < previousLowestPrice;
            boolean rocMadeHigherLow = roc[lowestPriceIdx - (currentIndex - divergencePeriod)] >
                    roc[previousLowestPriceIdx - (currentIndex - divergencePeriod)];

            return priceMadeLowerLow && rocMadeHigherLow;

        } else {
            // Bearish divergence: Higher price highs but lower ROC highs
            // Find the highest price in divergence period
            double highestPrice = Double.MIN_VALUE;
            int highestPriceIdx = -1;
            double previousHighestPrice = Double.MIN_VALUE;
            int previousHighestPriceIdx = -1;

            // Find two most recent price highs
            for (int i = currentIndex - divergencePeriod; i <= currentIndex; i++) {
                // Simple high detection (could be improved with local maxima detection)
                if (i > 0 && i < prices.length - 1 &&
                        prices[i] > prices[i-1] && prices[i] > prices[i+1]) {

                    if (prices[i] > highestPrice) {
                        previousHighestPrice = highestPrice;
                        previousHighestPriceIdx = highestPriceIdx;
                        highestPrice = prices[i];
                        highestPriceIdx = i;
                    } else if (prices[i] > previousHighestPrice) {
                        previousHighestPrice = prices[i];
                        previousHighestPriceIdx = i;
                    }
                }
            }

            // We need two distinct high points
            if (highestPriceIdx == -1 || previousHighestPriceIdx == -1) {
                return false;
            }

            // Ensure we're processing them in chronological order
            if (highestPriceIdx < previousHighestPriceIdx) {
                int tempIdx = highestPriceIdx;
                highestPriceIdx = previousHighestPriceIdx;
                previousHighestPriceIdx = tempIdx;

                double tempPrice = highestPrice;
                highestPrice = previousHighestPrice;
                previousHighestPrice = tempPrice;
            }

            // Check if price made higher high but ROC made lower high (bearish divergence)
            boolean priceMadeHigherHigh = highestPrice > previousHighestPrice;
            boolean rocMadeLowerHigh = roc[highestPriceIdx - (currentIndex - divergencePeriod)] <
                    roc[previousHighestPriceIdx - (currentIndex - divergencePeriod)];

            return priceMadeHigherHigh && rocMadeLowerHigh;
        }
    }

    private double[] calculateROCArray(double[] prices, int period, int startIndex, int endIndex) {
        // Create array to hold ROC values for the range
        double[] rocValues = new double[endIndex - startIndex + 1];

        for (int i = startIndex; i <= endIndex; i++) {
            if (i >= period) {
                rocValues[i - startIndex] = ((prices[i] - prices[i - period]) / prices[i - period]) * 100;
            } else {
                rocValues[i - startIndex] = 0; // Default value when not enough data
            }
        }

        return rocValues;
    }

    public static ROCDivergenceCondition bullish(int period, int divergencePeriod) {
        return new ROCDivergenceCondition(period, divergencePeriod, true);
    }

    public static ROCDivergenceCondition bearish(int period, int divergencePeriod) {
        return new ROCDivergenceCondition(period, divergencePeriod, false);
    }
}