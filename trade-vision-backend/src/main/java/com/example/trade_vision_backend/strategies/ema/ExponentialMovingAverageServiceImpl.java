package com.example.trade_vision_backend.strategies.ema;

import com.example.trade_vision_backend.strategies.ExponentialMovingAverageService;
import com.example.trade_vision_backend.strategies.SimpleMovingAverageService;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExponentialMovingAverageServiceImpl implements ExponentialMovingAverageService {
    private final SimpleMovingAverageService smaService;
    private final PriceDataRepository priceDataRepository;

    private static final int DECIMAL_SCALE = 8;

    @Nonnull
    @Override
    public BigDecimal calculateEMA(
            @Nonnull ZonedDateTime endDate,
            @Nonnull List<String> ids,
            int period) {
        if (period <= 0) {
            throw new IllegalArgumentException("Period must be a positive integer");
        }

        log.debug("Calculating EMA for period {} with end date {}", period, endDate);

        List<PriceDataPoint> priceDataPoints = priceDataRepository.findByIdsAndDateBefore(ids, endDate, period * 2);

        if (priceDataPoints.isEmpty()) {
            log.warn("No price data points found for the specified parameters");
            throw new IllegalArgumentException("Insufficient data for EMA calculation");
        }

        Collections.sort(priceDataPoints);

        return calculateEMAFromDataPoints(priceDataPoints, period);
    }

    @Nonnull
    @Override
    public List<BigDecimal> calculateEMASeries(
            @Nonnull ZonedDateTime endDate,
            @Nonnull List<String> ids,
            int period) {
        if (period <= 0) {
            throw new IllegalArgumentException("Period must be a positive integer");
        }

        log.debug("Calculating EMA series for period {} with end date {}", period, endDate);

        // Get the price data points up to endDate - need more data for a series
        List<PriceDataPoint> priceDataPoints = priceDataRepository.findByIdsAndDateBefore(ids, endDate, period * 3);

        if (priceDataPoints.size() < period) {
            log.warn("Insufficient price data points for EMA series calculation");
            return Collections.emptyList();
        }

        // Sort data by date (most recent last)
        Collections.sort(priceDataPoints);

        return calculateEMASeriesFromDataPoints(priceDataPoints, period);
    }

    /**
     * Calculate EMA from a list of price data points
     *
     * @param dataPoints Sorted list of price data points (oldest first)
     * @param period The EMA period
     * @return The EMA value
     */
    private BigDecimal calculateEMAFromDataPoints(List<PriceDataPoint> dataPoints, int period) {
        if (dataPoints.size() <= period) {
            // If we have exactly enough or fewer data points than the period, use SMA as the first EMA
            BigDecimal prices = BigDecimal.ZERO;
            for (int i = dataPoints.size() - period; i < dataPoints.size(); i++) {
                prices = prices.add(dataPoints.get(i).getPrice());
            }
            return prices.divide(BigDecimal.valueOf(period), DECIMAL_SCALE, RoundingMode.HALF_UP);
        }

        // Calculate the multiplier
        BigDecimal multiplier = BigDecimal.valueOf(2.0 / (period + 1));

        // Use SMA for the initial EMA value (for the first 'period' data points)
        BigDecimal ema = BigDecimal.ZERO;
        for (int i = 0; i < period; i++) {
            ema = ema.add(dataPoints.get(i).getPrice());
        }
        ema = ema.divide(BigDecimal.valueOf(period), DECIMAL_SCALE, RoundingMode.HALF_UP);

        // Calculate EMA for the remaining data points
        for (int i = period; i < dataPoints.size(); i++) {
            BigDecimal currentPrice = dataPoints.get(i).getPrice();
            // EMA = Current price * multiplier + Previous EMA * (1 - multiplier)
            ema = currentPrice.multiply(multiplier)
                    .add(ema.multiply(BigDecimal.ONE.subtract(multiplier)))
                    .setScale(DECIMAL_SCALE, RoundingMode.HALF_UP);
        }

        return ema;
    }

    private List<BigDecimal> calculateEMASeriesFromDataPoints(List<PriceDataPoint> dataPoints, int period) {
        List<BigDecimal> emaSeries = new ArrayList<>();

        if (dataPoints.size() < period) {
            return emaSeries; // Return empty list if not enough data
        }

        // Calculate the multiplier
        BigDecimal multiplier = BigDecimal.valueOf(2.0 / (period + 1));

        // Calculate initial SMA
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = 0; i < period; i++) {
            sum = sum.add(dataPoints.get(i).getPrice());
        }
        BigDecimal ema = sum.divide(BigDecimal.valueOf(period), DECIMAL_SCALE, RoundingMode.HALF_UP);
        emaSeries.add(ema);

        // Calculate subsequent EMAs
        for (int i = period; i < dataPoints.size(); i++) {
            BigDecimal currentPrice = dataPoints.get(i).getPrice();
            // EMA = Current price * multiplier + Previous EMA * (1 - multiplier)
            ema = currentPrice.multiply(multiplier)
                    .add(ema.multiply(BigDecimal.ONE.subtract(multiplier)))
                    .setScale(DECIMAL_SCALE, RoundingMode.HALF_UP);
            emaSeries.add(ema);
        }

        return emaSeries;
    }

    private BigDecimal calculateWeightMultiplier(@Nonnull Integer timePeriod) {
        if (timePeriod <= 0) {
            throw new IllegalArgumentException("Time period must be a positive integer");
        }
        return BigDecimal.valueOf(2.0 / (timePeriod + 1.0)).setScale(DECIMAL_SCALE, RoundingMode.HALF_UP);
    }
}