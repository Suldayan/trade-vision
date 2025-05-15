package com.example.trade_vision_backend.strategies;

import com.example.trade_vision_backend.common.BackTestRequest;
import com.example.trade_vision_backend.common.ConditionConfig;

public interface StrategyService {
    Strategy buildStrategyFromRequest(BackTestRequest userRequest);
    Condition createConditionFromConfig(ConditionConfig config);
}
