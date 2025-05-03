package com.example.trade_vision_backend.backtester;

import com.example.trade_vision_backend.backtester.internal.*;
import com.example.trade_vision_backend.domain.BackTestRequest;
import com.example.trade_vision_backend.market.MarketData;
import com.example.trade_vision_backend.market.MarketDataPoint;
import com.example.trade_vision_backend.strategies.Condition;
import com.example.trade_vision_backend.strategies.Strategy;
import com.example.trade_vision_backend.strategies.StrategyService;
import com.example.trade_vision_backend.domain.ConditionConfig;
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
import static org.mockito.ArgumentMatchers.anyInt;
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
        assertEquals(result.equityCurve().size(), marketData.getDataPoints().size(), "Equity curve should match data length + initial point");
    }

    @Test
    @DisplayName("Should generate valid results with complex composite strategy")
    void testComplexCompositeStrategy() {
        BackTestRequest request = createComplexBackTestRequest();
        Strategy complexStrategy = createMatchingComplexStrategy();

        BackTestResult result = backTesterService.runBackTest(complexStrategy, marketData, request);

        assertNotNull(result);
        assertFalse(result.trades().isEmpty(), "Complex strategy should generate trades");
        assertEquals(marketData.getDataPoints().size(), result.equityCurve().size(),
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
        assertEquals(marketData.getDataPoints().size(), result.equityCurve().size(), "Should still have full equity curve");
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

        double previousEquity = result.equityCurve().getFirst();
        boolean hasIncrease = false;

        for (int i = 1; i < result.equityCurve().size(); i++) {
            if (result.equityCurve().get(i) > previousEquity) {
                hasIncrease = true;
                break;
            }
            previousEquity = result.equityCurve().get(i);
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
        when(entryCondition.evaluate(any(MarketData.class), anyInt())).thenReturn(true);
        strategy.addEntryCondition(entryCondition);

        Condition exitCondition = Mockito.mock(Condition.class);
        when(exitCondition.evaluate(any(MarketData.class), anyInt())).thenAnswer(inv -> {
            int index = inv.getArgument(1);
            return index > 5 && index % 5 == 0;
        });
        strategy.addExitCondition(exitCondition);

        strategy.setRequireAllEntryConditions(true);
        strategy.setRequireAllExitConditions(true);

        return strategy;
    }

    private Strategy createNestedCompositeStrategy() {
        Strategy strategy = new Strategy();

        Condition complexEntryCondition = Mockito.mock(Condition.class);
        when(complexEntryCondition.evaluate(any(MarketData.class), anyInt())).thenAnswer(inv -> {
            int index = inv.getArgument(1);
            MarketData data = inv.getArgument(0);
            double price = data.close()[index];
            return price > 11 && (index % 3 == 0);
        });

        Condition complexExitCondition = Mockito.mock(Condition.class);
        when(complexExitCondition.evaluate(any(MarketData.class), anyInt())).thenAnswer(inv -> {
            int index = inv.getArgument(1);
            MarketData data = inv.getArgument(0);
            double price = data.close()[index];
            return price < 11 || (price > 12.5 && index % 2 == 0);
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
    }

    private record VolumeCondition(double threshold) implements Condition {

        @Override
        public boolean evaluate(MarketData data, int currentIndex) {
            return data.volume()[currentIndex] > threshold;
        }
    }
}