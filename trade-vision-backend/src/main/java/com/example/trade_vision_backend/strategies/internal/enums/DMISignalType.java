package com.example.trade_vision_backend.strategies.internal.enums;

public enum DMISignalType {
    PLUS_DI_ABOVE_MINUS_DI,         // +DI > -DI (bullish)
    MINUS_DI_ABOVE_PLUS_DI,         // -DI > +DI (bearish)
    PLUS_DI_CROSSES_ABOVE_MINUS_DI, // +DI crosses above -DI (bullish signal)
    MINUS_DI_CROSSES_ABOVE_PLUS_DI, // -DI crosses above +DI (bearish signal)
    ADX_ABOVE_THRESHOLD,            // ADX > threshold (strong trend)
    ADX_BELOW_THRESHOLD,            // ADX < threshold (weak trend)
    ADX_RISING,                     // ADX is rising (strengthening trend)
    ADX_FALLING,                    // ADX is falling (weakening trend)
    STRONG_TREND,                   // ADX > threshold and large DI separation
    STRONG_BULLISH,                 // ADX > threshold, +DI > -DI with large separation
    STRONG_BEARISH,                 // ADX > threshold, -DI > +DI with large separation
    WEAK_TREND,                     // ADX < threshold (range-bound market)
    DI_DIVERGENCE                   // Increasing separation between +DI and -DI
}
