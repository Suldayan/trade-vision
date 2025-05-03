package com.example.trade_vision_backend.strategies.internal.conditions;

import com.example.trade_vision_backend.market.MarketData;
import com.example.trade_vision_backend.strategies.Condition;

public class ATRCondition implements Condition {
    @Override
    public boolean evaluate(MarketData data, int currentIndex) {
        return false;
    }
}
