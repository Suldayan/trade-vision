package com.example.trade_vision_backend.strategies.internal;

import com.example.trade_vision_backend.market.MarketData;
import com.example.trade_vision_backend.strategies.Condition;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
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
    public boolean evaluate(@Nonnull MarketData data, int currentIndex) {
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

    @Override
    public boolean[] evaluateVector(@Nonnull MarketData data) {
        int dataSize = data.close().length;
        boolean[] result = new boolean[dataSize];

        if (operator == LogicalOperator.NOT) {
            if (negatedCondition == null) {
                // If no condition to negate, return all false
                return result;
            }
            boolean[] childResult = negatedCondition.evaluateVector(data);
            for (int i = 0; i < dataSize; i++) {
                result[i] = !childResult[i];
            }
            return result;
        }

        if (conditions.isEmpty()) {
            // Empty AND is true, empty OR is false
            boolean defaultValue = operator == LogicalOperator.AND;
            Arrays.fill(result, defaultValue);
            return result;
        }

        if (operator == LogicalOperator.AND) {
            // Initialize result to all true for AND operation
            Arrays.fill(result, true);

            // AND all conditions together
            for (Condition condition : conditions) {
                boolean[] conditionResult = condition.evaluateVector(data);
                for (int i = 0; i < dataSize; i++) {
                    result[i] = result[i] && conditionResult[i];
                }
            }
            return result;
        }

        // OR all conditions together
        for (Condition condition : conditions) {
            boolean[] conditionResult = condition.evaluateVector(data);
            for (int i = 0; i < dataSize; i++) {
                result[i] = result[i] || conditionResult[i];
                // Early termination: if already true, skip remaining conditions for this index
            }
        }

        return result;
    }
}