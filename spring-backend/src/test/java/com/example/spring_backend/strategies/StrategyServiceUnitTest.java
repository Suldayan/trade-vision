package com.example.spring_backend.strategies;

import com.example.spring_backend.common.BackTestRequest;
import com.example.spring_backend.common.ConditionConfig;
import com.example.spring_backend.market.MarketData;
import com.example.spring_backend.strategies.internal.conditions.RSICondition;
import com.example.spring_backend.strategies.internal.conditions.SMACrossoverCondition;
import com.example.spring_backend.strategies.internal.StrategyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StrategyServiceUnitTest {

    @InjectMocks
    private StrategyServiceImpl strategyService;

    private MarketData mockMarketData;

    @BeforeEach
    void setUp() {
        strategyService = new StrategyServiceImpl();
        mockMarketData = mock(MarketData.class);
    }

    @Nested
    @DisplayName("Condition Creation Tests")
    class ConditionCreationTests {

        @Test
        @DisplayName("Should create SMA Crossover condition from config")
        void createConditionFromConfig_shouldCreateSMACrossoverCondition() {
            Map<String, Object> smaParams = new HashMap<>();
            smaParams.put("fastPeriod", 5);
            smaParams.put("slowPeriod", 10);
            smaParams.put("crossAbove", true);

            ConditionConfig config = ConditionConfig.builder()
                    .type("SMA_CROSSOVER")
                    .parameters(smaParams)
                    .build();

            Condition condition = strategyService.createConditionFromConfig(config);

            assertNotNull(condition);
            assertInstanceOf(SMACrossoverCondition.class, condition);
        }

        @Test
        @DisplayName("Should create RSI Threshold condition from config")
        void createConditionFromConfig_shouldCreateRSICondition() {
            Map<String, Object> rsiParams = new HashMap<>();
            rsiParams.put("period", 14);
            rsiParams.put("upperThreshold", 30.0);
            rsiParams.put("lowerThreshold", 15.0);
            rsiParams.put("checkOverbought", true);

            ConditionConfig config = ConditionConfig.builder()
                    .type("RSI_THRESHOLD")
                    .parameters(rsiParams)
                    .build();

            Condition condition = strategyService.createConditionFromConfig(config);

            assertNotNull(condition);
            assertInstanceOf(RSICondition.class, condition);
        }

        @Test
        @DisplayName("Should throw exception for unknown condition type")
        void createConditionFromConfig_shouldThrowExceptionForUnknownType() {
            ConditionConfig config = ConditionConfig.builder()
                    .type("UNKNOWN_TYPE")
                    .parameters(new HashMap<>())
                    .build();

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> strategyService.createConditionFromConfig(config)
            );
            assertEquals("Unknown condition type: UNKNOWN_TYPE", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Strategy Building Tests")
    class StrategyBuildingTests {

        @Test
        @DisplayName("Should create empty strategy from request with no conditions")
        void buildStrategyFromRequest_shouldCreateEmptyStrategy() {
            BackTestRequest request = BackTestRequest.builder()
                    .entryConditions(new ArrayList<>())
                    .exitConditions(new ArrayList<>())
                    .requireAllEntryConditions(true)
                    .requireAllExitConditions(false)
                    .build();

            Strategy strategy = strategyService.buildStrategyFromRequest(request);

            assertNotNull(strategy);
            assertTrue(strategy.getEntryConditions().isEmpty());
            assertTrue(strategy.getExitConditions().isEmpty());
            assertTrue(strategy.isRequireAllEntryConditions());
            assertFalse(strategy.isRequireAllExitConditions());
        }

        @Test
        @DisplayName("Should create simple strategy with SMA entry and RSI exit")
        void buildStrategyFromRequest_shouldCreateSimpleStrategy() {
            Map<String, Object> smaParams = new HashMap<>();
            smaParams.put("fastPeriod", 5);
            smaParams.put("slowPeriod", 10);
            smaParams.put("crossAbove", true);

            ConditionConfig entrySMA = ConditionConfig.builder()
                    .type("SMA_CROSSOVER")
                    .parameters(smaParams)
                    .build();

            Map<String, Object> rsiParams = new HashMap<>();
            rsiParams.put("period", 14);
            rsiParams.put("upperThreshold", 70.0);
            rsiParams.put("lowerThreshold", 70.0);
            rsiParams.put("checkOverbought", false);

            ConditionConfig exitRSI = ConditionConfig.builder()
                    .type("RSI_THRESHOLD")
                    .parameters(rsiParams)
                    .build();

            BackTestRequest request = BackTestRequest.builder()
                    .entryConditions(List.of(entrySMA))
                    .exitConditions(List.of(exitRSI))
                    .requireAllEntryConditions(true)
                    .requireAllExitConditions(false)
                    .build();

            Strategy strategy = strategyService.buildStrategyFromRequest(request);

            assertNotNull(strategy);
            assertEquals(1, strategy.getEntryConditions().size());
            assertEquals(1, strategy.getExitConditions().size());
            assertInstanceOf(SMACrossoverCondition.class, strategy.getEntryConditions().getFirst());
            assertInstanceOf(RSICondition.class, strategy.getExitConditions().getFirst());
        }

        @Test
        @DisplayName("Should create complex strategy with multiple entry and exit conditions")
        void buildStrategyFromRequest_shouldCreateComplexStrategy() {
            Map<String, Object> smaEntryParams = new HashMap<>();
            smaEntryParams.put("fastPeriod", 5);
            smaEntryParams.put("slowPeriod", 10);
            smaEntryParams.put("crossAbove", true);

            ConditionConfig entrySMA = ConditionConfig.builder()
                    .type("SMA_CROSSOVER")
                    .parameters(smaEntryParams)
                    .build();

            Map<String, Object> rsiEntryParams = new HashMap<>();
            rsiEntryParams.put("period", 14);
            rsiEntryParams.put("upperThreshold", 30.0);
            rsiEntryParams.put("lowerThreshold", 20.0);
            rsiEntryParams.put("checkOverbought", true);

            ConditionConfig entryRSI = ConditionConfig.builder()
                    .type("RSI_THRESHOLD")
                    .parameters(rsiEntryParams)
                    .build();

            Map<String, Object> smaExitParams = new HashMap<>();
            smaExitParams.put("fastPeriod", 5);
            smaExitParams.put("slowPeriod", 10);
            smaExitParams.put("crossAbove", false);

            ConditionConfig exitSMA = ConditionConfig.builder()
                    .type("SMA_CROSSOVER")
                    .parameters(smaExitParams)
                    .build();

            Map<String, Object> rsiExitParams = new HashMap<>();
            rsiExitParams.put("period", 14);
            rsiExitParams.put("upperThreshold", 30.0);
            rsiExitParams.put("lowerThreshold", 20.0);
            rsiExitParams.put("checkOverbought", true);

            ConditionConfig exitRSI = ConditionConfig.builder()
                    .type("RSI_THRESHOLD")
                    .parameters(rsiExitParams)
                    .build();

            BackTestRequest request = BackTestRequest.builder()
                    .entryConditions(Arrays.asList(entrySMA, entryRSI))
                    .exitConditions(Arrays.asList(exitSMA, exitRSI))
                    .requireAllEntryConditions(true)
                    .requireAllExitConditions(false)
                    .build();

            Strategy strategy = strategyService.buildStrategyFromRequest(request);

            assertNotNull(strategy);
            assertEquals(2, strategy.getEntryConditions().size());
            assertEquals(2, strategy.getExitConditions().size());
            assertInstanceOf(SMACrossoverCondition.class, strategy.getEntryConditions().getFirst());
            assertInstanceOf(RSICondition.class, strategy.getEntryConditions().get(1));
            assertInstanceOf(SMACrossoverCondition.class, strategy.getExitConditions().getFirst());
            assertInstanceOf(RSICondition.class, strategy.getExitConditions().get(1));
            assertTrue(strategy.isRequireAllEntryConditions());
            assertFalse(strategy.isRequireAllExitConditions());
        }
    }

    @Nested
    @DisplayName("Strategy Logic Tests")
    class StrategyLogicTests {

        @Nested
        @DisplayName("Entry Condition Logic Tests")
        class EntryConditionTests {

            @Test
            @DisplayName("Should enter when all entry conditions are true and all are required")
            void shouldEnterWithAllConditionsTrue() {
                Condition mockCondition1 = mock(Condition.class);
                Condition mockCondition2 = mock(Condition.class);
                when(mockCondition1.evaluate(any(), anyInt())).thenReturn(true);
                when(mockCondition2.evaluate(any(), anyInt())).thenReturn(true);

                Strategy strategy = new Strategy();
                strategy.addEntryCondition(mockCondition1);
                strategy.addEntryCondition(mockCondition2);
                strategy.setRequireAllEntryConditions(true);

                assertTrue(strategy.shouldEnter(mockMarketData, 1));
                verify(mockCondition1).evaluate(mockMarketData, 1);
                verify(mockCondition2).evaluate(mockMarketData, 1);
            }

            @Test
            @DisplayName("Should not enter when one condition is false and all are required")
            void shouldNotEnterWithOneConditionFalse() {
                Condition mockCondition1 = mock(Condition.class);
                Condition mockCondition2 = mock(Condition.class);
                when(mockCondition1.evaluate(any(), anyInt())).thenReturn(true);
                when(mockCondition2.evaluate(any(), anyInt())).thenReturn(false);

                Strategy strategy = new Strategy();
                strategy.addEntryCondition(mockCondition1);
                strategy.addEntryCondition(mockCondition2);
                strategy.setRequireAllEntryConditions(true);

                assertFalse(strategy.shouldEnter(mockMarketData, 1));
            }

            @Test
            @DisplayName("Should enter when any condition is true and not all are required")
            void shouldEnterWithAnyConditionTrueWhenNotRequired() {
                Condition mockCondition1 = mock(Condition.class);
                Condition mockCondition2 = mock(Condition.class);
                when(mockCondition1.evaluate(any(), anyInt())).thenReturn(false);
                when(mockCondition2.evaluate(any(), anyInt())).thenReturn(true);

                Strategy strategy = new Strategy();
                strategy.addEntryCondition(mockCondition1);
                strategy.addEntryCondition(mockCondition2);
                strategy.setRequireAllEntryConditions(false);

                assertTrue(strategy.shouldEnter(mockMarketData, 1));
            }
        }

        @Nested
        @DisplayName("Exit Condition Logic Tests")
        class ExitConditionTests {

            @Test
            @DisplayName("Should exit when any condition is true and not all are required")
            void shouldExitWithAnyConditionTrue() {
                Condition mockCondition1 = mock(Condition.class);
                Condition mockCondition2 = mock(Condition.class);
                when(mockCondition1.evaluate(any(), anyInt())).thenReturn(false);
                when(mockCondition2.evaluate(any(), anyInt())).thenReturn(true);

                Strategy strategy = new Strategy();
                strategy.addExitCondition(mockCondition1);
                strategy.addExitCondition(mockCondition2);
                strategy.setRequireAllExitConditions(false);

                assertTrue(strategy.shouldExit(mockMarketData, 1));
            }

            @Test
            @DisplayName("Should not exit when all conditions are false and any is required")
            void shouldNotExitWithAllConditionsFalse() {
                Condition mockCondition1 = mock(Condition.class);
                Condition mockCondition2 = mock(Condition.class);
                when(mockCondition1.evaluate(any(), anyInt())).thenReturn(false);
                when(mockCondition2.evaluate(any(), anyInt())).thenReturn(false);

                Strategy strategy = new Strategy();
                strategy.addExitCondition(mockCondition1);
                strategy.addExitCondition(mockCondition2);
                strategy.setRequireAllExitConditions(false);

                assertFalse(strategy.shouldExit(mockMarketData, 1));
            }
        }

        @Test
        @DisplayName("Should not enter or exit when conditions lists are empty")
        void emptyConditionsLists() {
            Strategy strategy = new Strategy();

            assertFalse(strategy.shouldEnter(mockMarketData, 1));
            assertFalse(strategy.shouldExit(mockMarketData, 1));
        }
    }
}