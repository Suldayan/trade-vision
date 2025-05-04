package com.example.trade_vision_backend.strategies.internal.enums;

public enum IchimokuSignalType {
    TENKAN_CROSSES_ABOVE_KIJUN,   // Bullish trend signal (Tenkan crosses above Kijun)
    TENKAN_CROSSES_BELOW_KIJUN,   // Bearish trend signal (Tenkan crosses below Kijun)
    PRICE_ABOVE_CLOUD,            // Price is above the cloud (bullish)
    PRICE_BELOW_CLOUD,            // Price is below the cloud (bearish)
    PRICE_IN_CLOUD,               // Price is inside the cloud (indecision)
    BULLISH_CLOUD,                // Senkou Span A is above Senkou Span B (bullish future)
    BEARISH_CLOUD,                // Senkou Span A is below Senkou Span B (bearish future)
    CHIKOU_ABOVE_PRICE,           // Chikou Span is above price from 26 periods ago (bullish)
    CHIKOU_BELOW_PRICE,           // Chikou Span is below price from 26 periods ago (bearish)
    STRONG_BULLISH,               // Composite of multiple bullish signals
    STRONG_BEARISH                // Composite of multiple bearish signals
}