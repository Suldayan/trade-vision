package com.example.spring_backend.backtester;

import com.example.spring_backend.backtester.internal.BackTestResult;
import com.example.spring_backend.backtester.internal.BackTesterExceptions;
import com.example.spring_backend.backtester.internal.BackTesterOrchestrationServiceImpl;
import com.example.spring_backend.common.BackTestRequest;
import com.example.spring_backend.market.CsvImporterService;
import com.example.spring_backend.market.MarketData;
import com.example.spring_backend.strategies.Strategy;
import com.example.spring_backend.strategies.StrategyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("BackTesterOrchestrationServiceImpl Tests with CompletableFuture")
class BackTesterOrchestrationServiceImplTest {

    @Mock
    private CsvImporterService csvImporterService;

    @Mock
    private StrategyService strategyService;

    @Mock
    private BackTesterService backTesterService;

    @Mock
    private Executor backtestExecutor;

    @InjectMocks
    private BackTesterOrchestrationServiceImpl orchestrationService;

    private MultipartFile mockFile;
    private List<BackTestRequest> validRequests;
    private MarketData mockMarketData;
    private Strategy mockStrategy;
    private BackTestResult mockResult;

    @BeforeEach
    void setUp() {
        orchestrationService = new BackTesterOrchestrationServiceImpl(
                csvImporterService,
                strategyService,
                backTesterService,
                backtestExecutor
        );

        mockFile = new MockMultipartFile(
                "test.csv", "test.csv", "text/csv",
                "date,open,high,low,close,volume\n2023-01-01,100,105,98,102,1000".getBytes()
        );

        validRequests = createValidBackTestRequests(1);
        mockMarketData = mock(MarketData.class);
        mockStrategy = mock(Strategy.class);
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
        @DisplayName("Should successfully orchestrate a single backtest")
        void shouldSuccessfullyOrchestrateSingleBacktest() throws Exception {
            setupSuccessfulMocks();
            setupSynchronousExecutor();

            CompletableFuture<List<BackTestResult>> future = orchestrationService.runOrchestration(mockFile, validRequests);

            assertNotNull(future);
            List<BackTestResult> results = future.get();
            assertThat(results).hasSize(1).containsExactly(mockResult);

            verify(csvImporterService).importCsvFromStream(any(InputStream.class));
            verify(strategyService).buildStrategyFromRequest(validRequests.getFirst());
            verify(backTesterService).runBackTest(mockStrategy, mockMarketData, validRequests.getFirst());
        }

        @Test
        @DisplayName("Should successfully orchestrate multiple backtests")
        void shouldSuccessfullyOrchestrateMultipleBacktests() throws Exception {
            List<BackTestRequest> multipleRequests = createValidBackTestRequests(3);
            setupSuccessfulMocks();
            setupSynchronousExecutor();

            CompletableFuture<List<BackTestResult>> future = orchestrationService.runOrchestration(mockFile, multipleRequests);

            assertNotNull(future);
            List<BackTestResult> results = future.get();
            assertThat(results).hasSize(3);

            verify(csvImporterService, times(3)).importCsvFromStream(any(InputStream.class));
            verify(strategyService, times(3)).buildStrategyFromRequest(any(BackTestRequest.class));
            verify(backTesterService, times(3)).runBackTest(eq(mockStrategy), eq(mockMarketData), any(BackTestRequest.class));
        }

        @Test
        @DisplayName("Should handle maximum allowed concurrent backtests")
        void shouldHandleMaximumAllowedConcurrentBacktests() throws Exception {
            List<BackTestRequest> maxRequests = createValidBackTestRequests(5);
            setupSuccessfulMocks();
            setupSynchronousExecutor();

            CompletableFuture<List<BackTestResult>> future = orchestrationService.runOrchestration(mockFile, maxRequests);

            assertNotNull(future);
            List<BackTestResult> results = future.get();
            assertThat(results).hasSize(5);
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should reject requests exceeding maximum limit by returning a failed future")
        void shouldRejectRequestsExceedingMaximumLimit() {
            List<BackTestRequest> tooManyRequests = createValidBackTestRequests(6);

            CompletableFuture<List<BackTestResult>> future = orchestrationService.runOrchestration(mockFile, tooManyRequests);

            assertThatThrownBy(future::get)
                    .isInstanceOf(ExecutionException.class)
                    .hasCauseInstanceOf(BackTesterExceptions.InvalidRequestException.class)
                    .hasMessageContaining("Number of requests exceeds maximum for concurrent backtests: 5");

            verifyNoInteractions(csvImporterService, strategyService, backTesterService, backtestExecutor);
        }

        @Test
        @DisplayName("Should handle null requests by returning a failed future")
        void shouldHandleNullRequests() {
            CompletableFuture<List<BackTestResult>> future = orchestrationService.runOrchestration(mockFile, null);

            assertThatThrownBy(future::get)
                    .isInstanceOf(ExecutionException.class)
                    .hasCauseInstanceOf(BackTesterExceptions.InvalidRequestException.class)
                    .hasMessageContaining("Requests cannot be null or empty");

            verifyNoInteractions(csvImporterService, strategyService, backTesterService, backtestExecutor);
        }

        @Test
        @DisplayName("Should handle empty requests by returning a failed future")
        void shouldHandleEmptyRequests() {
            CompletableFuture<List<BackTestResult>> future = orchestrationService.runOrchestration(mockFile, Collections.emptyList());

            assertThatThrownBy(future::get)
                    .isInstanceOf(ExecutionException.class)
                    .hasCauseInstanceOf(BackTesterExceptions.InvalidRequestException.class)
                    .hasMessageContaining("Requests cannot be null or empty");

            verifyNoInteractions(csvImporterService, strategyService, backTesterService, backtestExecutor);
        }

        @Test
        @DisplayName("Should handle file reading IOException by completing future exceptionally")
        void shouldHandleFileReadingIOException() throws Exception {
            MultipartFile faultyFile = mock(MultipartFile.class);
            when(faultyFile.getInputStream()).thenThrow(new IOException("File read error"));
            setupAsyncExecution();

            CompletableFuture<List<BackTestResult>> future = orchestrationService.runOrchestration(faultyFile, validRequests);

            assertThatThrownBy(future::get)
                    .isInstanceOf(ExecutionException.class)
                    .hasCauseInstanceOf(BackTesterExceptions.InvalidRequestException.class)
                    .hasMessageContaining("Failed to process market data file");

            verify(backtestExecutor, times(2)).execute(any());
            verifyNoInteractions(csvImporterService, strategyService, backTesterService);
        }

        @Test
        @DisplayName("Should handle CSV parsing errors by completing future exceptionally")
        void shouldHandleCsvParsingErrors() throws Exception {
            when(csvImporterService.importCsvFromStream(any(InputStream.class))).thenThrow(new IOException("Invalid CSV format"));
            setupAsyncExecution();

            CompletableFuture<List<BackTestResult>> future = orchestrationService.runOrchestration(mockFile, validRequests);

            assertThatThrownBy(future::get)
                    .isInstanceOf(ExecutionException.class)
                    .hasCauseInstanceOf(BackTesterExceptions.InvalidRequestException.class)
                    .hasMessageContaining("Invalid CSV format");

            verify(csvImporterService).importCsvFromStream(any(InputStream.class));
            verifyNoInteractions(strategyService, backTesterService);
        }

        @Test
        @DisplayName("Should handle validation errors by completing future exceptionally")
        void shouldHandleValidationErrors() throws Exception {
            when(csvImporterService.importCsvFromStream(any(InputStream.class))).thenReturn(mockMarketData);
            when(strategyService.buildStrategyFromRequest(any(BackTestRequest.class))).thenThrow(new IllegalArgumentException("Invalid strategy parameters"));
            setupAsyncExecution();

            CompletableFuture<List<BackTestResult>> future = orchestrationService.runOrchestration(mockFile, validRequests);

            assertThatThrownBy(future::get)
                    .isInstanceOf(ExecutionException.class)
                    .hasCauseInstanceOf(BackTesterExceptions.InvalidRequestException.class)
                    .hasMessageContaining("Invalid request parameters");

            verify(csvImporterService).importCsvFromStream(any(InputStream.class));
            verify(strategyService).buildStrategyFromRequest(any(BackTestRequest.class));
            verifyNoInteractions(backTesterService);
        }

        @Test
        @DisplayName("Should handle unexpected errors by completing future exceptionally")
        void shouldHandleUnexpectedErrors() throws Exception {
            when(csvImporterService.importCsvFromStream(any(InputStream.class))).thenReturn(mockMarketData);
            when(strategyService.buildStrategyFromRequest(any(BackTestRequest.class))).thenReturn(mockStrategy);
            when(backTesterService.runBackTest(any(Strategy.class), any(MarketData.class), any(BackTestRequest.class))).thenThrow(new RuntimeException("Unexpected error"));
            setupAsyncExecution();

            CompletableFuture<List<BackTestResult>> future = orchestrationService.runOrchestration(mockFile, validRequests);

            assertThatThrownBy(future::get)
                    .isInstanceOf(ExecutionException.class)
                    .hasCauseInstanceOf(BackTesterExceptions.BackTestOrchestrationException.class)
                    .hasMessageContaining("Internal server error during backtest");

            verify(csvImporterService).importCsvFromStream(any(InputStream.class));
            verify(strategyService).buildStrategyFromRequest(any(BackTestRequest.class));
            verify(backTesterService).runBackTest(eq(mockStrategy), eq(mockMarketData), any(BackTestRequest.class));
        }

        @Test
        @DisplayName("Should handle partial failures by completing the future exceptionally")
        void shouldHandlePartialFailures() throws Exception {
            List<BackTestRequest> multipleRequests = createValidBackTestRequests(3);
            when(csvImporterService.importCsvFromStream(any(InputStream.class))).thenReturn(mockMarketData);
            when(strategyService.buildStrategyFromRequest(any(BackTestRequest.class))).thenReturn(mockStrategy);
            when(backTesterService.runBackTest(eq(mockStrategy), eq(mockMarketData), any(BackTestRequest.class)))
                    .thenReturn(mockResult)
                    .thenReturn(mockResult)
                    .thenThrow(new RuntimeException("Third backtest failed"));
            setupAsyncExecution();

            CompletableFuture<List<BackTestResult>> future = orchestrationService.runOrchestration(mockFile, multipleRequests);

            assertThatThrownBy(future::get)
                    .isInstanceOf(ExecutionException.class)
                    .hasCauseInstanceOf(BackTesterExceptions.BackTestOrchestrationException.class)
                    .hasMessageContaining("Internal server error during backtest");

            verify(backtestExecutor, times(4)).execute(any(Runnable.class));
        }

        @Test
        @DisplayName("Should handle timeout scenarios")
        void shouldHandleTimeoutScenarios() throws Exception {
            setupSuccessfulMocks();
            when(backTesterService.runBackTest(any(), any(), any())).thenAnswer(invocation -> {
                Thread.sleep(200);
                return mockResult;
            });
            setupAsyncExecution();

            CompletableFuture<List<BackTestResult>> future = orchestrationService.runOrchestration(mockFile, validRequests);

            assertThatThrownBy(() -> future.get(50, TimeUnit.MILLISECONDS)).isInstanceOf(TimeoutException.class);
            assertThatCode(() -> future.get(500, TimeUnit.MILLISECONDS)).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Concurrency and Resource Tests")
    class ConcurrencyTests {
        @Test
        @DisplayName("Should execute orchestration on the provided executor")
        void shouldExecuteBacktestsUsingProvidedExecutor() throws Exception {
            setupSuccessfulMocks();
            ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);

            orchestrationService.runOrchestration(mockFile, validRequests);

            verify(backtestExecutor).execute(runnableCaptor.capture());
            assertThat(runnableCaptor.getValue()).isNotNull();
        }

        @Test
        @DisplayName("Should reuse file data across multiple backtests without re-reading file")
        void shouldReuseFileDataAcrossMultipleBacktests() throws Exception {
            List<BackTestRequest> multipleRequests = createValidBackTestRequests(3);
            setupSuccessfulMocks();
            setupSynchronousExecutor();
            ArgumentCaptor<InputStream> inputStreamCaptor = ArgumentCaptor.forClass(InputStream.class);
            MultipartFile spyFile = spy(mockFile);

            orchestrationService.runOrchestration(spyFile, multipleRequests);

            verify(spyFile, times(1)).getBytes();
            verify(csvImporterService, times(3)).importCsvFromStream(inputStreamCaptor.capture());
            assertThat(inputStreamCaptor.getAllValues()).allMatch(stream -> stream instanceof ByteArrayInputStream);
        }
    }

    private void setupSuccessfulMocks() throws Exception {
        when(csvImporterService.importCsvFromStream(any(InputStream.class))).thenReturn(mockMarketData);
        when(strategyService.buildStrategyFromRequest(any(BackTestRequest.class))).thenReturn(mockStrategy);
        when(backTesterService.runBackTest(any(Strategy.class), any(MarketData.class), any(BackTestRequest.class))).thenReturn(mockResult);
    }

    private void setupSynchronousExecutor() {
        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(backtestExecutor).execute(any(Runnable.class));
    }

    private void setupAsyncExecution() {
        ExecutorService service = Executors.newSingleThreadExecutor();
        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            service.submit(task);
            return null;
        }).when(backtestExecutor).execute(any(Runnable.class));
    }

    private List<BackTestRequest> createValidBackTestRequests(int count) {
        List<BackTestRequest> requests = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            requests.add(BackTestRequest.builder()
                    .initialCapital(10000.0)
                    .riskPerTrade(0.02)
                    .commissionRate(0.001)
                    .slippagePercent(0.1)
                    .allowShort(false)
                    .entryConditions(Collections.emptyList())
                    .exitConditions(Collections.emptyList())
                    .requireAllEntryConditions(true)
                    .requireAllExitConditions(false)
                    .build());
        }
        return requests;
    }
}