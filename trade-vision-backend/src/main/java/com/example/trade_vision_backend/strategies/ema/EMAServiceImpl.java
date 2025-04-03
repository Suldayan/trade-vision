package com.example.trade_vision_backend.strategies.ema;

import com.example.trade_vision_backend.processing.CandleDTO;
import com.example.trade_vision_backend.processing.ProcessingDataService;
import com.example.trade_vision_backend.strategies.EMAService;
import com.example.trade_vision_backend.strategies.SMAService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class EMAServiceImpl implements EMAService {
    private final SMAService smaService; // Reuse your existing SMA
    private final ProcessingDataService processingDataService;

    private static final int SCALE = 4;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    @Override
    public BigDecimal calculateEMA(
            String baseId,
            String quoteId,
            String exchangeId,
            Long window,
            int period
    ) {
        List<CandleDTO> candles = processingDataService.fetchCandleData(baseId, quoteId, exchangeId, window, period);
        return calculateEMA(candles, period);
    }

    public BigDecimal calculateEMA(List<CandleDTO> candles, int period) {
        BigDecimal ema = smaService.calculateAverage(candles.subList(0, period));
        BigDecimal multiplier = calculateMultiplier(period);

        for (int i = period; i < candles.size(); i++) {
            BigDecimal currentClose = candles.get(i).closingPrice();
            ema = currentClose.subtract(ema)
                    .multiply(multiplier)
                    .add(ema)
                    .setScale(SCALE, ROUNDING);
        }
        return ema;
    }

    private BigDecimal calculateMultiplier(int period) {
        return BigDecimal.valueOf(2)
                .divide(BigDecimal.valueOf(period + 1), SCALE, ROUNDING);
    }
}