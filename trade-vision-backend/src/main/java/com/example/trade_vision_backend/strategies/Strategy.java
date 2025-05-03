package com.example.trade_vision_backend.strategies;

import com.example.trade_vision_backend.market.MarketData;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Strategy {
    private final List<Condition> entryConditions = new ArrayList<>();
    private final List<Condition> exitConditions = new ArrayList<>();

    @Setter
    private boolean requireAllEntryConditions = true;
    @Setter
    private boolean requireAllExitConditions = false;

    public boolean shouldEnter(MarketData data, int currentIndex) {
        if (entryConditions.isEmpty()) return false;

        if (requireAllEntryConditions) {
            return entryConditions.stream().allMatch(c -> c.evaluate(data, currentIndex));
        } else {
            return entryConditions.stream().anyMatch(c -> c.evaluate(data, currentIndex));
        }
    }

    public boolean shouldExit(MarketData data, int currentIndex) {
        if (exitConditions.isEmpty()) return false;

        if (requireAllExitConditions) {
            return exitConditions.stream().allMatch(c -> c.evaluate(data, currentIndex));
        } else {
            return exitConditions.stream().anyMatch(c -> c.evaluate(data, currentIndex));
        }
    }

    public void addEntryCondition(Condition condition) {
        entryConditions.add(condition);
    }

    public void addExitCondition(Condition condition) {
        exitConditions.add(condition);
    }
}