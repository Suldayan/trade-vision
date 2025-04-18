package com.example.trade_vision_backend.strategies;

import com.example.trade_vision_backend.data.MarketData;

public interface Condition {
    boolean evaluate(MarketData data, int currentIndex);
}
