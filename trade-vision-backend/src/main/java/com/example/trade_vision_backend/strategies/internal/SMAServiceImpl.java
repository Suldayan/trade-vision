package com.example.trade_vision_backend.strategies.internal;

import com.example.trade_vision_backend.processing.CandleDTO;
import com.example.trade_vision_backend.processing.ProcessingDataService;
import com.example.trade_vision_backend.strategies.SMAService;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class SMAServiceImpl implements SMAService {
    private final ProcessingDataService processingDataService;

    private static final int SCALE = 4;
    private static final RoundingMode DEFAULT_ROUNDING = RoundingMode.HALF_UP;

    @Nonnull
    @Override
    public BigDecimal calculateSMA(
            @Nonnull String baseId,
            @Nonnull String quoteId,
            @Nonnull String exchangeId,
            @Nonnull ZonedDateTime window
    ) {
        List<CandleDTO> candleDTOS = fetchCandleData(baseId, quoteId, exchangeId, window);
        log.info("Fetched {} candle records for calculation", candleDTOS.size());

        return calculateAverage(candleDTOS);
    }

    @Nonnull
    private List<CandleDTO> fetchCandleData(
            @Nonnull String baseId,
            @Nonnull String quoteId,
            @Nonnull String exchangeId,
            @Nonnull ZonedDateTime window
    ) {
        try {
            List<CandleDTO> candleDTOS = processingDataService.fetchAllCandlePairsWithinTimeRange(
                    baseId,
                    quoteId,
                    exchangeId,
                    window
            );
            if (candleDTOS == null || candleDTOS.isEmpty()) {
                log.warn("No candle data found for parameters: base={}, quote={}, exchange={}, endDate={}",
                        baseId, quoteId, exchangeId, window);
                throw new StrategyCalculationException("No candle data available for calculation");
            }

            return candleDTOS;
        } catch (Exception e) {
            log.error("Error fetching candle data", e);
            throw new StrategyCalculationException("Failed to fetch candle data", e);
        }
    }

    @Nonnull
    @Override
    public BigDecimal calculateAverage(@Nonnull List<CandleDTO> candleDTOS) {
        validateCandleList(candleDTOS);

        final int size = getNumberOfPeriods(candleDTOS);

        BigDecimal sum = Optional.of(candleDTOS)
                .filter(list -> !list.isEmpty())
                .map(list -> list.stream()
                        .map(CandleDTO::closingPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                )
                .orElseThrow(() -> new StrategyCalculationException("Cannot calculate average of empty list"));

        return sum.divide(
                BigDecimal.valueOf(size),
                SCALE,
                DEFAULT_ROUNDING
        );
    }

    @Override
    public final int getNumberOfPeriods(@Nonnull List<CandleDTO> candleDTOS) {
        return candleDTOS.size();
    }

    private void validateCandleList(List<CandleDTO> candleDTOS) {
        if (candleDTOS == null) {
            throw new IllegalArgumentException("Candle list cannot be null");
        }
    }
}