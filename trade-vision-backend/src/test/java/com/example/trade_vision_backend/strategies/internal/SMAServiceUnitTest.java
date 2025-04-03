package com.example.trade_vision_backend.strategies.internal;

import com.example.trade_vision_backend.processing.CandleDTO;
import com.example.trade_vision_backend.processing.ProcessingDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SMAServiceUnitTest {

    @Mock
    private ProcessingDataService processingDataService;

    @InjectMocks
    private SMAServiceImpl smaService;

    private final String baseId = "BTC";
    private final String quoteId = "USD";
    private final String exchangeId = "BINANCE";
    private ZonedDateTime testWindow;
    private List<CandleDTO> testCandles;

    @BeforeEach
    void setUp() {
        testWindow = ZonedDateTime.now();

        testCandles = List.of(
                createCandle(new BigDecimal("100.0000")),
                createCandle(new BigDecimal("200.0000")),
                createCandle(new BigDecimal("300.0000")),
                createCandle(new BigDecimal("400.0000")),
                createCandle(new BigDecimal("500.0000"))
        );
    }

    private CandleDTO createCandle(BigDecimal closingPrice) {
        return CandleDTO.builder()
                .baseId(baseId)
                .quoteId(quoteId)
                .exchangeId(exchangeId)
                .closingPrice(closingPrice)
                .timestamp(ZonedDateTime.now().minusHours(1))
                .build();
    }

    @Test
    void calculateSMA_ShouldReturnCorrectAverage() {
        when(processingDataService.fetchAllCandlePairsWithinTimeRange(
                eq(baseId), eq(quoteId), eq(exchangeId), any(ZonedDateTime.class)))
                .thenReturn(testCandles);

        BigDecimal result = smaService.calculateSMA(baseId, quoteId, exchangeId, testWindow);

        BigDecimal expected = new BigDecimal("300.0000"); // (100+200+300+400+500)/5 = 300
        assertEquals(expected, result);

        verify(processingDataService).fetchAllCandlePairsWithinTimeRange(
                baseId, quoteId, exchangeId, testWindow);
    }

    @Test
    void calculateAverage_ShouldReturnCorrectAverage() {
        BigDecimal result = smaService.calculateAverage(testCandles);

        BigDecimal expected = new BigDecimal("300.0000");
        assertEquals(expected, result);
    }

    @Test
    void getNumberOfPeriods_ShouldReturnCorrectSize() {
        int result = smaService.getNumberOfPeriods(testCandles);

        assertEquals(5, result);
    }

    @Test
    void validateCandleList_ShouldThrowExceptionWhenNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> smaService.calculateAverage(null)
        );

        assertEquals("Candle list cannot be null", exception.getMessage());
    }

    @Test
    void calculateAverage_ShouldThrowExceptionWhenEmpty() {
        List<CandleDTO> emptyList = Collections.emptyList();

        StrategyCalculationException exception = assertThrows(
                StrategyCalculationException.class,
                () -> smaService.calculateAverage(emptyList)
        );

        assertEquals("Cannot calculate average of empty list", exception.getMessage());
    }

    @Test
    void calculateSMA_ShouldThrowExceptionWhenNoData() {
        when(processingDataService.fetchAllCandlePairsWithinTimeRange(
                eq(baseId), eq(quoteId), eq(exchangeId), any(ZonedDateTime.class)))
                .thenReturn(Collections.emptyList());

        StrategyCalculationException exception = assertThrows(
                StrategyCalculationException.class,
                () -> smaService.calculateSMA(baseId, quoteId, exchangeId, testWindow)
        );

        assertEquals("Failed to fetch candle data", exception.getMessage());
    }

    @Test
    void calculateSMA_ShouldThrowExceptionWhenServiceFails() {
        RuntimeException underlyingException = new RuntimeException("Service failure");
        when(processingDataService.fetchAllCandlePairsWithinTimeRange(
                eq(baseId), eq(quoteId), eq(exchangeId), any(ZonedDateTime.class)))
                .thenThrow(underlyingException);

        StrategyCalculationException exception = assertThrows(
                StrategyCalculationException.class,
                () -> smaService.calculateSMA(baseId, quoteId, exchangeId, testWindow)
        );

        assertEquals("Failed to fetch candle data", exception.getMessage());
        assertEquals(underlyingException, exception.getCause());
    }

    @Test
    void calculateSMA_ShouldHandleRoundingCorrectly() {
        List<CandleDTO> unevenCandles = List.of(
                createCandle(new BigDecimal("100.1234")),
                createCandle(new BigDecimal("200.5678")),
                createCandle(new BigDecimal("300.9876"))
        );

        when(processingDataService.fetchAllCandlePairsWithinTimeRange(
                eq(baseId), eq(quoteId), eq(exchangeId), any(ZonedDateTime.class)))
                .thenReturn(unevenCandles);

        BigDecimal result = smaService.calculateSMA(baseId, quoteId, exchangeId, testWindow);

        BigDecimal expected = new BigDecimal("200.5596");
        assertEquals(expected, result);
    }
}
