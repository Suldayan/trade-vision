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

        final double initialCapital = request.getInitialCapital();
        final double commissionRate = request.getCommissionRate();

        log.info("Starting backtest with initial capital: ${}, commission rate: {}%",
                initialCapital, commissionRate * 100);

        // Pre-calculate all signals
        long signalStartTime = System.currentTimeMillis();
        boolean[] entrySignals = strategy.calculateEntrySignals(marketData);
        boolean[] exitSignals = strategy.calculateExitSignals(marketData);
        long signalDuration = System.currentTimeMillis() - signalStartTime;

        log.info("Signal calculation completed in {}ms", signalDuration);

        if (log.isDebugEnabled()) {
            int totalEntrySignals = countTrue(entrySignals);
            int totalExitSignals = countTrue(exitSignals);
            log.debug("Market data length: {}, Entry conditions: {}, Exit conditions: {}",
                    marketData.close().length,
                    strategy.getEntryConditions().size(),
                    strategy.getExitConditions().size());
            log.debug("Pre-calculated signals: {} entry signals, {} exit signals",
                    totalEntrySignals, totalExitSignals);
        }

        // Initialize backtest state
        double currentCapital = initialCapital;
        boolean inPosition = false;
        double entryPrice = 0.0;
        double positionSize = 0.0;

        final double[] close = marketData.close();
        final int dataLength = close.length;
        final List<MarketDataPoint> dataPoints = marketData.getDataPoints();

        // Pre-allocate collections with appropriate capacity
        List<Trade> trades = new ArrayList<>(dataLength / 10);
        double[] equityCurve = new double[dataLength];
        equityCurve[0] = initialCapital;

        int processedEntrySignals = 0;
        int processedExitSignals = 0;

        if (log.isDebugEnabled()) {
            log.debug("Beginning market data iteration for backtest");
        }

        // Main backtest loop
        for (int i = 1; i < dataLength; i++) {
            final double currentPrice = close[i];

            // Process exit signals first (takes priority)
            if (inPosition && exitSignals[i]) {
                processedExitSignals++;

                final double exitValue = positionSize * currentPrice;
                final double commission = exitValue * commissionRate;
                final double pnl = exitValue - (positionSize * entryPrice) - commission;

                currentCapital += pnl;

                final LocalDateTime exitDate = dataPoints.get(i).timestamp();
                trades.add(new Trade(entryPrice, currentPrice, positionSize, pnl, exitDate));

                if (log.isDebugEnabled()) {
                    log.debug("Exit signal at index {}: Exit price: ${}, P&L: ${}, Commission: ${}, Updated capital: ${}",
                            i, currentPrice, pnl, commission, currentCapital);
                }

                // Reset position state
                inPosition = false;
                positionSize = 0.0;
                entryPrice = 0.0;
            }
            // Process entry signals
            else if (!inPosition && entrySignals[i]) {
                processedEntrySignals++;
                entryPrice = currentPrice;
                positionSize = currentCapital / entryPrice; // Calculate once and cache
                inPosition = true;

                if (log.isDebugEnabled()) {
                    log.debug("Entry signal at index {}: Entry price: ${}, Position size: {}",
                            i, entryPrice, positionSize);
                }
            }

            equityCurve[i] = inPosition
                    ? positionSize * currentPrice
                    : currentCapital;
        }

        // Close any open positions at the end
        if (inPosition) {
            final double finalPrice = close[dataLength - 1];
            final double finalValue = positionSize * finalPrice;
            final double commission = finalValue * commissionRate;
            final double pnl = finalValue - (positionSize * entryPrice) - commission;

            currentCapital += pnl;

            final LocalDateTime finalDate = dataPoints.get(dataLength - 1).timestamp();
            trades.add(new Trade(entryPrice, finalPrice, positionSize, pnl, finalDate));

            if (log.isDebugEnabled()) {
                log.debug("Closing open position at end of backtest: Exit price: ${}, P&L: ${}, Commission: ${}, Final capital: ${}",
                        finalPrice, pnl, commission, currentCapital);
            }
        }

        log.info("Backtest completed with {} trades ({} entry signals processed, {} exit signals processed)",
                trades.size(), processedEntrySignals, processedExitSignals);
        log.info("Initial capital: ${}, Final capital: ${}, Total return: {:.2f}%",
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

        if (log.isDebugEnabled()) {
            log.debug("Calculating performance metrics");
        }

        final double totalReturn = (finalCapital - initialCapital) / initialCapital * 100;
        final double maxDrawdown = calculateMaxDrawdown(equityCurve);

        final TradeStatistics stats = calculateTradeStatistics(trades);

        log.info("Performance metrics: Total return: {:.2f}%, Win ratio: {:.2f}%, Max drawdown: {:.2f}%",
                totalReturn, stats.winRatio * 100, maxDrawdown * 100);

        if (log.isDebugEnabled()) {
            log.debug("Additional metrics: Total trades: {}, Win count: {}, Loss count: {}, Avg win: ${:.2f}, Avg loss: ${:.2f}",
                    trades.size(), stats.winCount, stats.lossCount, stats.averageWin, stats.averageLoss);
        }

        return new BackTestResult(
                totalReturn,
                finalCapital,
                trades.size(),
                stats.winRatio,
                maxDrawdown,
                trades,
                equityCurve
        );
    }

    private TradeStatistics calculateTradeStatistics(@Nonnull List<Trade> trades) {
        if (trades.isEmpty()) {
            return new TradeStatistics(0, 0, 0, 0.0, 0.0, 0.0);
        }

        int winCount = 0;
        int lossCount = 0;
        double totalWinPnl = 0.0;
        double totalLossPnl = 0.0;

        for (Trade trade : trades) {
            final double pnl = trade.pnl();
            if (pnl > 0) {
                winCount++;
                totalWinPnl += pnl;
            } else if (pnl < 0) {
                lossCount++;
                totalLossPnl += pnl;
            }
            // pnl == 0 is neither win nor loss
        }

        final double winRatio = (double) winCount / trades.size();
        final double averageWin = winCount > 0 ? totalWinPnl / winCount : 0.0;
        final double averageLoss = lossCount > 0 ? totalLossPnl / lossCount : 0.0;

        return new TradeStatistics(winCount, lossCount, trades.size(), winRatio, averageWin, averageLoss);
    }

    private double calculateMaxDrawdown(@Nonnull double[] equity) {
        if (log.isTraceEnabled()) {
            log.trace("Calculating maximum drawdown from {} equity points", equity.length);
        }

        double maxDrawdown = 0.0;
        double peak = equity[0];

        for (double value : equity) {
            if (value > peak) {
                peak = value;
            }

            final double drawdown = (peak - value) / peak;
            if (drawdown > maxDrawdown) {
                maxDrawdown = drawdown;
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Max drawdown calculated: {:.2f}%", maxDrawdown * 100);
        }

        return maxDrawdown;
    }

    private int countTrue(@Nonnull boolean[] array) {
        int count = 0;
        for (boolean b : array) {
            if (b) count++;
        }
        return count;
    }

    private record TradeStatistics(
            int winCount,
            int lossCount,
            int totalTrades,
            double winRatio,
            double averageWin,
            double averageLoss
    ) {}
}