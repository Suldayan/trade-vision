package com.example.spring_backend.backtester;

import com.example.spring_backend.backtester.internal.BackTestResult;
import com.example.spring_backend.backtester.internal.BackTesterController;
import com.example.spring_backend.backtester.internal.BackTesterOrchestrationServiceImpl;
import com.example.spring_backend.common.BackTestRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BackTesterController.class)
@DisplayName("BackTesterController Tests")
public class BackTesterControllerTest {

    @MockitoBean
    private BackTesterOrchestrationServiceImpl backTesterOrchestrationService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private List<BackTestRequest> singleRequest;
    private List<BackTestRequest> maxRequests;
    private BackTestResult mockResult;

    @BeforeEach
    public void setUp() {
        MultipartFile csvFile = createValidCsvFile();
        singleRequest = createValidBackTestRequests(1);
        maxRequests = createValidBackTestRequests(5);
        mockResult = BackTestResult.builder()
                .totalReturn(15.5)
                .finalCapital(11550.0)
                .tradeCount(10)
                .winRatio(0.6)
                .maxDrawdown(0.05)
                .trades(Collections.emptyList())
                .equityCurve(new double[0])
                .build();
    }

    @Nested
    @DisplayName("Happy Path Tests")
    class HappyPathTests {

        @Test
        @DisplayName("Should successfully execute backtest with valid CSV and requests")
        void shouldSuccessfullyExecuteBacktestWithValidData() throws Exception {
            CompletableFuture<List<BackTestResult>> expectedResults = createValidBackTestResults(5);

            given(backTesterOrchestrationService.runOrchestration(any(MultipartFile.class), any(List.class)))
                    .willReturn(expectedResults);

            MockMultipartFile csvFile = createValidCsvFile();

            String requestsJson = objectMapper.writeValueAsString(maxRequests);
            MockMultipartFile requestsPart = new MockMultipartFile(
                    "request",
                    "",
                    "application/json",
                    requestsJson.getBytes()
            );

            MvcResult mvcResult = mockMvc.perform(multipart("/api/backtest/execute")
                            .file(csvFile)
                            .file(requestsPart))
                    .andExpect(request().asyncStarted())
                    .andReturn();

            mockMvc.perform(asyncDispatch(mvcResult))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(5)))
                    .andExpect(jsonPath("$[0].totalReturn", is(15.5)))
                    .andExpect(jsonPath("$[0].finalCapital", is(11550.0)))
                    .andExpect(jsonPath("$[0].tradeCount", is(10)))
                    .andExpect(jsonPath("$[0].winRatio", is(0.6)))
                    .andExpect(jsonPath("$[0].maxDrawdown", is(0.05)))
                    .andExpect(jsonPath("$[0].trades", notNullValue()))
                    .andExpect(jsonPath("$[0].equityCurve", notNullValue()))
                    .andExpect(jsonPath("$[*].totalReturn", everyItem(notNullValue())))
                    .andExpect(jsonPath("$[*].finalCapital", everyItem(notNullValue())))
                    .andExpect(jsonPath("$[*].tradeCount", everyItem(notNullValue())))
                    .andExpect(jsonPath("$[*].winRatio", everyItem(notNullValue())))
                    .andExpect(jsonPath("$[*].maxDrawdown", everyItem(notNullValue())));

            verify(backTesterOrchestrationService, times(1))
                    .runOrchestration(any(MultipartFile.class), any(List.class));
        }

        @Test
        @DisplayName("Should return health status")
        void shouldReturnHealthStatus() throws Exception {
            mockMvc.perform(get("/api/backtest/health"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status", is("UP")))
                    .andExpect(jsonPath("$.timestamp", notNullValue()));
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle orchestration service failure")
        void shouldHandleOrchestrationServiceFailure() throws Exception {
            CompletableFuture<List<BackTestResult>> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(new RuntimeException("Processing failed"));

            given(backTesterOrchestrationService.runOrchestration(any(MultipartFile.class), any(List.class)))
                    .willReturn(failedFuture);

            MockMultipartFile csvFile = createValidCsvFile();
            String requestsJson = objectMapper.writeValueAsString(singleRequest);
            MockMultipartFile requestsPart = new MockMultipartFile(
                    "request", "", "application/json", requestsJson.getBytes()
            );

            MvcResult mvcResult = mockMvc.perform(multipart("/api/backtest/execute")
                            .file(csvFile)
                            .file(requestsPart))
                    .andExpect(request().asyncStarted())
                    .andReturn();

            mockMvc.perform(asyncDispatch(mvcResult))
                    .andDo(print())
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("Should reject request with missing file")
        void shouldRejectRequestWithMissingFile() throws Exception {
            String requestsJson = objectMapper.writeValueAsString(singleRequest);
            MockMultipartFile requestsPart = new MockMultipartFile(
                    "request", "", "application/json", requestsJson.getBytes()
            );

            mockMvc.perform(multipart("/api/backtest/execute")
                            .file(requestsPart))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject request with missing request data")
        void shouldRejectRequestWithMissingRequestData() throws Exception {
            MockMultipartFile csvFile = createValidCsvFile();

            mockMvc.perform(multipart("/api/backtest/execute")
                            .file(csvFile))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject request with invalid JSON in request part")
        void shouldRejectRequestWithInvalidJson() throws Exception {
            MockMultipartFile csvFile = createValidCsvFile();
            MockMultipartFile invalidRequestPart = new MockMultipartFile(
                    "request", "", "application/json", "invalid json".getBytes()
            );

            mockMvc.perform(multipart("/api/backtest/execute")
                            .file(csvFile)
                            .file(invalidRequestPart))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should successfully execute with single request")
        void shouldExecuteWithSingleRequest() throws Exception {
            CompletableFuture<List<BackTestResult>> expectedResults = createValidBackTestResults(1);

            given(backTesterOrchestrationService.runOrchestration(any(MultipartFile.class), any(List.class)))
                    .willReturn(expectedResults);

            MockMultipartFile csvFile = createValidCsvFile();
            String requestsJson = objectMapper.writeValueAsString(singleRequest);
            MockMultipartFile requestsPart = new MockMultipartFile(
                    "request", "", "application/json", requestsJson.getBytes()
            );

            MvcResult mvcResult = mockMvc.perform(multipart("/api/backtest/execute")
                            .file(csvFile)
                            .file(requestsPart))
                    .andExpect(request().asyncStarted())
                    .andReturn();

            mockMvc.perform(asyncDispatch(mvcResult))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].totalReturn", is(15.5)));

            verify(backTesterOrchestrationService, times(1))
                    .runOrchestration(any(MultipartFile.class), any(List.class));
        }

        @Test
        @DisplayName("Should handle empty results from orchestration service")
        void shouldHandleEmptyResults() throws Exception {
            CompletableFuture<List<BackTestResult>> emptyResults =
                    CompletableFuture.completedFuture(Collections.emptyList());

            given(backTesterOrchestrationService.runOrchestration(any(MultipartFile.class), any(List.class)))
                    .willReturn(emptyResults);

            MockMultipartFile csvFile = createValidCsvFile();
            String requestsJson = objectMapper.writeValueAsString(singleRequest);
            MockMultipartFile requestsPart = new MockMultipartFile(
                    "request", "", "application/json", requestsJson.getBytes()
            );

            MvcResult mvcResult = mockMvc.perform(multipart("/api/backtest/execute")
                            .file(csvFile)
                            .file(requestsPart))
                    .andExpect(request().asyncStarted())
                    .andReturn();

            mockMvc.perform(asyncDispatch(mvcResult))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("Should accept different file types with CSV content")
        void shouldAcceptDifferentFileTypesWithCsvContent() throws Exception {
            CompletableFuture<List<BackTestResult>> expectedResults = createValidBackTestResults(1);

            given(backTesterOrchestrationService.runOrchestration(any(MultipartFile.class), any(List.class)))
                    .willReturn(expectedResults);

            String csvContent = """
                    Date,Open,High,Low,Close,Volume
                    2023-01-01,100.0,105.0,99.0,104.0,1000000""";

            MockMultipartFile txtFile = new MockMultipartFile(
                    "file", "data.txt", "text/plain", csvContent.getBytes()
            );

            String requestsJson = objectMapper.writeValueAsString(singleRequest);
            MockMultipartFile requestsPart = new MockMultipartFile(
                    "request", "", "application/json", requestsJson.getBytes()
            );

            MvcResult mvcResult = mockMvc.perform(multipart("/api/backtest/execute")
                            .file(txtFile)
                            .file(requestsPart))
                    .andExpect(request().asyncStarted())
                    .andReturn();

            mockMvc.perform(asyncDispatch(mvcResult))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {

        @Test
        @DisplayName("Should handle maximum number of concurrent requests")
        void shouldHandleMaximumRequests() throws Exception {
            List<BackTestRequest> manyRequests = createValidBackTestRequests(10);
            CompletableFuture<List<BackTestResult>> expectedResults = createValidBackTestResults(10);

            given(backTesterOrchestrationService.runOrchestration(any(MultipartFile.class), any(List.class)))
                    .willReturn(expectedResults);

            MockMultipartFile csvFile = createValidCsvFile();
            String requestsJson = objectMapper.writeValueAsString(manyRequests);
            MockMultipartFile requestsPart = new MockMultipartFile(
                    "request", "", "application/json", requestsJson.getBytes()
            );

            MvcResult mvcResult = mockMvc.perform(multipart("/api/backtest/execute")
                            .file(csvFile)
                            .file(requestsPart))
                    .andExpect(request().asyncStarted())
                    .andReturn();

            mockMvc.perform(asyncDispatch(mvcResult))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(10)))
                    .andExpect(jsonPath("$[*].totalReturn", everyItem(notNullValue())));

            verify(backTesterOrchestrationService, times(1))
                    .runOrchestration(any(MultipartFile.class), argThat(requests ->
                            requests != null && requests.size() == 10));
        }
    }

    // Helper methods
    private MockMultipartFile createValidCsvFile() {
        String csvContent = """
                Date,Open,High,Low,Close,Volume
                2023-01-01,100.0,105.0,99.0,104.0,1000000
                2023-01-02,104.0,108.0,103.0,107.0,1200000
                2023-01-03,107.0,110.0,106.0,109.0,1100000
                2023-01-04,109.0,112.0,108.0,111.0,1300000
                2023-01-05,111.0,114.0,110.0,113.0,1150000""";

        return new MockMultipartFile(
                "file",
                "test-data.csv",
                "text/csv",
                csvContent.getBytes()
        );
    }

    private List<BackTestRequest> createValidBackTestRequests(int count) {
        List<BackTestRequest> requests = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            requests.add(BackTestRequest.builder()
                    .initialCapital(10000.0 + (i * 1000)) // Vary initial capital
                    .riskPerTrade(0.02 + (i * 0.001)) // Vary risk slightly
                    .commissionRate(0.001)
                    .slippagePercent(0.1)
                    .allowShort(i % 2 == 0) // Alternate short selling
                    .entryConditions(Collections.emptyList())
                    .exitConditions(Collections.emptyList())
                    .requireAllEntryConditions(true)
                    .requireAllExitConditions(false)
                    .build());
        }
        return requests;
    }

    private CompletableFuture<List<BackTestResult>> createValidBackTestResults(int count) {
        List<BackTestResult> results = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            results.add(mockResult);
        }
        return CompletableFuture.completedFuture(results);
    }
}