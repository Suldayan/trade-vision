package com.example.trade_vision_backend.strategies;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

public interface ExponentialMovingAverageService {
    BigDecimal calculateEMA(
            ZonedDateTime endDate,
            List<String> ids,
            int period
    );

    List<BigDecimal> calculateEMASeries(
            ZonedDateTime endDate,
            List<String> ids,
            int period
    );
}
