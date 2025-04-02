package com.example.trade_vision_backend.strategies;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

public interface EMAService {
    BigDecimal calculateEMA(
        String baseId,
        String quoteId,
        String exchangeId,
        Long window
    );

    List<BigDecimal> calculateEMASeries(
            String baseId,
            String quoteId,
            String exchangeId,
            Long window
    );
}
