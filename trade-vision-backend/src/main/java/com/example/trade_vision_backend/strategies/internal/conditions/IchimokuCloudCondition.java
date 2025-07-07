package com.example.trade_vision_backend.strategies.internal.conditions;

import com.example.trade_vision_backend.market.MarketData;
import com.example.trade_vision_backend.indicators.IndicatorUtils;
import com.example.trade_vision_backend.strategies.Condition;
import com.example.trade_vision_backend.strategies.internal.enums.IchimokuSignalType;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class IchimokuCloudCondition implements Condition {
    private final int tenkanPeriod;
    private final int kijunPeriod;
    private final int chikouPeriod;
    private final IchimokuSignalType signalType;

    public IchimokuCloudCondition(IchimokuSignalType signalType) {
        this(9, 26, 52, signalType);
    }

    @Override
    public boolean evaluate(@Nonnull MarketData data, int currentIndex) {
        if (currentIndex < Math.max(Math.max(tenkanPeriod, kijunPeriod), chikouPeriod)) {
            return false; // Not enough data for calculation
        }

        Map<String, double[]> ichimoku = IndicatorUtils.ichimokuCloud(
                data.high(), data.low(), data.close(),
                tenkanPeriod, kijunPeriod, chikouPeriod
        );

        double[] tenkanSen = ichimoku.get("tenkanSen");
        double[] kijunSen = ichimoku.get("kijunSen");
        double[] senkouSpanA = ichimoku.get("senkouSpanA");
        double[] senkouSpanB = ichimoku.get("senkouSpanB");
        double[] chikouSpan = ichimoku.get("chikouSpan");
        double currentPrice = data.close()[currentIndex];

        // Check for NaN values to avoid false signals
        if (Double.isNaN(tenkanSen[currentIndex]) || Double.isNaN(kijunSen[currentIndex]) ||
                Double.isNaN(senkouSpanA[currentIndex]) || Double.isNaN(senkouSpanB[currentIndex])) {
            return false;
        }

        return evaluateSignal(tenkanSen, kijunSen, senkouSpanA, senkouSpanB, chikouSpan,
                data.close(), currentIndex);
    }

    @Override
    public boolean[] evaluateVector(@Nonnull MarketData data) {
        int length = data.close().length;
        boolean[] signals = new boolean[length];

        Map<String, double[]> ichimoku = IndicatorUtils.ichimokuCloud(
                data.high(), data.low(), data.close(),
                tenkanPeriod, kijunPeriod, chikouPeriod
        );

        double[] tenkanSen = ichimoku.get("tenkanSen");
        double[] kijunSen = ichimoku.get("kijunSen");
        double[] senkouSpanA = ichimoku.get("senkouSpanA");
        double[] senkouSpanB = ichimoku.get("senkouSpanB");
        double[] chikouSpan = ichimoku.get("chikouSpan");
        double[] closePrices = data.close();

        int minRequiredIndex = Math.max(Math.max(tenkanPeriod, kijunPeriod), chikouPeriod);

        for (int i = minRequiredIndex; i < length; i++) {
            // Check for NaN values to avoid false signals
            if (Double.isNaN(tenkanSen[i]) || Double.isNaN(kijunSen[i]) ||
                    Double.isNaN(senkouSpanA[i]) || Double.isNaN(senkouSpanB[i])) {
                signals[i] = false;
                continue;
            }

            signals[i] = evaluateSignal(tenkanSen, kijunSen, senkouSpanA, senkouSpanB,
                    chikouSpan, closePrices, i);
        }

        return signals;
    }

    private boolean evaluateSignal(double[] tenkanSen, double[] kijunSen,
                                   double[] senkouSpanA, double[] senkouSpanB,
                                   double[] chikouSpan, double[] closePrices, int currentIndex) {
        double currentPrice = closePrices[currentIndex];

        final int chikouIndex = currentIndex - kijunPeriod;
        switch (signalType) {
            case TENKAN_CROSSES_ABOVE_KIJUN:
                return currentIndex > 0 &&
                        tenkanSen[currentIndex - 1] <= kijunSen[currentIndex - 1] &&
                        tenkanSen[currentIndex] > kijunSen[currentIndex];

            case TENKAN_CROSSES_BELOW_KIJUN:
                return currentIndex > 0 &&
                        tenkanSen[currentIndex - 1] >= kijunSen[currentIndex - 1] &&
                        tenkanSen[currentIndex] < kijunSen[currentIndex];

            case PRICE_ABOVE_CLOUD:
                return currentPrice > Math.max(senkouSpanA[currentIndex], senkouSpanB[currentIndex]);

            case PRICE_BELOW_CLOUD:
                return currentPrice < Math.min(senkouSpanA[currentIndex], senkouSpanB[currentIndex]);

            case PRICE_IN_CLOUD:
                double cloudTop = Math.max(senkouSpanA[currentIndex], senkouSpanB[currentIndex]);
                double cloudBottom = Math.min(senkouSpanA[currentIndex], senkouSpanB[currentIndex]);
                return currentPrice >= cloudBottom && currentPrice <= cloudTop;

            case BULLISH_CLOUD:
                return senkouSpanA[currentIndex] > senkouSpanB[currentIndex];

            case BEARISH_CLOUD:
                return senkouSpanA[currentIndex] < senkouSpanB[currentIndex];

            case CHIKOU_ABOVE_PRICE:
                return chikouIndex >= 0 && !Double.isNaN(chikouSpan[chikouIndex]) &&
                        chikouSpan[chikouIndex] > closePrices[chikouIndex];

            case CHIKOU_BELOW_PRICE:
                return chikouIndex >= 0 && !Double.isNaN(chikouSpan[chikouIndex]) &&
                        chikouSpan[chikouIndex] < closePrices[chikouIndex];

            case STRONG_BULLISH:
                return evaluateSignalForType(tenkanSen, kijunSen, senkouSpanA, senkouSpanB,
                        chikouSpan, closePrices, currentIndex, IchimokuSignalType.PRICE_ABOVE_CLOUD) &&
                        evaluateSignalForType(tenkanSen, kijunSen, senkouSpanA, senkouSpanB,
                                chikouSpan, closePrices, currentIndex, IchimokuSignalType.BULLISH_CLOUD) &&
                        evaluateSignalForType(tenkanSen, kijunSen, senkouSpanA, senkouSpanB,
                                chikouSpan, closePrices, currentIndex, IchimokuSignalType.TENKAN_CROSSES_ABOVE_KIJUN);

            case STRONG_BEARISH:
                return evaluateSignalForType(tenkanSen, kijunSen, senkouSpanA, senkouSpanB,
                        chikouSpan, closePrices, currentIndex, IchimokuSignalType.PRICE_BELOW_CLOUD) &&
                        evaluateSignalForType(tenkanSen, kijunSen, senkouSpanA, senkouSpanB,
                                chikouSpan, closePrices, currentIndex, IchimokuSignalType.BEARISH_CLOUD) &&
                        evaluateSignalForType(tenkanSen, kijunSen, senkouSpanA, senkouSpanB,
                                chikouSpan, closePrices, currentIndex, IchimokuSignalType.TENKAN_CROSSES_BELOW_KIJUN);

            default:
                return false;
        }
    }

    // Helper method for composite signals that need to evaluate multiple types
    private boolean evaluateSignalForType(double[] tenkanSen, double[] kijunSen,
                                          double[] senkouSpanA, double[] senkouSpanB,
                                          double[] chikouSpan, double[] closePrices,
                                          int currentIndex, @Nonnull IchimokuSignalType type) {
        // Temporarily store the original signal type
        IchimokuSignalType originalType = this.signalType;

        // Create a temporary condition with the requested type
        IchimokuCloudCondition tempCondition = new IchimokuCloudCondition(tenkanPeriod, kijunPeriod, chikouPeriod, type);

        // Evaluate using the shared logic
        return tempCondition.evaluateSignal(tenkanSen, kijunSen, senkouSpanA, senkouSpanB,
                chikouSpan, closePrices, currentIndex);
    }
}