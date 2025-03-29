package com.example.trade_vision_backend.strategies.ema;

import com.example.trade_vision_backend.strategies.ExponentialMovingAverageService;
import com.example.trade_vision_backend.strategies.SimpleMovingAverageService;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExponentialMovingAverageServiceImpl implements ExponentialMovingAverageService {
    private final SimpleMovingAverageService sma;

    @Nonnull
    @Override
    public BigDecimal calculateEMA(
            @Nonnull ZonedDateTime endDate,
            @Nonnull List<String> ids,
            int period) {
        return null;
    }

    @Nonnull
    @Override
    public List<BigDecimal> calculateEMASeries(
            @Nonnull ZonedDateTime endDate,
            @Nonnull List<String> ids,
            int period) {
        return List.of();
    }
}
