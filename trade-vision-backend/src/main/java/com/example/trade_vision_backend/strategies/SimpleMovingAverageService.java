package com.example.trade_vision_backend.strategies;

import com.example.trade_vision_backend.processing.CandleDTO;
import jakarta.annotation.Nonnull;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

public interface SimpleMovingAverageService {
    BigDecimal calculateSMA(ZonedDateTime endDate, List<String> ids);
    BigDecimal calculateAverage(List<CandleDTO> candleDTOS);
    int getNumberOfPeriods(List<CandleDTO> candleDTOS);
}
