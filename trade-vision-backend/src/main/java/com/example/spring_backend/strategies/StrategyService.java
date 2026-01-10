package com.example.spring_backend.strategies;

import com.example.spring_backend.common.BackTestRequest;
import com.example.spring_backend.common.ConditionConfig;

public interface StrategyService {
    Strategy buildStrategyFromRequest(BackTestRequest userRequest);
    Condition createConditionFromConfig(ConditionConfig config);
}
