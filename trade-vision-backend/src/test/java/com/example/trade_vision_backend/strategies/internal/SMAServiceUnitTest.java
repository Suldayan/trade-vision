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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SMAServiceImplTest {

    @Mock
    private ProcessingDataService processingDataService;

    @InjectMocks
    private SMAServiceImpl smaService;

    private List<CandleDTO> testCandles;
    private final String baseId = "BTC";
    private final String quoteId = "USD";
    private final String exchangeId = "BINANCE";
    private final int period = 5;

    @BeforeEach
    void setUp() {
        ZonedDateTime now = ZonedDateTime.now();
        testCandles = new ArrayList<>();

        testCandles.add(CandleDTO.builder()
                .baseId(baseId)
                .quoteId(quoteId)
                .exchangeId(exchangeId)
                .closingPrice(new BigDecimal("100.0000"))
                .timestamp(now.minusHours(5))
                .build());

        testCandles.add(CandleDTO.builder()
                .baseId(baseId)
                .quoteId(quoteId)
                .exchangeId(exchangeId)
                .closingPrice(new BigDecimal("200.0000"))
                .timestamp(now.minusHours(4))
                .build());

        testCandles.add(CandleDTO.builder()
                .baseId(baseId)
                .quoteId(quoteId)
                .exchangeId(exchangeId)
                .closingPrice(new BigDecimal("300.0000"))
                .timestamp(now.minusHours(3))
                .build());

        testCandles.add(CandleDTO.builder()
                .baseId(baseId)
                .quoteId(quoteId)
                .exchangeId(exchangeId)
                .closingPrice(new BigDecimal("400.0000"))
                .timestamp(now.minusHours(2))
                .build());

        testCandles.add(CandleDTO.builder()
                .baseId(baseId)
                .quoteId(quoteId)
                .exchangeId(exchangeId)
                .closingPrice(new BigDecimal("500.0000"))
                .timestamp(now.minusHours(1))
                .build());

        // Sum: 1500, Average: 300
    }

    @Test
    void calculateSMA_shouldReturnCorrectAverage() {
        when(processingDataService.fetchAllCandlePairsWithinTimeRange(
                eq(baseId), eq(quoteId), eq(exchangeId), eq(period)
        )).thenReturn(testCandles);

        BigDecimal result = smaService.calculateSMA(baseId, quoteId, exchangeId, period);

        assertEquals(new BigDecimal("300.0000"), result);
    }

    @Test
    void calculateAverage_shouldReturnCorrectValue() {
        BigDecimal result = smaService.calculateAverage(testCandles);

        assertEquals(new BigDecimal("300.0000"), result);
    }

    @Test
    void getNumberOfPeriods_shouldReturnCorrectSize() {
        int size = smaService.getNumberOfPeriods(testCandles);

        assertEquals(5, size);
    }

    @Test
    void calculateSMA_shouldThrowException_whenNoDataFound() {
        when(processingDataService.fetchAllCandlePairsWithinTimeRange(
                any(), any(), any(), anyInt()
        )).thenReturn(Collections.emptyList());

        assertThrows(StrategyCalculationException.class, () ->
                smaService.calculateSMA(baseId, quoteId, exchangeId, period)
        );
    }

    @Test
    void calculateSMA_shouldThrowException_whenServiceThrowsException() {
        when(processingDataService.fetchAllCandlePairsWithinTimeRange(
                any(), any(), any(), anyInt()
        )).thenThrow(new RuntimeException("Service error"));

        assertThrows(StrategyCalculationException.class, () ->
                smaService.calculateSMA(baseId, quoteId, exchangeId, period)
        );
    }

    @Test
    void calculateAverage_shouldThrowException_whenNullList() {
        assertThrows(IllegalArgumentException.class, () ->
                smaService.calculateAverage(null)
        );
    }

    @Test
    void calculateAverage_shouldThrowException_whenEmptyList() {
        assertThrows(StrategyCalculationException.class, () ->
                smaService.calculateAverage(Collections.emptyList())
        );
    }
}