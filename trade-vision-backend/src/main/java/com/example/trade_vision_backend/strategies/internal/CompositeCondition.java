package com.example.trade_vision_backend.strategies.internal;

import com.example.trade_vision_backend.market.MarketData;
import com.example.trade_vision_backend.strategies.Condition;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class CompositeCondition implements Condition {

    public enum LogicalOperator {
        AND,  // All conditions must be true
        OR,   // Any condition must be true
        NOT   // Negate the result of the child condition
    }

    private final List<Condition> conditions = new ArrayList<>();
    private final LogicalOperator operator;
    private Condition negatedCondition; // Used only for NOT operator

    public CompositeCondition(Condition condition) {
        this.operator = LogicalOperator.NOT;
        this.negatedCondition = condition;
    }

    public void addCondition(Condition condition) {
        if (operator == LogicalOperator.NOT) {
            throw new IllegalStateException("Cannot add conditions to a NOT operator");
        }
        conditions.add(condition);
    }

    @Override
    public boolean evaluate(MarketData data, int currentIndex) {
        if (operator == LogicalOperator.NOT) {
            return !negatedCondition.evaluate(data, currentIndex);
        }
        if (conditions.isEmpty()) {
            return operator == LogicalOperator.AND; // Empty AND is true, empty OR is false
        }
        if (operator == LogicalOperator.AND) {
            return conditions.stream().allMatch(c -> c.evaluate(data, currentIndex));
        }
        return conditions.stream().anyMatch(c -> c.evaluate(data, currentIndex));
    }
}