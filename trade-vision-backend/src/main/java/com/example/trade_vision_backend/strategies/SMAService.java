package com.example.trade_vision_backend.strategies;

import com.example.trade_vision_backend.processing.CandleDTO;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

public interface SMAService {
    BigDecimal calculateSMA(String baseId, String quoteId, String exchangeId, ZonedDateTime window);
    BigDecimal calculateAverage(List<CandleDTO> candleDTOS);
    int getNumberOfPeriods(List<CandleDTO> candleDTOS);
}
