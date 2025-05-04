package com.example.trade_vision_backend.strategies.internal.conditions;

import com.example.trade_vision_backend.market.MarketData;
import com.example.trade_vision_backend.indicators.IndicatorUtils;
import com.example.trade_vision_backend.strategies.Condition;
import com.example.trade_vision_backend.strategies.internal.enums.IchimokuSignalType;
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
    public boolean evaluate(MarketData data, int currentIndex) {
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

        // Evaluate based on the selected signal type
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
                        chikouSpan[chikouIndex] > data.close()[chikouIndex];

            case CHIKOU_BELOW_PRICE:
                return chikouIndex >= 0 && !Double.isNaN(chikouSpan[chikouIndex]) &&
                        chikouSpan[chikouIndex] < data.close()[chikouIndex];

            case STRONG_BULLISH:
                return evaluate(data, currentIndex, IchimokuSignalType.PRICE_ABOVE_CLOUD) &&
                        evaluate(data, currentIndex, IchimokuSignalType.BULLISH_CLOUD) &&
                        evaluate(data, currentIndex, IchimokuSignalType.TENKAN_CROSSES_ABOVE_KIJUN);

            case STRONG_BEARISH:
                return evaluate(data, currentIndex, IchimokuSignalType.PRICE_BELOW_CLOUD) &&
                        evaluate(data, currentIndex, IchimokuSignalType.BEARISH_CLOUD) &&
                        evaluate(data, currentIndex, IchimokuSignalType.TENKAN_CROSSES_BELOW_KIJUN);

            default:
                return false;
        }
    }

    // Helper method to evaluate other signal types within composite signals
    private boolean evaluate(MarketData data, int currentIndex, IchimokuSignalType type) {
        return new IchimokuCloudCondition(tenkanPeriod, kijunPeriod, chikouPeriod, type)
                .evaluate(data, currentIndex);
    }
}