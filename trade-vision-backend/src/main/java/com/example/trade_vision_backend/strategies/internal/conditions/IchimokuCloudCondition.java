package com.example.trade_vision_backend.strategies.internal.conditions;

import com.example.trade_vision_backend.market.MarketData;
import com.example.trade_vision_backend.strategies.Condition;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class IchimokuCloudCondition implements Condition {
    @Override
    public boolean evaluate(MarketData data, int currentIndex) {
        return false;
    }
}
