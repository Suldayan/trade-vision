package com.example.trade_vision_backend.strategies.ema;

import com.example.trade_vision_backend.processing.CandleDTO;
import com.example.trade_vision_backend.strategies.EMAService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class EMAServiceImpl implements EMAService {
    @Override
    public BigDecimal calculateEMA(String baseId, String quoteId, String exchangeId, int period) {
        return null;
    }

    @Override
    public List<BigDecimal> calculateEMASeries(List<CandleDTO> candles, int period) {
        return List.of();
    }
}
