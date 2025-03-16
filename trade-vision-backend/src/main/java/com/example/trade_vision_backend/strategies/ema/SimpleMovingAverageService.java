package com.example.trade_vision_backend.strategies.ema;

import java.math.BigDecimal;

public interface SimpleMovingAverageService {
    BigDecimal sumClosingPrice();
    Integer getTotalNumberOfPeriods();
    BigDecimal calculateAverage();
}
