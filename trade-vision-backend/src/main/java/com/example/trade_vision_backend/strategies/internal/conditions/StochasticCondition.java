package com.example.trade_vision_backend.strategies.internal.conditions;

import com.example.trade_vision_backend.market.MarketData;
import com.example.trade_vision_backend.indicators.IndicatorUtils;
import com.example.trade_vision_backend.strategies.Condition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@RequiredArgsConstructor
@Slf4j
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

    @Override
    public boolean[] evaluateVector(MarketData data) {
        long startTime = System.currentTimeMillis();

        double[] highs = data.high();
        double[] lows = data.low();
        double[] closes = data.close();
        int length = closes.length;
        boolean[] signals = new boolean[length];

        Map<String, double[]> stoch = IndicatorUtils.stochastic(highs, lows, closes, kPeriod, dPeriod);
        double[] k = stoch.get("%K");
        double[] d = stoch.get("%D");

        if (checkOverbought) {
            // Check for overbought condition (both %K and %D > upperThreshold)
            for (int i = 0; i < length; i++) {
                signals[i] = !Double.isNaN(k[i]) && !Double.isNaN(d[i]) &&
                        k[i] > upperThreshold && d[i] > upperThreshold;
            }
        } else {
            // Check for oversold condition (both %K and %D < lowerThreshold)
            for (int i = 0; i < length; i++) {
                signals[i] = !Double.isNaN(k[i]) && !Double.isNaN(d[i]) &&
                        k[i] < lowerThreshold && d[i] < lowerThreshold;
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        log.debug("StochasticCondition ({}/{} {}) vectorized evaluation completed in {}ms",
                kPeriod, dPeriod, checkOverbought ? "overbought" : "oversold", duration);

        return signals;
    }

    public static StochasticCondition overbought(int kPeriod, int dPeriod, double threshold) {
        return new StochasticCondition(kPeriod, dPeriod, threshold, 0, true);
    }

    public static StochasticCondition oversold(int kPeriod, int dPeriod, double threshold) {
        return new StochasticCondition(kPeriod, dPeriod, 0, threshold, false);
    }

    public static StochasticCondition overbought() {
        return overbought(14, 3, 80.0);
    }

    public static StochasticCondition oversold() {
        return oversold(14, 3, 20.0);
    }

    public static StochasticCondition fastOverbought() {
        return overbought(5, 3, 80.0);
    }

    public static StochasticCondition fastOversold() {
        return oversold(5, 3, 20.0);
    }

    public static StochasticCondition slowOverbought() {
        return overbought(21, 5, 80.0);
    }

    public static StochasticCondition slowOversold() {
        return oversold(21, 5, 20.0);
    }
}