package com.example.trade_vision_backend.strategies;

import com.example.trade_vision_backend.market.MarketData;

public interface Condition {
    boolean evaluate(MarketData data, int currentIndex);
    boolean[] evaluateVector(MarketData data);
}
