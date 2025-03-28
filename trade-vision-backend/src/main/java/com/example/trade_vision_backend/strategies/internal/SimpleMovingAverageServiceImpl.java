package com.example.trade_vision_backend.strategies.internal;

import com.example.trade_vision_backend.processing.CandleDTO;
import com.example.trade_vision_backend.processing.ProcessedMarketDTO;
import com.example.trade_vision_backend.processing.ProcessingDataService;
import com.example.trade_vision_backend.strategies.SimpleMovingAverageService;
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
public class SimpleMovingAverageServiceImpl implements SimpleMovingAverageService {
    private static final int SCALE = 4;
    private static final RoundingMode DEFAULT_ROUNDING = RoundingMode.HALF_UP;

    private final ProcessingDataService processingDataService;

    @Override
    public BigDecimal sumClosingPrice(
            @Nonnull ZonedDateTime endDate,
            @Nonnull List<String> ids
    ) {
        validateInputIds(ids);

        final String baseId = ids.getFirst();
        final String quoteId = ids.get(1);
        final String exchangeId = ids.getLast();

        List<CandleDTO> candleDTOS = fetchCandleData(baseId, quoteId, exchangeId, endDate);
        log.info("Fetched {} candle records for calculation", candleDTOS.size());

        return calculateSum(candleDTOS);
    }

    @Nonnull
    @Override
    public BigDecimal calculateAverage(@Nonnull List<CandleDTO> candleDTOS) {
        validateCandleList(candleDTOS);

        final int size = candleDTOS.size();

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

    private List<CandleDTO> fetchCandleData(
            String baseId,
            String quoteId,
            String exchangeId,
            ZonedDateTime endDate
    ) {
        try {
            List<CandleDTO> candleDTOS = processingDataService.fetchAllCandlePairsWithinTimeRange(
                    baseId,
                    quoteId,
                    exchangeId,
                    endDate
            );
            if (candleDTOS == null || candleDTOS.isEmpty()) {
                log.warn("No candle data found for parameters: base={}, quote={}, exchange={}, endDate={}",
                        baseId, quoteId, exchangeId, endDate);
                throw new StrategyCalculationException("No candle data available for calculation");
            }

            return candleDTOS;
        } catch (Exception e) {
            log.error("Error fetching candle data", e);
            throw new StrategyCalculationException("Failed to fetch candle data", e);
        }
    }

    private BigDecimal calculateSum(List<CandleDTO> candleDTOS) {
        return Optional.of(candleDTOS)
                .filter(list -> !list.isEmpty())
                .map(list -> list.stream()
                        .map(CandleDTO::closingPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                )
                .orElseThrow(() -> new StrategyCalculationException("Cannot calculate sum of empty list"));
    }

    private void validateInputIds(List<String> ids) {
        if (ids == null || ids.size() < 3) {
            throw new IllegalArgumentException("Invalid input: Requires at least 3 IDs (base, quote, exchange)");
        }
    }

    private void validateCandleList(List<CandleDTO> candleDTOS) {
        if (candleDTOS == null) {
            throw new IllegalArgumentException("Candle list cannot be null");
        }
    }
}