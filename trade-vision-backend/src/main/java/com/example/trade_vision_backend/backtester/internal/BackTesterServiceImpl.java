package com.example.trade_vision_backend.backtester.internal;

import com.example.trade_vision_backend.common.BackTestRequest;
import com.example.trade_vision_backend.backtester.BackTesterService;
import com.example.trade_vision_backend.market.MarketData;
import com.example.trade_vision_backend.market.MarketDataPoint;
import com.example.trade_vision_backend.strategies.Strategy;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class BackTesterServiceImpl implements BackTesterService {

    @Nonnull
    @Override
    public BackTestResult runBackTest(
            @Nonnull Strategy strategy,
            @Nonnull MarketData marketData,
            @Nonnull BackTestRequest request) {
        log.info("Starting backtest with initial capital: ${}, commission rate: {}%",
                request.getInitialCapital(), request.getCommissionRate() * 100);
        log.debug("Market data length: {}, Entry conditions: {}, Exit conditions: {}",
                marketData.close().length,
                strategy.getEntryConditions().size(),
                strategy.getExitConditions().size());

        // Pre-Calculate all signals
        long signalStartTime = System.currentTimeMillis();
        boolean[] entrySignals = strategy.calculateEntrySignals(marketData);
        boolean[] exitSignals = strategy.calculateExitSignals(marketData);
        long signalDuration = System.currentTimeMillis() - signalStartTime;
        log.info("Signal calculation completed in {}ms", signalDuration);

        // Count total signals for logging
        int totalEntrySignals = countTrue(entrySignals);
        int totalExitSignals = countTrue(exitSignals);
        log.debug("Pre-calculated signals: {} entry signals, {} exit signals",
                totalEntrySignals, totalExitSignals);

        double initialCapital = request.getInitialCapital();
        double currentCapital = initialCapital;
        boolean inPosition = false;
        double entryPrice = 0.0;
        final int totalDataPoints = marketData.close().length;

        List<Trade> trades = new ArrayList<>(totalDataPoints/10);
        double[] equityCurve = new double[totalDataPoints];

        equityCurve[0] = initialCapital;
        double[] close = marketData.close();
        int dataLength = close.length;

        log.debug("Beginning market data iteration for backtest");
        int processedEntrySignals = 0;
        int processedExitSignals = 0;

        List<MarketDataPoint> dataPoints = marketData.getDataPoints();
        List<LocalDateTime> dates = dataPoints.stream()
                .map(MarketDataPoint::timestamp)
                .toList();

        if (dates.size() != dataPoints.size()) {
            throw new IllegalArgumentException("Market data timestamps don't match price data length");
        }

        // Main backtest loop
        for (int i = 1; i < dataLength; i++) {
            double currentPrice = close[i];

            if (inPosition && exitSignals[i]) {
                processedExitSignals++;
                double positionSize = entryPrice > 0 ? currentCapital / entryPrice : 0;
                double exitValue = positionSize * currentPrice;
                double pnl = exitValue - (positionSize * entryPrice);
                LocalDateTime date = dates.get(i);

                // Apply transaction costs
                double commission = exitValue * request.getCommissionRate();
                pnl -= commission;

                currentCapital += pnl;

                trades.add(new Trade(entryPrice, currentPrice, positionSize, pnl, date));

                log.debug("Exit signal at index {}: Exit price: ${}, P&L: ${}, Commission: ${}, Updated capital: ${}",
                        i, currentPrice, pnl, commission, currentCapital);

                // Reset position flag
                inPosition = false;
            }
            else if (!inPosition && entrySignals[i]) {
                processedEntrySignals++;
                entryPrice = currentPrice;
                inPosition = true;
                log.debug("Entry signal at index {}: Entry price: ${}", i, entryPrice);
            }

            // Update equity curve
            if (inPosition) {
                // Calculate current position value
                double positionSize = entryPrice > 0 ? currentCapital / entryPrice : 0;
                double currentValue = positionSize * currentPrice;
                equityCurve[i] = currentValue;
            } else {
                equityCurve[i] = currentCapital;
            }
        }

        // Close any open positions at the end
        if (inPosition) {
            double positionSize = entryPrice > 0 ? currentCapital / entryPrice : 0;
            double finalValue = positionSize * close[dataLength - 1];
            double pnl = finalValue - (positionSize * entryPrice);
            double commission = finalValue * request.getCommissionRate();
            pnl -= commission;
            currentCapital += pnl;

            LocalDateTime finalDate = dates.get(dataLength - 1);

            log.debug("Closing open position at end of backtest: Exit price: ${}, P&L: ${}, Commission: ${}, Final capital: ${}",
                    close[dataLength - 1], pnl, commission, currentCapital);

            trades.add(new Trade(entryPrice, close[dataLength - 1], positionSize, pnl, finalDate));
        }

        log.info("Backtest completed with {} trades ({} entry signals processed, {} exit signals processed)",
                trades.size(), processedEntrySignals, processedExitSignals);
        log.info("Initial capital: ${}, Final capital: ${}, Total return: {}%",
                initialCapital, currentCapital, ((currentCapital - initialCapital) / initialCapital) * 100);

        // Clear strategy cache to free memory
        strategy.clearCache();

        return calculatePerformanceMetrics(initialCapital, currentCapital, trades, equityCurve);
    }

    @Nonnull
    private BackTestResult calculatePerformanceMetrics(
            double initialCapital,
            double finalCapital,
            @Nonnull List<Trade> trades,
            @Nonnull double[] equityCurve) {
        log.debug("Calculating performance metrics");

        double totalReturn = (finalCapital - initialCapital) / initialCapital * 100;
        int winCount = (int) trades.stream().filter(t -> t.pnl() > 0).count();
        double winRatio = trades.isEmpty() ? 0 : (double) winCount / trades.size();
        double maxDrawdown = calculateMaxDrawdown(equityCurve);

        double averageWin = trades.stream()
                .filter(t -> t.pnl() > 0)
                .mapToDouble(Trade::pnl)
                .average()
                .orElse(0);

        double averageLoss = trades.stream()
                .filter(t -> t.pnl() < 0)
                .mapToDouble(Trade::pnl)
                .average()
                .orElse(0);

        log.info("Performance metrics: Total return: {}%, Win ratio: {}%, Max drawdown: {}%",
                String.format("%.2f", totalReturn),
                String.format("%.2f", winRatio * 100),
                String.format("%.2f", maxDrawdown * 100));
        log.debug("Additional metrics: Total trades: {}, Win count: {}, Loss count: {}, Avg win: ${}, Avg loss: ${}",
                trades.size(), winCount, trades.size() - winCount,
                String.format("%.2f", averageWin),
                String.format("%.2f", averageLoss));

        return new BackTestResult(
                totalReturn,
                finalCapital,
                trades.size(),
                winRatio,
                maxDrawdown,
                trades,
                equityCurve
        );
    }

    private double calculateMaxDrawdown(@Nonnull double[] equity) {
        log.trace("Calculating maximum drawdown from {} equity points", equity.length);

        double maxDrawdown = 0;
        double peak = equity[0];

        for (double value : equity) {
            if (value > peak) {
                peak = value;
            }

            double drawdown = (peak - value) / peak;
            if (drawdown > maxDrawdown) {
                maxDrawdown = drawdown;
            }
        }

        log.debug("Max drawdown calculated: {}%", String.format("%.2f", maxDrawdown * 100));
        return maxDrawdown;
    }

    private int countTrue(@Nonnull boolean[] array) {
        int count = 0;
        for (boolean b : array) {
            if (b) count++;
        }
        return count;
    }
}