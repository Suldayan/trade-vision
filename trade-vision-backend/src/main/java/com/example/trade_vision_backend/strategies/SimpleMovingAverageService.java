package com.example.trade_vision_backend.strategies;

import java.math.BigDecimal;
import java.util.List;

public interface SimpleMovingAverageService {
    BigDecimal sumClosingPrice(Long startDate, Long endDate, List<String> ids);
    Integer getTotalNumberOfPeriods();
    BigDecimal calculateAverage();
}
