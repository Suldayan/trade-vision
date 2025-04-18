package com.example.trade_vision_backend.strategies.internal;

import com.example.trade_vision_backend.backtester.internal.BackTestRequest;
import com.example.trade_vision_backend.backtester.internal.ConditionConfig;
import com.example.trade_vision_backend.indicators.internal.RSICondition;
import com.example.trade_vision_backend.indicators.internal.SMACrossoverCondition;
import com.example.trade_vision_backend.strategies.Condition;
import com.example.trade_vision_backend.strategies.StrategyService;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class StrategyServiceImpl implements StrategyService {

    @Nonnull
    @Override
    public Strategy buildStrategyFromRequest(@Nonnull BackTestRequest request) {
        Strategy strategy = new Strategy();

        // Add entry conditions
        for (ConditionConfig config : request.getEntryConditions()) {
            strategy.addEntryCondition(createConditionFromConfig(config));
        }

        // Add exit conditions
        for (ConditionConfig config : request.getExitConditions()) {
            strategy.addExitCondition(createConditionFromConfig(config));
        }

        // Set strategy behavior
        strategy.setRequireAllEntryConditions(request.isRequireAllEntryConditions());
        strategy.setRequireAllExitConditions(request.isRequireAllExitConditions());

        return strategy;
    }

    @Nonnull
    @Override
    public Condition createConditionFromConfig(@Nonnull ConditionConfig config) {
        return switch (config.getType()) {
            case "SMA_CROSSOVER" -> new SMACrossoverCondition(
                    (int) config.getParameters().get("fastPeriod"),
                    (int) config.getParameters().get("slowPeriod"),
                    (boolean) config.getParameters().get("crossAbove")
            );
            case "RSI_THRESHOLD" -> new RSICondition(
                    (int) config.getParameters().get("period"),
                    (double) config.getParameters().get("threshold"),
                    (boolean) config.getParameters().get("belowThreshold")
            );
            // Add more condition types as needed
            default -> throw new IllegalArgumentException("Unknown condition type: " + config.getType());
        };
    }
}
