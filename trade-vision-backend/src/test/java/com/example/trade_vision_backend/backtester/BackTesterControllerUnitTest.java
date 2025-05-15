package com.example.trade_vision_backend.backtester;

import com.example.trade_vision_backend.backtester.internal.BackTestResult;
import com.example.trade_vision_backend.backtester.internal.BackTesterController;
import com.example.trade_vision_backend.backtester.internal.Trade;
import com.example.trade_vision_backend.domain.BackTestRequest;
import com.example.trade_vision_backend.domain.ConditionConfig;
import com.example.trade_vision_backend.market.CsvImporterService;
import com.example.trade_vision_backend.market.MarketData;
import com.example.trade_vision_backend.strategies.StrategyService;
import com.example.trade_vision_backend.strategies.Strategy;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("BackTester Controller Unit Tests")
@ActiveProfiles("test")
public class BackTesterControllerUnitTest {

    @Mock
    private CsvImporterService csvImporterService;

    @Mock
    private StrategyService strategyService;

    @Mock
    private BackTesterService backTesterService;

    @InjectMocks
    private BackTesterController backTesterController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(backTesterController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Should successfully execute backtest and return detailed results")
    public void testSuccessfulBacktestExecution() throws Exception {
        // Create mock data
        String csvContent = """
                date,open,high,low,close,volume
                2023-01-01,100,105,95,102,1000
                2023-01-02,102,107,100,105,1200""";

        MockMultipartFile csvFile = new MockMultipartFile(
                "file",
                "test-data.csv",
                MediaType.TEXT_PLAIN_VALUE,
                csvContent.getBytes()
        );

        BackTestRequest backTestRequest = createSampleBackTestRequest();

        MockMultipartFile jsonFile = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(backTestRequest)
        );

        MarketData mockMarketData = new MarketData();

        Strategy mockStrategy = new Strategy();

        List<Trade> trades = new ArrayList<>();
        trades.add(new Trade(100.0, 105.0, 10.0, 50.0));

        List<Double> equityCurve = List.of(10000.0, 10050.0);

        BackTestResult expectedResult = BackTestResult.builder()
                .totalReturn(5.0)
                .finalCapital(10050.0)
                .tradeCount(1)
                .winRatio(100.0)
                .maxDrawdown(0.0)
                .trades(trades)
                .equityCurve(equityCurve)
                .build();

        when(csvImporterService.importCsvFromStream(any(InputStream.class))).thenReturn(mockMarketData);
        when(strategyService.buildStrategyFromRequest(any(BackTestRequest.class))).thenReturn(mockStrategy);

        doReturn(expectedResult).when(backTesterService).runBackTest(any(Strategy.class), any(MarketData.class), any(BackTestRequest.class));

        mockMvc.perform(multipart("/backtest/execute")
                        .file(csvFile)
                        .file(jsonFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalReturn").value(5.0))
                .andExpect(jsonPath("$.finalCapital").value(10050.0))
                .andExpect(jsonPath("$.tradeCount").value(1))
                .andExpect(jsonPath("$.winRatio").value(100.0))
                .andExpect(jsonPath("$.maxDrawdown").value(0.0))
                .andExpect(jsonPath("$.trades[0].entryPrice").value(100.0))
                .andExpect(jsonPath("$.trades[0].exitPrice").value(105.0))
                .andExpect(jsonPath("$.trades[0].positionSize").value(10.0))
                .andExpect(jsonPath("$.trades[0].pnl").value(50.0))
                .andExpect(jsonPath("$.equityCurve[1]").value(10050.0));
    }

    @Test
    @DisplayName("Should return Bad Request status when CSV import fails")
    public void testBacktestWithInvalidCsvFormat() throws Exception {
        String csvContent = "invalid csv format";

        MockMultipartFile csvFile = new MockMultipartFile(
                "file",
                "test-data.csv",
                MediaType.TEXT_PLAIN_VALUE,
                csvContent.getBytes()
        );

        BackTestRequest backTestRequest = createSampleBackTestRequest();

        MockMultipartFile jsonFile = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(backTestRequest)
        );

        when(csvImporterService.importCsvFromStream(any(InputStream.class))).thenThrow(new java.io.IOException("CSV parsing error"));

        mockMvc.perform(multipart("/backtest/execute")
                        .file(csvFile)
                        .file(jsonFile))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return Internal Server Error when runtime exception occurs")
    public void testBacktestWithRuntimeException() throws Exception {
        String csvContent = "date,open,high,low,close,volume\n" +
                "2023-01-01,100,105,95,102,1000";

        MockMultipartFile csvFile = new MockMultipartFile(
                "file",
                "test-data.csv",
                MediaType.TEXT_PLAIN_VALUE,
                csvContent.getBytes()
        );

        BackTestRequest backTestRequest = createSampleBackTestRequest();

        MockMultipartFile jsonFile = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(backTestRequest)
        );

        MarketData mockMarketData = new MarketData();

        when(csvImporterService.importCsvFromStream(any(InputStream.class))).thenReturn(mockMarketData);
        when(strategyService.buildStrategyFromRequest(any(BackTestRequest.class))).thenThrow(new RuntimeException("Strategy building error"));

        mockMvc.perform(multipart("/backtest/execute")
                        .file(csvFile)
                        .file(jsonFile))
                .andExpect(status().isInternalServerError());
    }

    private BackTestRequest createSampleBackTestRequest() {
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

        Map<String, Object> macdParams = new HashMap<>();
        macdParams.put("fastPeriod", 12);
        macdParams.put("slowPeriod", 26);
        macdParams.put("signalPeriod", 9);
        macdParams.put("crossAbove", true);

        ConditionConfig macdCondition = ConditionConfig.builder()
                .type("MACD_CROSSOVER")
                .parameters(macdParams)
                .build();

        Map<String, Object> orParams = new HashMap<>();
        orParams.put("conditions", List.of(andGroup, macdCondition));

        ConditionConfig orGroup = ConditionConfig.builder()
                .type("OR")
                .parameters(orParams)
                .build();

        entryConditions.add(orGroup);

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
                .initialCapital(10000.0)
                .riskPerTrade(0.02)
                .commissionRate(0.001)
                .slippagePercent(0.1)
                .allowShort(false)
                .entryConditions(entryConditions)
                .exitConditions(exitConditions)
                .requireAllEntryConditions(true)
                .requireAllExitConditions(false)
                .build();
    }
}