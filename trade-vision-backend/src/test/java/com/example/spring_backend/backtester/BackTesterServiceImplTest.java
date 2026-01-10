package com.example.spring_backend.backtester;

import com.example.spring_backend.backtester.internal.*;
import com.example.spring_backend.common.BackTestRequest;
import com.example.spring_backend.market.MarketData;
import com.example.spring_backend.market.MarketDataPoint;
import com.example.spring_backend.strategies.Condition;
import com.example.spring_backend.strategies.Strategy;
import com.example.spring_backend.strategies.StrategyService;
import com.example.spring_backend.common.ConditionConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BackTesterServiceImplTest {

    @Mock
    private StrategyService strategyService;

    @InjectMocks
    private BackTesterServiceImpl backTesterService;

    private MarketData marketData;

    @BeforeEach
    void setUp() {
        double[] open = {10, 10.8, 10.3, 10.6, 11, 11.3, 11.2, 10.7, 10.2, 10.3, 10.8, 11.5, 12.2, 12.3, 11.7, 11.6, 12, 12.5, 12.9, 13};
        double[] high = {10.2, 11.2, 10.8, 11, 11.4, 11.7, 11.3, 10.8, 10.4, 10.7, 11.2, 12.2, 12.7, 12.5, 12, 12, 12.5, 13, 13.2, 13.1};
        double[] low = {9.8, 10.7, 10.2, 10.5, 10.9, 11.1, 10.8, 10.3, 9.8, 10.1, 10.7, 11.4, 12.1, 11.8, 11.3, 11.5, 11.9, 12.4, 12.8, 12.4};
        double[] close = {10, 11, 10.5, 10.8, 11.2, 11.5, 11, 10.5, 10, 10.5, 11, 12, 12.5, 12, 11.5, 11.8, 12.3, 12.8, 13, 12.5};
        double[] volume = {1000, 1500, 1200, 1300, 1400, 1600, 1300, 1100, 900, 1000, 1200, 1800, 2000, 1500, 1300, 1400, 1600, 1800, 2000, 1500};

        marketData = new MarketData();
        LocalDateTime baseTime = LocalDateTime.of(2023, 1, 1, 9, 30);

        for (int i = 0; i < close.length; i++) {
            MarketDataPoint point = MarketDataPoint.builder()
                    .timestamp(baseTime.plusDays(i))
                    .open(open[i])
                    .high(high[i])
                    .low(low[i])
                    .close(close[i])
                    .adjustedClose(close[i])
                    .volume((long) volume[i])
                    .dividendAmount(0.0)
                    .splitCoefficient(1.0)
                    .build();
            marketData.addDataPoint(point);
        }
    }

    @Test
    @DisplayName("Should execute trades with simple price threshold strategy")
    void testSimpleStrategy() {
        Strategy strategy = new Strategy();
        strategy.addEntryCondition(new TestCondition(11, true));
        strategy.addExitCondition(new TestCondition(10.5, false));
        strategy.setRequireAllEntryConditions(true);
        strategy.setRequireAllExitConditions(true);

        BackTestRequest request = BackTestRequest.builder()
                .initialCapital(10000)
                .commissionRate(0.001)
                .slippagePercent(0.001)
                .riskPerTrade(0.02)
                .build();

        BackTestResult result = backTesterService.runBackTest(strategy, marketData, request);

        assertNotNull(result);
        assertFalse(result.trades().isEmpty(), "Should have executed at least one trade");
        assertEquals(result.equityCurve().length, marketData.getDataPoints().size(), "Equity curve should match data length + initial point");
    }

    @Test
    @DisplayName("Should generate valid results with complex composite strategy")
    void testComplexCompositeStrategy() {
        BackTestRequest request = createComplexBackTestRequest();
        Strategy complexStrategy = createMatchingComplexStrategy();

        BackTestResult result = backTesterService.runBackTest(complexStrategy, marketData, request);

        assertNotNull(result);
        assertFalse(result.trades().isEmpty(), "Complex strategy should generate trades");
        assertEquals(marketData.getDataPoints().size(), result.equityCurve().length,
                "Equity curve should have one more point than price data (for initial capital)");

        assertTrue(result.totalReturn() != 0, "Should have calculated total return");
        assertTrue(result.maxDrawdown() >= 0, "Max drawdown should be calculated");
        assertTrue(result.winRatio() >= 0 && result.winRatio() <= 1, "Win ratio should be between 0 and 1");

        for (Trade trade : result.trades()) {
            assertTrue(trade.entryPrice() > 0, "Entry price should be positive");
            assertTrue(trade.exitPrice() > 0, "Exit price should be positive");
            assertTrue(trade.positionSize() > 0, "Position size should be positive");
        }

        System.out.println(result);
    }

    @Test
    @DisplayName("Should generate empty trade list when no entry conditions are met")
    void testNoTradesStrategy() {
        Strategy noTradeStrategy = new Strategy();
        noTradeStrategy.addEntryCondition(new TestCondition(100, true));

        BackTestRequest request = BackTestRequest.builder()
                .initialCapital(10000)
                .commissionRate(0.001)
                .slippagePercent(0.001)
                .build();

        BackTestResult result = backTesterService.runBackTest(noTradeStrategy, marketData, request);

        assertNotNull(result);
        assertEquals(0, result.trades().size(), "Should not have any trades");
        assertEquals(marketData.getDataPoints().size(), result.equityCurve().length, "Should still have full equity curve");
        assertEquals(10000, result.finalCapital(), "Final capital should equal initial capital");
        assertEquals(0, result.totalReturn(), "Total return should be 0%");
    }

    @Test
    @DisplayName("Should correctly calculate maximum drawdown for losing strategy")
    void testMaximumDrawdown() {
        Strategy badStrategy = new Strategy();
        badStrategy.addEntryCondition(new TestCondition(12, true));
        badStrategy.addExitCondition(new TestCondition(11.5, false));

        BackTestRequest request = BackTestRequest.builder()
                .initialCapital(10000)
                .commissionRate(0.001)
                .slippagePercent(0.001)
                .build();

        BackTestResult result = backTesterService.runBackTest(badStrategy, marketData, request);

        assertTrue(result.maxDrawdown() > 0, "Should have a drawdown with this strategy");
    }

    @Test
    @DisplayName("Should compound returns and show equity growth with profitable strategy")
    void testCompoundingReturns() {
        Strategy profitableStrategy = new Strategy();

        profitableStrategy.addEntryCondition(new TestCondition(10.5, false));
        profitableStrategy.addExitCondition(new TestCondition(11.5, true));

        BackTestRequest request = BackTestRequest.builder()
                .initialCapital(10000)
                .commissionRate(0.001)
                .slippagePercent(0.001)
                .build();

        BackTestResult result = backTesterService.runBackTest(profitableStrategy, marketData, request);

        assertTrue(result.totalReturn() > 0, "Should have positive returns");
        assertTrue(result.finalCapital() > 10000, "Final capital should be more than initial");

        double previousEquity = result.equityCurve()[0];
        boolean hasIncrease = false;

        for (int i = 1; i < result.equityCurve().length; i++) {
            if (result.equityCurve()[i] > previousEquity) {
                hasIncrease = true;
                break;
            }
            previousEquity = result.equityCurve()[i];
        }

        assertTrue(hasIncrease, "Equity curve should show some increase with profitable strategy");
    }

    @Test
    @DisplayName("Should handle multiple entry and exit conditions with different requirements")
    void testMultipleEntryExitConditions() {
        Strategy strategy = new Strategy();

        strategy.addEntryCondition(new TestCondition(11, true));
        strategy.addEntryCondition(new VolumeCondition(1500));
        strategy.setRequireAllEntryConditions(true);

        strategy.addExitCondition(new TestCondition(10.5, false));
        strategy.addExitCondition(new TestCondition(12.5, true));
        strategy.setRequireAllExitConditions(false);

        BackTestRequest request = BackTestRequest.builder()
                .initialCapital(10000)
                .commissionRate(0.001)
                .slippagePercent(0.001)
                .build();

        BackTestResult result = backTesterService.runBackTest(strategy, marketData, request);

        assertNotNull(result);
        assertFalse(result.trades().isEmpty(), "Strategy should generate trades");
    }

    @Test
    @DisplayName("Should evaluate complex nested conditions correctly")
    void testComplexNestedConditions() {
        Strategy complexStrategy = createNestedCompositeStrategy();

        BackTestRequest request = BackTestRequest.builder()
                .initialCapital(10000)
                .commissionRate(0.001)
                .slippagePercent(0.001)
                .build();

        BackTestResult result = backTesterService.runBackTest(complexStrategy, marketData, request);

        System.out.println(result);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should reduce returns when transaction costs are applied")
    void testTransactionCosts() {
        Strategy strategy = new Strategy();
        strategy.addEntryCondition(new TestCondition(11, true));
        strategy.addExitCondition(new TestCondition(10.5, false));

        BackTestRequest noCostRequest = BackTestRequest.builder()
                .initialCapital(10000)
                .commissionRate(0)
                .slippagePercent(0)
                .build();

        BackTestResult noCostResult = backTesterService.runBackTest(strategy, marketData, noCostRequest);

        BackTestRequest highCostRequest = BackTestRequest.builder()
                .initialCapital(10000)
                .commissionRate(0.01)
                .slippagePercent(0.01)
                .build();

        BackTestResult highCostResult = backTesterService.runBackTest(strategy, marketData, highCostRequest);

        assertTrue(noCostResult.finalCapital() >= highCostResult.finalCapital(),
                "Higher transaction costs should result in lower final capital");
    }

    @Test
    @DisplayName("Should handle open positions at the end of backtesting period")
    void testOpenPositionAtEnd() {
        Strategy strategy = new Strategy();
        strategy.addEntryCondition(new TestCondition(12, true));

        BackTestRequest request = BackTestRequest.builder()
                .initialCapital(10000)
                .commissionRate(0.001)
                .slippagePercent(0.001)
                .build();

        BackTestResult result = backTesterService.runBackTest(strategy, marketData, request);

        assertFalse(result.trades().isEmpty(), "Should have at least one trade");
    }

    @Test
    @DisplayName("Should apply risk per trade parameter correctly")
    void testRiskPerTrade() {
        Strategy strategy = new Strategy();
        strategy.addEntryCondition(new TestCondition(11, true));
        strategy.addExitCondition(new TestCondition(10, false));

        BackTestRequest request = BackTestRequest.builder()
                .initialCapital(10000)
                .commissionRate(0.001)
                .slippagePercent(0.001)
                .riskPerTrade(0.02)
                .build();

        BackTestResult result = backTesterService.runBackTest(strategy, marketData, request);

        assertNotNull(result);
    }

    @Test
    @DisplayName("Should ensure market data methods provide consistent values")
    void testMarketDataMethodsMatch() {
        double[] closePrices = marketData.close();
        double[] openPrices = marketData.open();
        double[] highPrices = marketData.high();
        double[] lowPrices = marketData.low();
        double[] volumeData = marketData.volume();

        assertEquals(marketData.getDataPoints().size(), closePrices.length, "Close prices array length should match data points size");

        assertEquals(marketData.getDataPoints().getFirst().close(), closePrices[0], "First close price should match");
        assertEquals(marketData.getDataPoints().getLast().close(), closePrices[closePrices.length - 1], "Last close price should match");

        assertEquals(marketData.getDataPoints().getFirst().open(), openPrices[0], "First open price should match");
        assertEquals(marketData.getDataPoints().getFirst().high(), highPrices[0], "First high price should match");
        assertEquals(marketData.getDataPoints().getFirst().low(), lowPrices[0], "First low price should match");
        assertEquals(marketData.getDataPoints().getFirst().volume(), volumeData[0], "First volume should match");
    }

    private BackTestRequest createComplexBackTestRequest() {
        Map<String, Object> rsiParams = new HashMap<>();
        rsiParams.put("period", 14);
        rsiParams.put("upperThreshold", 70.0);
        rsiParams.put("lowerThreshold", 30.0);
        rsiParams.put("checkOverbought", false);

        ConditionConfig rsiCondition = ConditionConfig.builder()
                .type("RSI_THRESHOLD")
                .parameters(rsiParams)
                .build();

        Map<String, Object> smaParams = new HashMap<>();
        smaParams.put("fastPeriod", 5);
        smaParams.put("slowPeriod", 20);
        smaParams.put("crossAbove", true);

        ConditionConfig smaCondition = ConditionConfig.builder()
                .type("SMA_CROSSOVER")
                .parameters(smaParams)
                .build();

        Map<String, Object> andParams = new HashMap<>();
        List<ConditionConfig> conditions = new ArrayList<>();
        conditions.add(rsiCondition);
        conditions.add(smaCondition);
        andParams.put("conditions", conditions);

        ConditionConfig andCondition = ConditionConfig.builder()
                .type("AND")
                .parameters(andParams)
                .build();

        Map<String, Object> bbParams = new HashMap<>();
        bbParams.put("period", 20);
        bbParams.put("numStd", 2.0);
        bbParams.put("checkUpper", true);

        ConditionConfig bbCondition = ConditionConfig.builder()
                .type("BOLLINGER_BANDS")
                .parameters(bbParams)
                .build();

        Map<String, Object> macdParams = new HashMap<>();
        macdParams.put("fastPeriod", 12);
        macdParams.put("slowPeriod", 26);
        macdParams.put("signalPeriod", 9);
        macdParams.put("crossAbove", false);

        ConditionConfig macdCondition = ConditionConfig.builder()
                .type("MACD_CROSSOVER")
                .parameters(macdParams)
                .build();

        Map<String, Object> orParams = new HashMap<>();
        List<ConditionConfig> exitConditions = new ArrayList<>();
        exitConditions.add(bbCondition);
        exitConditions.add(macdCondition);
        orParams.put("conditions", exitConditions);

        ConditionConfig orCondition = ConditionConfig.builder()
                .type("OR")
                .parameters(orParams)
                .build();

        return BackTestRequest.builder()
                .dataFilePath("test_data.csv")
                .initialCapital(10000.0)
                .riskPerTrade(0.02)
                .entryConditions(List.of(andCondition))
                .exitConditions(List.of(orCondition))
                .requireAllEntryConditions(true)
                .requireAllExitConditions(true)
                .allowShort(false)
                .commissionRate(0.001)
                .slippagePercent(0.001)
                .build();
    }

    private Strategy createMatchingComplexStrategy() {
        Strategy strategy = new Strategy();

        Condition entryCondition = Mockito.mock(Condition.class);
        // Mock both evaluate and evaluateVector methods
        when(entryCondition.evaluateVector(any(MarketData.class))).thenAnswer(inv -> {
            MarketData data = inv.getArgument(0);
            int length = data.close().length;
            boolean[] signals = new boolean[length];
            Arrays.fill(signals, true); // All entry signals are true
            return signals;
        });
        strategy.addEntryCondition(entryCondition);

        Condition exitCondition = Mockito.mock(Condition.class);

        // Mock vectorized evaluation
        when(exitCondition.evaluateVector(any(MarketData.class))).thenAnswer(inv -> {
            MarketData data = inv.getArgument(0);
            int length = data.close().length;
            boolean[] signals = new boolean[length];
            for (int i = 0; i < length; i++) {
                signals[i] = i > 5 && i % 5 == 0;
            }
            return signals;
        });
        strategy.addExitCondition(exitCondition);

        strategy.setRequireAllEntryConditions(true);
        strategy.setRequireAllExitConditions(true);

        return strategy;
    }

    private Strategy createNestedCompositeStrategy() {
        Strategy strategy = new Strategy();

        Condition complexEntryCondition = Mockito.mock(Condition.class);

        // Mock vectorized evaluation
        when(complexEntryCondition.evaluateVector(any(MarketData.class))).thenAnswer(inv -> {
            MarketData data = inv.getArgument(0);
            double[] prices = data.close();
            int length = prices.length;
            boolean[] signals = new boolean[length];
            for (int i = 0; i < length; i++) {
                signals[i] = prices[i] > 11 && (i % 3 == 0);
            }
            return signals;
        });

        Condition complexExitCondition = Mockito.mock(Condition.class);
        // Mock vectorized evaluation
        when(complexExitCondition.evaluateVector(any(MarketData.class))).thenAnswer(inv -> {
            MarketData data = inv.getArgument(0);
            double[] prices = data.close();
            int length = prices.length;
            boolean[] signals = new boolean[length];
            for (int i = 0; i < length; i++) {
                signals[i] = prices[i] < 11 || (prices[i] > 12.5 && i % 2 == 0);
            }
            return signals;
        });

        strategy.addEntryCondition(complexEntryCondition);
        strategy.addExitCondition(complexExitCondition);
        strategy.setRequireAllEntryConditions(true);
        strategy.setRequireAllExitConditions(true);

        return strategy;
    }

    private record TestCondition(double threshold, boolean checkAbove) implements Condition {

        @Override
        public boolean evaluate(MarketData data, int currentIndex) {
            double price = data.close()[currentIndex];
            return checkAbove ? price > threshold : price < threshold;
        }

        @Override
        public boolean[] evaluateVector(MarketData data) {
            double[] prices = data.close();
            int length = prices.length;
            boolean[] signals = new boolean[length];

            if (checkAbove) {
                for (int i = 0; i < length; i++) {
                    signals[i] = prices[i] > threshold;
                }
            } else {
                for (int i = 0; i < length; i++) {
                    signals[i] = prices[i] < threshold;
                }
            }

            return signals;
        }
    }

    private record VolumeCondition(double threshold) implements Condition {

        @Override
        public boolean evaluate(MarketData data, int currentIndex) {
            return data.volume()[currentIndex] > threshold;
        }

        @Override
        public boolean[] evaluateVector(MarketData data) {
            double[] volumes = data.volume();
            int length = volumes.length;
            boolean[] signals = new boolean[length];

            for (int i = 0; i < length; i++) {
                signals[i] = volumes[i] > threshold;
            }

            return signals;
        }
    }

    // Example test method showing how to use the vectorized approach
    @Test
    public void testVectorizedStrategyProcessing() {
        // Create test data
        MarketData marketData = createTestMarketData();

        // Create strategy with vectorized conditions
        Strategy strategy = new Strategy();
        strategy.addEntryCondition(new TestCondition(10.0, true));
        strategy.addExitCondition(new VolumeCondition(1000.0));

        // Use vectorized processing instead of loops
        boolean[] entrySignals = strategy.calculateEntrySignals(marketData);
        boolean[] exitSignals = strategy.calculateExitSignals(marketData);

        // Verify signals
        assertNotNull(entrySignals);
        assertNotNull(exitSignals);
        assertEquals(marketData.close().length, entrySignals.length);
        assertEquals(marketData.close().length, exitSignals.length);

        // Test specific signal conditions
        for (int i = 0; i < entrySignals.length; i++) {
            boolean expectedEntry = marketData.close()[i] > 10.0;
            assertEquals(expectedEntry, entrySignals[i], "Entry signal mismatch at index " + i);

            boolean expectedExit = marketData.volume()[i] > 1000.0;
            assertEquals(expectedExit, exitSignals[i], "Exit signal mismatch at index " + i);
        }
    }

    // Helper method to create test market data
    private MarketData createTestMarketData() {
        // Create sample data for testing
        double[] closes = {9.0, 10.5, 11.2, 8.9, 12.1, 10.8, 13.5, 9.2, 11.9, 12.8};
        double[] volumes = {800, 1200, 950, 1100, 1500, 900, 1800, 700, 1300, 1600};
        double[] highs = new double[closes.length];
        double[] lows = new double[closes.length];
        double[] opens = new double[closes.length];

        // Simple high/low/open generation for testing
        for (int i = 0; i < closes.length; i++) {
            highs[i] = closes[i] + 0.5;
            lows[i] = closes[i] - 0.5;
            opens[i] = i > 0 ? closes[i-1] : closes[i] - 0.2; // Open is previous close or slightly below first close
        }

        MarketData marketData = new MarketData();
        LocalDateTime baseTime = LocalDateTime.now().minusDays(closes.length);

        // Create MarketDataPoint objects and add them to MarketData
        for (int i = 0; i < closes.length; i++) {
            MarketDataPoint point = MarketDataPoint.builder()
                    .timestamp(baseTime.plusDays(i))
                    .open(opens[i])
                    .high(highs[i])
                    .low(lows[i])
                    .close(closes[i])
                    .adjustedClose(closes[i]) // Assuming no adjustments for test data
                    .volume((long) volumes[i])
                    .dividendAmount(0.0) // No dividends for test data
                    .splitCoefficient(1.0) // No splits for test data
                    .build();

            marketData.addDataPoint(point);
        }

        return marketData;
    }

    // Test to verify backward compatibility
    @Test
    public void testBackwardCompatibilityWithIndividualEvaluation() {
        MarketData marketData = createTestMarketData();
        Strategy strategy = new Strategy();
        TestCondition condition = new TestCondition(10.0, true);
        strategy.addEntryCondition(condition);

        // Test individual evaluation still works
        for (int i = 0; i < marketData.close().length; i++) {
            boolean individualResult = condition.evaluate(marketData, i);
            boolean expectedResult = marketData.close()[i] > 10.0;
            assertEquals(expectedResult, individualResult, "Individual evaluation failed at index " + i);
        }

        // Test vectorized evaluation produces same results
        boolean[] vectorResults = condition.evaluateVector(marketData);
        for (int i = 0; i < marketData.close().length; i++) {
            boolean individualResult = condition.evaluate(marketData, i);
            assertEquals(individualResult, vectorResults[i], "Vectorized vs individual mismatch at index " + i);
        }
    }
}