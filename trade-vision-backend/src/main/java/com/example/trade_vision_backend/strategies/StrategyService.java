package com.example.trade_vision_backend.strategies;

import com.example.trade_vision_backend.domain.BackTestRequest;
import com.example.trade_vision_backend.domain.ConditionConfig;

public interface StrategyService {
    Strategy buildStrategyFromRequest(BackTestRequest userRequest);
    Condition createConditionFromConfig(ConditionConfig config);
}
