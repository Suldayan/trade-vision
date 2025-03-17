package com.example.trade_vision_backend.strategies;

import java.math.BigDecimal;

public interface SimpleMovingAverageService {
    BigDecimal sumClosingPrice(Integer timePeriod);
    Integer getTotalNumberOfPeriods();
    BigDecimal calculateAverage();
}
