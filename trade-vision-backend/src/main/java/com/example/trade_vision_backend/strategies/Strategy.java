package com.example.trade_vision_backend.strategies;

import com.example.trade_vision_backend.market.MarketData;
import jakarta.annotation.Nonnull;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Getter
@Slf4j
public class Strategy {
    private final List<Condition> entryConditions = new ArrayList<>();
    private final List<Condition> exitConditions = new ArrayList<>();

    @Setter
    private boolean requireAllEntryConditions = true;
    @Setter
    private boolean requireAllExitConditions = false;

    // Cache for vectorized calculations (not currently in use)
    private boolean[] entrySignalsCache;
    private boolean[] exitSignalsCache;
    private MarketData lastMarketData;

    // =============================================================================
    // BATCH PROCESSING METHODS (CURRENTLY USED IN MAIN FLOW)
    // =============================================================================

    /**
     * Pre-calculates entry signals for entire dataset using vectorized processing.
     * This is the RECOMMENDED method for backtesting as it's significantly faster
     * than calling shouldEnter() in a loop.
     *
     * @param marketData The market data to analyze
     * @return boolean array where true indicates an entry signal at that index
     */
    public boolean[] calculateEntrySignals(@Nonnull MarketData marketData) {
        log.debug("Pre-calculating entry signals for {} data points with {} conditions",
                marketData.close().length, entryConditions.size());

        if (entryConditions.isEmpty()) {
            return new boolean[marketData.close().length];
        }

        int length = marketData.close().length;
        boolean[] signals = new boolean[length];

        // Pre-calculate all conditions for entire dataset
        List<boolean[]> conditionResults = new ArrayList<>();
        for (Condition condition : entryConditions) {
            long startTime = System.currentTimeMillis();
            boolean[] conditionSignals = condition.evaluateVector(marketData);
            long duration = System.currentTimeMillis() - startTime;
            log.debug("ENTRY SIGNAL: Condition {} calculated in {}ms", condition.getClass().getSimpleName(), duration);
            conditionResults.add(conditionSignals);
        }

        // Apply AND/OR logic across all conditions
        for (int i = 0; i < length; i++) {
            if (requireAllEntryConditions) {
                // ALL conditions must be true (AND logic)
                int finalI = i;
                signals[i] = conditionResults.stream().allMatch(results -> results[finalI]);
            } else {
                // ANY condition can be true (OR logic)
                int finalI1 = i;
                signals[i] = conditionResults.stream().anyMatch(results -> results[finalI1]);
            }
        }

        int signalCount = countTrue(signals);
        log.debug("Entry signals calculated: {} signals found out of {} data points",
                signalCount, length);

        // Cache the results
        entrySignalsCache = signals;
        lastMarketData = marketData;

        return signals;
    }

    /**
     * Pre-calculates exit signals for entire dataset using vectorized processing.
     * This is the RECOMMENDED method for backtesting as it's significantly faster
     * than calling shouldExit() in a loop.
     *
     * @param marketData The market data to analyze
     * @return boolean array where true indicates an exit signal at that index
     */
    public boolean[] calculateExitSignals(@Nonnull MarketData marketData) {
        log.debug("Pre-calculating exit signals for {} data points with {} conditions",
                marketData.close().length, exitConditions.size());

        if (exitConditions.isEmpty()) {
            return new boolean[marketData.close().length];
        }

        int length = marketData.close().length;
        boolean[] signals = new boolean[length];

        // Pre-calculate all conditions for entire dataset
        List<boolean[]> conditionResults = new ArrayList<>();
        for (Condition condition : exitConditions) {
            long startTime = System.currentTimeMillis();
            boolean[] conditionSignals = condition.evaluateVector(marketData);
            long duration = System.currentTimeMillis() - startTime;
            log.debug("EXIT SIGNAL: Condition {} calculated in {}ms", condition.getClass().getSimpleName(), duration);
            conditionResults.add(conditionSignals);
        }

        // Apply AND/OR logic across all conditions
        for (int i = 0; i < length; i++) {
            if (requireAllExitConditions) {
                // ALL conditions must be true (AND logic)
                int finalI = i;
                signals[i] = conditionResults.stream().allMatch(results -> results[finalI]);
            } else {
                // ANY condition can be true (OR logic)
                int finalI1 = i;
                signals[i] = conditionResults.stream().anyMatch(results -> results[finalI1]);
            }
        }

        int signalCount = countTrue(signals);
        log.debug("Exit signals calculated: {} signals found out of {} data points",
                signalCount, length);

        // Cache the results
        exitSignalsCache = signals;
        lastMarketData = marketData;

        return signals;
    }

    // =============================================================================
    // INDIVIDUAL EVALUATION METHODS (FOR SINGLE POINT EVALUATION)
    // =============================================================================

    /**
     * Evaluates entry conditions for a single data point.
     *
     * WARNING: This method is significantly slower than calculateEntrySignals()
     * when used in loops. Use calculateEntrySignals() for backtesting instead.
     *
     * @param data The market data
     * @param currentIndex The index to evaluate
     * @return true if entry conditions are met
     */
    public boolean shouldEnter(@Nonnull MarketData data, int currentIndex) {
        if (entryConditions.isEmpty()) return false;

        if (requireAllEntryConditions) {
            return entryConditions.stream().allMatch(c -> c.evaluate(data, currentIndex));
        } else {
            return entryConditions.stream().anyMatch(c -> c.evaluate(data, currentIndex));
        }
    }

    /**
     * Evaluates exit conditions for a single data point.
     *
     * WARNING: This method is significantly slower than calculateExitSignals()
     * when used in loops. Use calculateExitSignals() for backtesting instead.
     *
     * @param data The market data
     * @param currentIndex The index to evaluate
     * @return true if exit conditions are met
     */
    public boolean shouldExit(@Nonnull MarketData data, int currentIndex) {
        if (exitConditions.isEmpty()) return false;

        if (requireAllExitConditions) {
            return exitConditions.stream().allMatch(c -> c.evaluate(data, currentIndex));
        } else {
            return exitConditions.stream().anyMatch(c -> c.evaluate(data, currentIndex));
        }
    }

    // =============================================================================
    // CACHE-AWARE METHODS (NOT CURRENTLY IMPLEMENTED IN MAIN FLOW)
    // =============================================================================

    /**
     * Evaluates entry conditions using cached results if available, otherwise
     * falls back to individual evaluation. This provides good performance
     * when batch processing has been done, but still works for ad-hoc queries.
     *
     * @param data The market data
     * @param currentIndex The index to evaluate
     * @return true if entry conditions are met
     */
    public boolean shouldEnterCached(@Nonnull MarketData data, int currentIndex) {
        // Use cached vectorized results if available
        if (entrySignalsCache != null && lastMarketData == data && currentIndex < entrySignalsCache.length) {
            return entrySignalsCache[currentIndex];
        }

        // Fall back to individual evaluation
        return shouldEnter(data, currentIndex);
    }

    /**
     * Evaluates exit conditions using cached results if available, otherwise
     * falls back to individual evaluation. This provides good performance
     * when batch processing has been done, but still works for ad-hoc queries.
     *
     * @param data The market data
     * @param currentIndex The index to evaluate
     * @return true if exit conditions are met
     */
    public boolean shouldExitCached(@Nonnull MarketData data, int currentIndex) {
        // Use cached vectorized results if available
        if (exitSignalsCache != null && lastMarketData == data && currentIndex < exitSignalsCache.length) {
            return exitSignalsCache[currentIndex];
        }

        // Fall back to individual evaluation
        return shouldExit(data, currentIndex);
    }

    // =============================================================================
    // CONDITION MANAGEMENT
    // =============================================================================

    public void addEntryCondition(@Nonnull Condition condition) {
        entryConditions.add(condition);
        // Clear cache when conditions change
        entrySignalsCache = null;
    }

    public void addExitCondition(@Nonnull Condition condition) {
        exitConditions.add(condition);
        // Clear cache when conditions change
        exitSignalsCache = null;
    }

    // =============================================================================
    // UTILITY METHODS
    // =============================================================================

    /**
     * Clears all cached signal results. Useful for testing or when you want
     * to force recalculation of signals.
     */
    public void clearCache() {
        entrySignalsCache = null;
        exitSignalsCache = null;
        lastMarketData = null;
    }

    /**
     * Checks if signals are currently cached for the given market data.
     *
     * @param marketData The market data to check
     * @return true if signals are cached for this data
     */
    public boolean hasCachedSignals(@Nonnull MarketData marketData) {
        return entrySignalsCache != null &&
                exitSignalsCache != null &&
                lastMarketData == marketData;
    }

    private int countTrue(boolean[] array) {
        int count = 0;
        for (boolean b : array) {
            if (b) count++;
        }
        return count;
    }
}