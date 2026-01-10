package com.example.spring_backend.strategies;

import com.example.spring_backend.market.MarketData;

public interface Condition {
    boolean evaluate(MarketData data, int currentIndex);
    boolean[] evaluateVector(MarketData data);
}
