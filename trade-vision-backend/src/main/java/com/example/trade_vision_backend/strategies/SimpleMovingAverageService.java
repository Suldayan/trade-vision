package com.example.trade_vision_backend.strategies;

import java.math.BigDecimal;

public interface SimpleMovingAverageService {
    BigDecimal sumClosingPrice(Long startDate, Long endDate);
    Integer getTotalNumberOfPeriods();
    BigDecimal calculateAverage();
}
