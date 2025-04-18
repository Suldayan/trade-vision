package com.example.trade_vision_backend.strategies;

import com.example.trade_vision_backend.backtester.internal.BackTestRequest;
import com.example.trade_vision_backend.backtester.internal.ConditionConfig;
import com.example.trade_vision_backend.strategies.internal.Strategy;

public interface StrategyService {
    Strategy buildStrategyFromRequest(BackTestRequest userRequest);
    Condition createConditionFromConfig(ConditionConfig config);
}
