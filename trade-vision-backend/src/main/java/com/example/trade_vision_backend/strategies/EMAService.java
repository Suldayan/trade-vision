package com.example.trade_vision_backend.strategies;

import com.example.trade_vision_backend.processing.CandleDTO;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

public interface EMAService {
    BigDecimal calculateEMA(
        String baseId,
        String quoteId,
        String exchangeId,
        int period
    );

    List<BigDecimal> calculateEMASeries(
            List<CandleDTO> candles,
            int period
    );
}
