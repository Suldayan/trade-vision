package com.example.trade_vision_backend.strategies.internal.conditions;

import com.example.trade_vision_backend.market.MarketData;
import com.example.trade_vision_backend.indicators.IndicatorUtils;
import com.example.trade_vision_backend.strategies.Condition;
import com.example.trade_vision_backend.strategies.internal.enums.DMISignalType;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class DMICondition implements Condition {
    private final int period;
    private final DMISignalType signalType;
    private final double threshold;
    private final double divergenceThreshold;

    public DMICondition(DMISignalType signalType) {
        this(14, signalType, 25.0, 10.0);
    }

    public DMICondition(int period, DMISignalType signalType) {
        this(period, signalType, 25.0, 10.0);
    }

    public DMICondition(int period, DMISignalType signalType, double threshold) {
        this(period, signalType, threshold, 10.0);
    }

    @Override
    public boolean evaluate(MarketData data, int currentIndex) {
        // Check if we have enough data for calculation (ADX needs 2*period-1 bars)
        if (currentIndex < 2 * period - 1) {
            return false;
        }

        Map<String, double[]> dmiResult = IndicatorUtils.dmi(
                data.high(), data.low(), data.close(), period
        );

        // Validate map contains all required keys
        if (!dmiResult.containsKey("plusDI") || !dmiResult.containsKey("minusDI") || !dmiResult.containsKey("ADX")) {
            return false;
        }

        double[] plusDI = dmiResult.get("plusDI");
        double[] minusDI = dmiResult.get("minusDI");
        double[] adx = dmiResult.get("ADX");

        // Check for NaN values to avoid false signals
        if (Double.isNaN(plusDI[currentIndex]) || Double.isNaN(minusDI[currentIndex]) ||
                Double.isNaN(adx[currentIndex])) {
            return false;
        }

        // Handle signal types that require historical comparison
        if (requiresHistoricalComparison(signalType) && currentIndex == 0) {
            return false;
        }

        // Evaluate based on the selected signal type
        return switch (signalType) {
            case PLUS_DI_ABOVE_MINUS_DI -> plusDI[currentIndex] > minusDI[currentIndex];
            case MINUS_DI_ABOVE_PLUS_DI -> minusDI[currentIndex] > plusDI[currentIndex];

            case PLUS_DI_CROSSES_ABOVE_MINUS_DI -> plusDI[currentIndex - 1] <= minusDI[currentIndex - 1] &&
                    plusDI[currentIndex] > minusDI[currentIndex];

            case MINUS_DI_CROSSES_ABOVE_PLUS_DI -> minusDI[currentIndex - 1] <= plusDI[currentIndex - 1] &&
                    minusDI[currentIndex] > plusDI[currentIndex];

            case ADX_ABOVE_THRESHOLD -> adx[currentIndex] > threshold;
            case ADX_BELOW_THRESHOLD -> adx[currentIndex] < threshold;

            case WEAK_TREND -> adx[currentIndex] < threshold &&
                    Math.abs(plusDI[currentIndex] - minusDI[currentIndex]) < divergenceThreshold;

            case ADX_RISING -> adx[currentIndex] > adx[currentIndex - 1];
            case ADX_FALLING -> adx[currentIndex] < adx[currentIndex - 1];

            case STRONG_TREND -> adx[currentIndex] > threshold &&
                    Math.abs(plusDI[currentIndex] - minusDI[currentIndex]) > divergenceThreshold;

            case STRONG_BULLISH -> adx[currentIndex] > threshold &&
                    plusDI[currentIndex] > minusDI[currentIndex] &&
                    (plusDI[currentIndex] - minusDI[currentIndex]) > divergenceThreshold;

            case STRONG_BEARISH -> adx[currentIndex] > threshold &&
                    minusDI[currentIndex] > plusDI[currentIndex] &&
                    (minusDI[currentIndex] - plusDI[currentIndex]) > divergenceThreshold;

            case DI_DIVERGENCE -> currentIndex > period &&
                    Math.abs(plusDI[currentIndex] - minusDI[currentIndex]) >
                            Math.abs(plusDI[currentIndex - period] - minusDI[currentIndex - period]);
        };
    }

    private boolean requiresHistoricalComparison(DMISignalType signalType) {
        return signalType == DMISignalType.PLUS_DI_CROSSES_ABOVE_MINUS_DI ||
                signalType == DMISignalType.MINUS_DI_CROSSES_ABOVE_PLUS_DI ||
                signalType == DMISignalType.ADX_RISING ||
                signalType == DMISignalType.ADX_FALLING ||
                signalType == DMISignalType.DI_DIVERGENCE;
    }
}