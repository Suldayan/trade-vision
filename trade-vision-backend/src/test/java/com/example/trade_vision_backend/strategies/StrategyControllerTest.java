package com.example.trade_vision_backend.strategies;

import com.example.trade_vision_backend.strategies.internal.StrategyController;
import com.example.trade_vision_backend.strategies.internal.StrategyManager;
import com.example.trade_vision_backend.strategies.internal.StrategyModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StrategyController.class)
@DisplayName("Strategy Controller Test")
public class StrategyControllerTest {

    @MockitoBean
    private StrategyManager strategyManager;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private StrategyModel rsiStrategy;
    private List<StrategyModel> strategyList;

    @BeforeEach
    public void setup() {
        Map<String, StrategyModel.ParameterDefinition> rsiParams = new HashMap<>();
        rsiParams.put("period", new StrategyModel.ParameterDefinition("number", 14, "Period"));
        rsiParams.put("upperThreshold", new StrategyModel.ParameterDefinition("number", 70, "Upper Threshold"));
        rsiParams.put("lowerThreshold", new StrategyModel.ParameterDefinition("number", 30, "Lower Threshold"));

        rsiStrategy = new StrategyModel("RSI_THRESHOLD", "RSI Threshold",
                "Triggers when RSI crosses above or below specified threshold levels", rsiParams);

        Map<String, StrategyModel.ParameterDefinition> smaParams = new HashMap<>();
        smaParams.put("fastPeriod", new StrategyModel.ParameterDefinition("number", 5, "Fast Period"));
        smaParams.put("slowPeriod", new StrategyModel.ParameterDefinition("number", 20, "Slow Period"));

        StrategyModel smaStrategy = new StrategyModel("SMA_CROSSOVER", "SMA Crossover",
                "Signals when fast moving average crosses above or below slow moving average", smaParams);

        strategyList = new ArrayList<>();
        strategyList.add(rsiStrategy);
        strategyList.add(smaStrategy);
    }

    @Nested
    @DisplayName("GET /api/strategies/all")
    class GetAllStrategiesTests {

        @Test
        @DisplayName("Successfully returns all strategies")
        void getAllStrategies_SuccessfullyReturnsAllStrategies() throws Exception {
            given(strategyManager.getAllStrategies()).willReturn(strategyList);

            ResultActions response = mockMvc.perform(get("/api/strategies/all"));

            response.andExpect(status().isOk())
                    .andExpect(content().contentType("application/json"))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].key", is("RSI_THRESHOLD")))
                    .andExpect(jsonPath("$[0].name", is("RSI Threshold")))
                    .andExpect(jsonPath("$[0].description", is("Triggers when RSI crosses above or below specified threshold levels")))
                    .andExpect(jsonPath("$[0].parameters.period.type", is("number")))
                    .andExpect(jsonPath("$[0].parameters.period.defaultValue", is(14)))
                    .andExpect(jsonPath("$[1].key", is("SMA_CROSSOVER")))
                    .andExpect(jsonPath("$[1].name", is("SMA Crossover")))
                    .andDo(print());
        }

        @Test
        @DisplayName("Returns empty list when no strategies exist")
        void getAllStrategies_ReturnsEmptyListWhenNoStrategies() throws Exception {
            given(strategyManager.getAllStrategies()).willReturn(new ArrayList<>());

            ResultActions response = mockMvc.perform(get("/api/strategies/all"));

            response.andExpect(status().isOk())
                    .andExpect(content().contentType("application/json"))
                    .andExpect(jsonPath("$", hasSize(0)))
                    .andExpect(jsonPath("$", is(empty())))
                    .andDo(print());
        }

        @Test
        @DisplayName("Validates JSON structure and data types")
        void getAllStrategies_ValidatesJsonStructureAndDataTypes() throws Exception {
            given(strategyManager.getAllStrategies()).willReturn(strategyList);

            ResultActions response = mockMvc.perform(get("/api/strategies/all"));

            response.andExpect(status().isOk())
                    .andExpect(jsonPath("$", isA(List.class)))
                    .andExpect(jsonPath("$[0]", isA(Map.class)))
                    .andExpect(jsonPath("$[0].key", isA(String.class)))
                    .andExpect(jsonPath("$[0].name", isA(String.class)))
                    .andExpect(jsonPath("$[0].description", isA(String.class)))
                    .andExpect(jsonPath("$[0].parameters", isA(Map.class)))
                    .andExpect(jsonPath("$[0].parameters.period.type", is("number")))
                    .andExpect(jsonPath("$[0].parameters.period.defaultValue", isA(Number.class)))
                    .andExpect(jsonPath("$[0].parameters.period.label", isA(String.class)))
                    .andDo(print());
        }

        @Test
        @DisplayName("Returns correct HTTP headers")
        void getAllStrategies_ReturnsCorrectHttpHeaders() throws Exception {
            given(strategyManager.getAllStrategies()).willReturn(strategyList);

            mockMvc.perform(get("/api/strategies/all"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type", "application/json"));
        }
    }

    @Nested
    @DisplayName("GET /api/strategies/{key}")
    class GetSpecificStrategyTests {

        @Test
        @DisplayName("Successfully returns specific strategy")
        void getStrategy_SuccessfullyReturnsSpecificStrategy() throws Exception {
            String strategyKey = "RSI_THRESHOLD";
            given(strategyManager.getStrategy(strategyKey)).willReturn(rsiStrategy);

            ResultActions response = mockMvc.perform(get("/api/strategies/{key}", strategyKey));

            response.andExpect(status().isOk())
                    .andExpect(content().contentType("application/json"))
                    .andExpect(jsonPath("$.key", is("RSI_THRESHOLD")))
                    .andExpect(jsonPath("$.name", is("RSI Threshold")))
                    .andExpect(jsonPath("$.description", is("Triggers when RSI crosses above or below specified threshold levels")))
                    .andExpect(jsonPath("$.parameters.period.type", is("number")))
                    .andExpect(jsonPath("$.parameters.period.defaultValue", is(14)))
                    .andExpect(jsonPath("$.parameters.period.label", is("Period")))
                    .andExpect(jsonPath("$.parameters.upperThreshold.defaultValue", is(70)))
                    .andExpect(jsonPath("$.parameters.lowerThreshold.defaultValue", is(30)))
                    .andDo(print());
        }

        @Test
        @DisplayName("Returns strategy with complex parameters")
        void getStrategy_ReturnsStrategyWithComplexParameters() throws Exception {
            Map<String, StrategyModel.ParameterDefinition> complexParams = new HashMap<>();
            complexParams.put("checkOverbought", new StrategyModel.ParameterDefinition("boolean", false, "Check Overbought"));
            complexParams.put("crossAbove", new StrategyModel.ParameterDefinition("boolean", true, "Cross Above"));

            StrategyModel complexStrategy = new StrategyModel("COMPLEX_STRATEGY", "Complex Strategy",
                    "Strategy with various parameter types", complexParams);

            given(strategyManager.getStrategy("COMPLEX_STRATEGY")).willReturn(complexStrategy);

            ResultActions response = mockMvc.perform(get("/api/strategies/{key}", "COMPLEX_STRATEGY"));

            response.andExpect(status().isOk())
                    .andExpect(jsonPath("$.parameters.checkOverbought.type", is("boolean")))
                    .andExpect(jsonPath("$.parameters.checkOverbought.defaultValue", is(false)))
                    .andExpect(jsonPath("$.parameters.crossAbove.type", is("boolean")))
                    .andExpect(jsonPath("$.parameters.crossAbove.defaultValue", is(true)))
                    .andDo(print());
        }

        @Test
        @DisplayName("Returns correct HTTP headers")
        void getStrategy_ReturnsCorrectHttpHeaders() throws Exception {
            given(strategyManager.getStrategy("RSI_THRESHOLD")).willReturn(rsiStrategy);

            mockMvc.perform(get("/api/strategies/{key}", "RSI_THRESHOLD"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type", "application/json"));
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Returns 404 when strategy not found")
        void getStrategy_Returns404WhenStrategyNotFound() throws Exception {
            String nonExistentKey = "NON_EXISTENT_STRATEGY";
            given(strategyManager.getStrategy(nonExistentKey)).willReturn(null);

            ResultActions response = mockMvc.perform(get("/api/strategies/{key}", nonExistentKey));

            response.andExpect(status().isNotFound())
                    .andDo(print());
        }

        @Test
        @DisplayName("Handles empty key parameter")
        void getStrategy_HandlesEmptyKeyParameter() throws Exception {
            String emptyKey = "";

            mockMvc.perform(get("/api/strategies/{key}", emptyKey))
                    .andExpect(status().isNotFound())
                    .andDo(print());
        }

        @Test
        @DisplayName("Handles special characters in key")
        void getStrategy_HandlesSpecialCharactersInKey() throws Exception {
            String specialKey = "STRATEGY_WITH_SPECIAL_CHARS_!@#";
            given(strategyManager.getStrategy(specialKey)).willReturn(null);

            ResultActions response = mockMvc.perform(get("/api/strategies/{key}", specialKey));

            response.andExpect(status().isNotFound())
                    .andDo(print());
        }
    }
}