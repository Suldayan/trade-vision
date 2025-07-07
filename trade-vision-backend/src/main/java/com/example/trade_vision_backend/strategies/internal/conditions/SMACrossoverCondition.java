package com.example.trade_vision_backend.strategies.internal.conditions;

import com.example.trade_vision_backend.market.MarketData;
import com.example.trade_vision_backend.indicators.IndicatorUtils;
import com.example.trade_vision_backend.strategies.Condition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class SMACrossoverCondition implements Condition {
    private final int fastPeriod;
    private final int slowPeriod;
    private final boolean crossAbove;

    @Override
    public boolean evaluate(MarketData data, int currentIndex) {
        if (currentIndex < 1) return false;

        double[] fastSMA = IndicatorUtils.sma(data.close(), fastPeriod);
        double[] slowSMA = IndicatorUtils.sma(data.close(), slowPeriod);

        if (Double.isNaN(fastSMA[currentIndex]) || Double.isNaN(slowSMA[currentIndex]) ||
                Double.isNaN(fastSMA[currentIndex-1]) || Double.isNaN(slowSMA[currentIndex-1])) {
            return false;
        }

        if (crossAbove) {
            return fastSMA[currentIndex-1] <= slowSMA[currentIndex-1] &&
                    fastSMA[currentIndex] > slowSMA[currentIndex];
        } else {
            return fastSMA[currentIndex-1] >= slowSMA[currentIndex-1] &&
                    fastSMA[currentIndex] < slowSMA[currentIndex];
        }
    }

    @Override
    public boolean[] evaluateVector(MarketData data) {
        long startTime = System.currentTimeMillis();

        double[] prices = data.close();
        int length = prices.length;
        boolean[] signals = new boolean[length];

        double[] fastSMA = IndicatorUtils.sma(prices, fastPeriod);
        double[] slowSMA = IndicatorUtils.sma(prices, slowPeriod);

        for (int i = 1; i < length; i++) {
            if (Double.isNaN(fastSMA[i]) || Double.isNaN(slowSMA[i]) ||
                    Double.isNaN(fastSMA[i-1]) || Double.isNaN(slowSMA[i-1])) {
                signals[i] = false;
                continue;
            }

            if (crossAbove) {
                // Golden cross: fast SMA crosses above slow SMA
                signals[i] = fastSMA[i-1] <= slowSMA[i-1] && fastSMA[i] > slowSMA[i];
            } else {
                // Death cross: fast SMA crosses below slow SMA
                signals[i] = fastSMA[i-1] >= slowSMA[i-1] && fastSMA[i] < slowSMA[i];
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        log.debug("SMACrossoverCondition ({}/{} {}) vectorized evaluation completed in {}ms",
                fastPeriod, slowPeriod, crossAbove ? "golden cross" : "death cross", duration);

        return signals;
    }

    public static SMACrossoverCondition goldenCross(int fastPeriod, int slowPeriod) {
        return new SMACrossoverCondition(fastPeriod, slowPeriod, true);
    }

    public static SMACrossoverCondition deathCross(int fastPeriod, int slowPeriod) {
        return new SMACrossoverCondition(fastPeriod, slowPeriod, false);
    }

    // Common crossover combinations
    public static SMACrossoverCondition goldenCross() {
        return goldenCross(50, 200);
    }

    public static SMACrossoverCondition deathCross() {
        return deathCross(50, 200);
    }

    public static SMACrossoverCondition shortTermGoldenCross() {
        return goldenCross(10, 20);
    }

    public static SMACrossoverCondition shortTermDeathCross() {
        return deathCross(10, 20);
    }
}