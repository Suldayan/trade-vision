package com.example.trade_vision_backend.backtester;

import com.example.trade_vision_backend.backtester.internal.BackTestResult;
import com.example.trade_vision_backend.backtester.internal.Trade;
import com.example.trade_vision_backend.common.BackTestRequest;
import com.example.trade_vision_backend.common.ConditionConfig;
import com.example.trade_vision_backend.market.CsvImporterService;
import com.example.trade_vision_backend.market.MarketData;
import com.example.trade_vision_backend.strategies.Strategy;
import com.example.trade_vision_backend.strategies.StrategyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(BackTesterControllerIntegrationTest.TestConfig.class)
public class BackTesterControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CsvImporterService csvImporterService;

    @Autowired
    private StrategyService strategyService;

    @Autowired
    private BackTesterService backTesterService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public CsvImporterService csvImporterService() {
            return Mockito.mock(CsvImporterService.class);
        }

        @Bean
        @Primary
        public StrategyService strategyService() {
            return Mockito.mock(StrategyService.class);
        }

        @Bean
        @Primary
        public BackTesterService backTesterService() {
            return Mockito.mock(BackTesterService.class);
        }
    }

    @BeforeEach
    void setUp() {
        Mockito.reset(csvImporterService, strategyService, backTesterService);
    }

    @Test
    @DisplayName("Should successfully run 5 concurrent backtests")
    public void testSuccessfulConcurrentBacktests() throws Exception {
        String csvContent = """
            date,open,high,low,close,volume
            2023-01-01,100,105,95,102,1000
            2023-01-02,102,107,100,105,1200
            2023-01-03,105,110,103,108,1100
            2023-01-04,108,112,106,110,1300
            2023-01-05,110,115,108,113,1400""";

        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();

        // Add CSV file
        ByteArrayResource csvResource = new ByteArrayResource(csvContent.getBytes()) {
            @Override
            public String getFilename() {
                return "test-data.csv";
            }
        };
        parts.add("file", csvResource);

        // Add JSON request
        List<BackTestRequest> backTestRequests = createMultipleBackTestRequests();
        String jsonContent = objectMapper.writeValueAsString(backTestRequests);
        ByteArrayResource jsonResource = new ByteArrayResource(jsonContent.getBytes()) {
            @Override
            public String getFilename() {
                return "request.json";
            }
        };
        parts.add("request", jsonResource);

        MarketData mockMarketData = new MarketData();
        Strategy mockStrategy = new Strategy();
        List<BackTestResult> expectedResults = createMockBackTestResults();

        when(csvImporterService.importCsvFromStream(any(InputStream.class))).thenReturn(mockMarketData);
        when(strategyService.buildStrategyFromRequest(any(BackTestRequest.class))).thenReturn(mockStrategy);
        when(backTesterService.runBackTest(any(Strategy.class), any(MarketData.class), any(BackTestRequest.class)))
                .thenReturn(expectedResults.get(0))
                .thenReturn(expectedResults.get(1))
                .thenReturn(expectedResults.get(2))
                .thenReturn(expectedResults.get(3))
                .thenReturn(expectedResults.get(4));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(parts, headers);

        ResponseEntity<BackTestResult[]> response = restTemplate.postForEntity(
                "/backtest/execute",
                requestEntity,
                BackTestResult[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(5);

        List<BackTestResult> results = Arrays.asList(response.getBody());

        // Verify results exist with expected characteristics (order-independent)
        // Result 1: 25K capital with 3.2% return
        BackTestResult conservativeResult = results.stream()
                .filter(r -> Math.abs(r.finalCapital() - 25800.0) < 0.01)
                .findFirst()
                .orElse(null);
        assertThat(conservativeResult).isNotNull();
        assertThat(conservativeResult.totalReturn()).isEqualTo(3.2);
        assertThat(conservativeResult.tradeCount()).isEqualTo(2);
        assertThat(conservativeResult.winRatio()).isEqualTo(100.0);

        // Result 2: 10K capital with 12.0% return
        BackTestResult aggressiveResult = results.stream()
                .filter(r -> Math.abs(r.finalCapital() - 11200.0) < 0.01)
                .findFirst()
                .orElse(null);
        assertThat(aggressiveResult).isNotNull();
        assertThat(aggressiveResult.totalReturn()).isEqualTo(12.0);
        assertThat(aggressiveResult.tradeCount()).isEqualTo(4);
        assertThat(aggressiveResult.winRatio()).isEqualTo(75.0);

        // Result 3: 20K capital with 8.5% return
        BackTestResult balancedResult = results.stream()
                .filter(r -> Math.abs(r.finalCapital() - 21700.0) < 0.01)
                .findFirst()
                .orElse(null);
        assertThat(balancedResult).isNotNull();
        assertThat(balancedResult.totalReturn()).isEqualTo(8.5);
        assertThat(balancedResult.tradeCount()).isEqualTo(3);
        assertThat(balancedResult.winRatio()).isEqualTo(66.67);

        // Result 4: 10K capital with 5.0% return
        BackTestResult originalResult = results.stream()
                .filter(r -> Math.abs(r.finalCapital() - 10500.0) < 0.01)
                .findFirst()
                .orElse(null);
        assertThat(originalResult).isNotNull();
        assertThat(originalResult.totalReturn()).isEqualTo(5.0);
        assertThat(originalResult.tradeCount()).isEqualTo(3);
        assertThat(originalResult.winRatio()).isEqualTo(66.67);

        // Result 5: Higher capital 15K with 7.5% return
        BackTestResult higherCapitalResult = results.stream()
                .filter(r -> Math.abs(r.finalCapital() - 16125.0) < 0.01)
                .findFirst()
                .orElse(null);
        assertThat(higherCapitalResult).isNotNull();
        assertThat(higherCapitalResult.totalReturn()).isEqualTo(7.5);
        assertThat(higherCapitalResult.tradeCount()).isEqualTo(4);
        assertThat(higherCapitalResult.winRatio()).isEqualTo(75.0);

        verify(csvImporterService, times(5)).importCsvFromStream(any(InputStream.class));
        verify(strategyService, times(5)).buildStrategyFromRequest(any(BackTestRequest.class));
        verify(backTesterService, times(5)).runBackTest(any(Strategy.class), any(MarketData.class), any(BackTestRequest.class));
    }

    private List<BackTestRequest> createMultipleBackTestRequests() {
        List<BackTestRequest> requests = new ArrayList<>();

        // Request 1: Original configuration
        requests.add(createBackTestRequestWithParams(10000.0, 0.02, 0.001, 0.1, false));

        // Request 2: Higher capital, different risk
        requests.add(createBackTestRequestWithParams(15000.0, 0.015, 0.0015, 0.15, true));

        // Request 3: Conservative setup
        requests.add(createBackTestRequestWithParams(25000.0, 0.025, 0.0005, 0.05, false));

        // Request 4: Aggressive setup
        requests.add(createBackTestRequestWithParams(10000.0, 0.03, 0.002, 0.2, true));

        // Request 5: Balanced setup
        requests.add(createBackTestRequestWithParams(20000.0, 0.02, 0.001, 0.1, false));

        return requests;
    }

    private BackTestRequest createBackTestRequestWithParams(double initialCapital, double riskPerTrade,
                                                            double commissionRate, double slippagePercent,
                                                            boolean allowShort) {
        List<ConditionConfig> entryConditions = new ArrayList<>();

        Map<String, Object> rsiParams = new HashMap<>();
        rsiParams.put("period", 14);
        rsiParams.put("upperThreshold", 70);
        rsiParams.put("lowerThreshold", 30);
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
        andParams.put("conditions", List.of(rsiCondition, smaCondition));

        ConditionConfig andGroup = ConditionConfig.builder()
                .type("AND")
                .parameters(andParams)
                .build();

        entryConditions.add(andGroup);

        List<ConditionConfig> exitConditions = new ArrayList<>();

        Map<String, Object> exitRsiParams = new HashMap<>();
        exitRsiParams.put("period", 14);
        exitRsiParams.put("upperThreshold", 70);
        exitRsiParams.put("lowerThreshold", 30);
        exitRsiParams.put("checkOverbought", true);

        ConditionConfig exitRsiCondition = ConditionConfig.builder()
                .type("RSI_THRESHOLD")
                .parameters(exitRsiParams)
                .build();

        exitConditions.add(exitRsiCondition);

        return BackTestRequest.builder()
                .initialCapital(initialCapital)
                .riskPerTrade(riskPerTrade)
                .commissionRate(commissionRate)
                .slippagePercent(slippagePercent)
                .allowShort(allowShort)
                .entryConditions(entryConditions)
                .exitConditions(exitConditions)
                .requireAllEntryConditions(true)
                .requireAllExitConditions(false)
                .build();
    }

    private List<BackTestResult> createMockBackTestResults() {
        List<BackTestResult> results = new ArrayList<>();

        // Result 1: Original 10K capital with 5.0% return = 500 profit
        List<Trade> trades1 = List.of(
                new Trade(100.0, 105.0, 50.0, 250.0, LocalDateTime.of(2023, 1, 1, 0, 0)),
                new Trade(105.0, 110.0, 40.0, 200.0, LocalDateTime.of(2023, 1, 2, 0, 0)),
                new Trade(110.0, 108.0, 25.0, -50.0, LocalDateTime.of(2023, 1, 3, 0, 0))
        );
        results.add(BackTestResult.builder()
                .totalReturn(5.0)
                .finalCapital(10500.0)
                .tradeCount(3)
                .winRatio(66.67)
                .maxDrawdown(2.5)
                .trades(trades1)
                .equityCurve(List.of(10000.0, 10250.0, 10450.0, 10500.0))
                .build());

        // Result 2: Higher capital 15K with 7.5% return = 1125 profit
        List<Trade> trades2 = List.of(
                new Trade(102.0, 107.0, 60.0, 300.0, LocalDateTime.of(2023, 1, 1, 0, 0)),
                new Trade(107.0, 112.0, 55.0, 275.0, LocalDateTime.of(2023, 1, 2, 0, 0)),
                new Trade(112.0, 115.0, 50.0, 150.0, LocalDateTime.of(2023, 1, 3, 0, 0)),
                new Trade(115.0, 110.0, 80.0, -400.0, LocalDateTime.of(2023, 1, 4, 0, 0))
        );
        results.add(BackTestResult.builder()
                .totalReturn(7.5)
                .finalCapital(16125.0)
                .tradeCount(4)
                .winRatio(75.0)
                .maxDrawdown(1.8)
                .trades(trades2)
                .equityCurve(List.of(15000.0, 15300.0, 15575.0, 15725.0, 16125.0))
                .build());

        // Result 3: Conservative 25K with 3.2% return = 800 profit
        List<Trade> trades3 = List.of(
                new Trade(100.0, 103.0, 150.0, 450.0, LocalDateTime.of(2023, 1, 1, 0, 0)),
                new Trade(103.0, 106.0, 116.0, 350.0, LocalDateTime.of(2023, 1, 2, 0, 0))
        );
        results.add(BackTestResult.builder()
                .totalReturn(3.2)
                .finalCapital(25800.0)
                .tradeCount(2)
                .winRatio(100.0)
                .maxDrawdown(0.0)
                .trades(trades3)
                .equityCurve(List.of(25000.0, 25450.0, 25800.0))
                .build());

        // Result 4: Aggressive 10K with 12.0% return = 1200 profit
        List<Trade> trades4 = List.of(
                new Trade(100.0, 105.0, 60.0, 300.0, LocalDateTime.of(2023, 1, 1, 0, 0)),
                new Trade(105.0, 110.0, 55.0, 275.0, LocalDateTime.of(2023, 1, 2, 0, 0)),
                new Trade(110.0, 115.0, 50.0, 250.0, LocalDateTime.of(2023, 1, 3, 0, 0)),
                new Trade(115.0, 110.0, 75.0, -375.0, LocalDateTime.of(2023, 1, 4, 0, 0))
        );
        results.add(BackTestResult.builder()
                .totalReturn(12.0)
                .finalCapital(11200.0)
                .tradeCount(4)
                .winRatio(75.0)
                .maxDrawdown(3.2)
                .trades(trades4)
                .equityCurve(List.of(10000.0, 10300.0, 10575.0, 10825.0, 11200.0))
                .build());

        // Result 5: Balanced 20K with 8.5% return = 1700 profit
        List<Trade> trades5 = List.of(
                new Trade(100.0, 105.0, 100.0, 500.0, LocalDateTime.of(2023, 1, 1, 0, 0)),
                new Trade(105.0, 110.0, 90.0, 450.0, LocalDateTime.of(2023, 1, 2, 0, 0)),
                new Trade(110.0, 105.0, 150.0, -750.0, LocalDateTime.of(2023, 1, 3, 0, 0))
        );
        results.add(BackTestResult.builder()
                .totalReturn(8.5)
                .finalCapital(21700.0)
                .tradeCount(3)
                .winRatio(66.67)
                .maxDrawdown(2.1)
                .trades(trades5)
                .equityCurve(List.of(20000.0, 20500.0, 20950.0, 21700.0))
                .build());

        return results;
    }
}