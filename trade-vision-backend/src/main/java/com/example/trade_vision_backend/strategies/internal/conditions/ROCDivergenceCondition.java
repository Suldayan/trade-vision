package com.example.trade_vision_backend.strategies.internal.conditions;

import com.example.trade_vision_backend.market.MarketData;
import com.example.trade_vision_backend.strategies.Condition;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class ROCDivergenceCondition implements Condition {
    private final int period;           // Period for ROC calculation
    private final int divergencePeriod; // Period to look for divergence
    private final boolean bullish;      // true for bullish divergence, false for bearish

    @Override
    public boolean evaluate(@Nonnull MarketData data, int currentIndex) {
        if (currentIndex < period + divergencePeriod) {
            return false;
        }

        double[] prices = data.close();
        double[] roc = calculateROCArray(prices, period, currentIndex - divergencePeriod, currentIndex);

        if (bullish) {
            return evaluateBullishDivergence(prices, roc, currentIndex);
        } else {
            return evaluateBearishDivergence(prices, roc, currentIndex);
        }
    }

    @Override
    public boolean[] evaluateVector(@Nonnull MarketData data) {
        long startTime = System.currentTimeMillis();

        double[] prices = data.close();
        int length = prices.length;
        boolean[] signals = new boolean[length];

        double[] rocValues = calculateFullROCArray(prices, period);
        LocalExtrema extrema = findLocalExtrema(prices);

        // Process each point
        for (int i = period + divergencePeriod; i < length; i++) {
            if (bullish) {
                signals[i] = evaluateBullishDivergenceVectorized(prices, rocValues, extrema, i);
            } else {
                signals[i] = evaluateBearishDivergenceVectorized(prices, rocValues, extrema, i);
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        log.debug("ROCDivergenceCondition ({}) vectorized evaluation completed in {}ms",
                bullish ? "bullish" : "bearish", duration);

        return signals;
    }

    private boolean evaluateBullishDivergence(double[] prices, double[] roc, int currentIndex) {
        double lowestPrice = Double.MAX_VALUE;
        int lowestPriceIdx = -1;
        double previousLowestPrice = Double.MAX_VALUE;
        int previousLowestPriceIdx = -1;

        // Find two most recent price lows
        for (int i = currentIndex - divergencePeriod; i <= currentIndex; i++) {
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

        if (lowestPriceIdx == -1 || previousLowestPriceIdx == -1) {
            return false;
        }

        // Ensure chronological order
        if (lowestPriceIdx < previousLowestPriceIdx) {
            int tempIdx = lowestPriceIdx;
            lowestPriceIdx = previousLowestPriceIdx;
            previousLowestPriceIdx = tempIdx;

            double tempPrice = lowestPrice;
            lowestPrice = previousLowestPrice;
            previousLowestPrice = tempPrice;
        }

        // Check divergence
        boolean priceMadeLowerLow = lowestPrice < previousLowestPrice;
        boolean rocMadeHigherLow = roc[lowestPriceIdx - (currentIndex - divergencePeriod)] >
                roc[previousLowestPriceIdx - (currentIndex - divergencePeriod)];

        return priceMadeLowerLow && rocMadeHigherLow;
    }

    private boolean evaluateBearishDivergence(double[] prices, double[] roc, int currentIndex) {
        double highestPrice = Double.MIN_VALUE;
        int highestPriceIdx = -1;
        double previousHighestPrice = Double.MIN_VALUE;
        int previousHighestPriceIdx = -1;

        // Find two most recent price highs
        for (int i = currentIndex - divergencePeriod; i <= currentIndex; i++) {
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

        if (highestPriceIdx == -1 || previousHighestPriceIdx == -1) {
            return false;
        }

        // Ensure chronological order
        if (highestPriceIdx < previousHighestPriceIdx) {
            int tempIdx = highestPriceIdx;
            highestPriceIdx = previousHighestPriceIdx;
            previousHighestPriceIdx = tempIdx;

            double tempPrice = highestPrice;
            highestPrice = previousHighestPrice;
            previousHighestPrice = tempPrice;
        }

        // Check divergence
        boolean priceMadeHigherHigh = highestPrice > previousHighestPrice;
        boolean rocMadeLowerHigh = roc[highestPriceIdx - (currentIndex - divergencePeriod)] <
                roc[previousHighestPriceIdx - (currentIndex - divergencePeriod)];

        return priceMadeHigherHigh && rocMadeLowerHigh;
    }

    private boolean evaluateBullishDivergenceVectorized(double[] prices, double[] rocValues,
                                                        @Nonnull LocalExtrema extrema, int currentIndex) {
        // Find the two most recent lows within our divergence period
        List<Integer> recentLows = new ArrayList<>();
        int startIdx = Math.max(0, currentIndex - divergencePeriod);

        for (int lowIdx : extrema.lows) {
            if (lowIdx >= startIdx && lowIdx <= currentIndex) {
                recentLows.add(lowIdx);
            }
        }

        if (recentLows.size() < 2) return false;

        // Get the two most recent lows
        int size = recentLows.size();
        int newerLowIdx = recentLows.get(size - 1);
        int olderLowIdx = recentLows.get(size - 2);

        // Check for bullish divergence
        boolean priceMadeLowerLow = prices[newerLowIdx] < prices[olderLowIdx];
        boolean rocMadeHigherLow = rocValues[newerLowIdx] > rocValues[olderLowIdx];

        return priceMadeLowerLow && rocMadeHigherLow;
    }

    private boolean evaluateBearishDivergenceVectorized(double[] prices, double[] rocValues,
                                                        @Nonnull LocalExtrema extrema, int currentIndex) {
        // Find the two most recent highs within our divergence period
        List<Integer> recentHighs = new ArrayList<>();
        int startIdx = Math.max(0, currentIndex - divergencePeriod);

        for (int highIdx : extrema.highs) {
            if (highIdx >= startIdx && highIdx <= currentIndex) {
                recentHighs.add(highIdx);
            }
        }

        if (recentHighs.size() < 2) return false;

        // Get the two most recent highs
        int size = recentHighs.size();
        int newerHighIdx = recentHighs.get(size - 1);
        int olderHighIdx = recentHighs.get(size - 2);

        // Check for bearish divergence
        boolean priceMadeHigherHigh = prices[newerHighIdx] > prices[olderHighIdx];
        boolean rocMadeLowerHigh = rocValues[newerHighIdx] < rocValues[olderHighIdx];

        return priceMadeHigherHigh && rocMadeLowerHigh;
    }

    private double[] calculateFullROCArray(double[] prices, int period) {
        double[] rocValues = new double[prices.length];

        for (int i = period; i < prices.length; i++) {
            rocValues[i] = ((prices[i] - prices[i - period]) / prices[i - period]) * 100;
        }

        return rocValues;
    }

    private LocalExtrema findLocalExtrema(double[] prices) {
        List<Integer> lows = new ArrayList<>();
        List<Integer> highs = new ArrayList<>();

        for (int i = 1; i < prices.length - 1; i++) {
            if (prices[i] < prices[i-1] && prices[i] < prices[i+1]) {
                lows.add(i);
            } else if (prices[i] > prices[i-1] && prices[i] > prices[i+1]) {
                highs.add(i);
            }
        }

        return new LocalExtrema(lows, highs);
    }

    private double[] calculateROCArray(double[] prices, int period, int startIndex, int endIndex) {
        double[] rocValues = new double[endIndex - startIndex + 1];

        for (int i = startIndex; i <= endIndex; i++) {
            if (i >= period) {
                rocValues[i - startIndex] = ((prices[i] - prices[i - period]) / prices[i - period]) * 100;
            } else {
                rocValues[i - startIndex] = 0;
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

    private record LocalExtrema(List<Integer> lows, List<Integer> highs) {
    }
}