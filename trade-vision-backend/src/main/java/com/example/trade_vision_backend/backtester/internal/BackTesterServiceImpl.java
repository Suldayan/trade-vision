package com.example.trade_vision_backend.backtester.internal;

import com.example.trade_vision_backend.common.BackTestRequest;
import com.example.trade_vision_backend.backtester.BackTesterService;
import com.example.trade_vision_backend.market.MarketData;
import com.example.trade_vision_backend.strategies.Strategy;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

        double initialCapital = request.getInitialCapital();
        double currentCapital = initialCapital;
        boolean inPosition = false;
        double entryPrice = 0.0;

        List<Trade> trades = new ArrayList<>();
        List<Double> equityCurve = new ArrayList<>();

        equityCurve.add(initialCapital);
        double[] close = marketData.close();
        int dataLength = close.length;

        log.debug("Beginning market data iteration for backtest");
        int entrySignals = 0;
        int exitSignals = 0;

        for (int i = 1; i < dataLength; i++) {
            double currentPrice = close[i];

            // Check for exit if in position
            if (inPosition && strategy.shouldExit(marketData, i)) {
                exitSignals++;
                // Calculate profit/loss
                double positionSize = entryPrice > 0 ? currentCapital / entryPrice : 0;
                double exitValue = positionSize * currentPrice;
                double pnl = exitValue - (positionSize * entryPrice);

                // Apply transaction costs
                double commission = exitValue * request.getCommissionRate();
                pnl -= commission;

                // Update capital
                currentCapital += pnl;

                // Record trade
                trades.add(new Trade(entryPrice, currentPrice, positionSize, pnl));

                log.debug("Exit signal at index {}: Exit price: ${}, P&L: ${}, Commission: ${}, Updated capital: ${}",
                        i, currentPrice, pnl, commission, currentCapital);

                // Reset position flag
                inPosition = false;
            }
            // Check for entry if not in position
            else if (!inPosition && strategy.shouldEnter(marketData, i)) {
                entrySignals++;
                entryPrice = currentPrice;
                inPosition = true;
                log.debug("Entry signal at index {}: Entry price: ${}", i, entryPrice);
            }

            // Update equity curve
            if (inPosition) {
                // Calculate current position value
                double positionSize = entryPrice > 0 ? currentCapital / entryPrice : 0;
                double currentValue = positionSize * currentPrice;
                equityCurve.add(currentValue);
            } else {
                equityCurve.add(currentCapital);
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

            log.debug("Closing open position at end of backtest: Exit price: ${}, P&L: ${}, Commission: ${}, Final capital: ${}",
                    close[dataLength - 1], pnl, commission, currentCapital);

            trades.add(new Trade(entryPrice, close[dataLength - 1], positionSize, pnl));
        }

        log.info("Backtest completed with {} trades ({} entry signals, {} exit signals)",
                trades.size(), entrySignals, exitSignals);
        log.info("Initial capital: ${}, Final capital: ${}, Total return: {}%",
                initialCapital, currentCapital, ((currentCapital - initialCapital) / initialCapital) * 100);

        return calculatePerformanceMetrics(initialCapital, currentCapital, trades, equityCurve);
    }

    @Nonnull
    private BackTestResult calculatePerformanceMetrics(
            double initialCapital,
            double finalCapital,
            @Nonnull List<Trade> trades,
            @Nonnull List<Double> equityCurve) {
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

    private double calculateMaxDrawdown(@Nonnull List<Double> equity) {
        log.trace("Calculating maximum drawdown from {} equity points", equity.size());

        double maxDrawdown = 0;
        double peak = equity.getFirst();

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
}