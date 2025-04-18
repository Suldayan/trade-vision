package com.example.trade_vision_backend.backtester.internal;

import com.example.trade_vision_backend.backtester.BackTesterService;
import com.example.trade_vision_backend.data.MarketData;
import com.example.trade_vision_backend.strategies.internal.Strategy;
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
        double initialCapital = request.getInitialCapital();
        double currentCapital = initialCapital;
        boolean inPosition = false;
        double entryPrice = 0.0;

        List<Trade> trades = new ArrayList<>();
        List<Double> equityCurve = new ArrayList<>();
        equityCurve.add(initialCapital);

        // Extract data arrays for processing
        double[] close = marketData.getClose();
        int dataLength = close.length;

        // Main backtest loop
        for (int i = 1; i < dataLength; i++) { // Start at 1 to have previous bar data
            double currentPrice = close[i];

            // Check for exit if in position
            if (inPosition && strategy.shouldExit(marketData, i)) {
                // Calculate profit/loss
                double positionSize = entryPrice > 0 ? currentCapital / entryPrice : 0;
                double exitValue = positionSize * currentPrice;
                double pnl = exitValue - (positionSize * entryPrice);

                // Apply transaction costs
                pnl -= (exitValue * request.getCommissionRate());

                // Update capital
                currentCapital += pnl;

                // Record trade
                trades.add(new Trade(entryPrice, currentPrice, positionSize, pnl));

                // Reset position flag
                inPosition = false;
            }
            // Check for entry if not in position
            else if (!inPosition && strategy.shouldEnter(marketData, i)) {
                entryPrice = currentPrice;
                inPosition = true;
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
            pnl -= (finalValue * request.getCommissionRate());
            currentCapital += pnl;
            trades.add(new Trade(entryPrice, close[dataLength - 1], positionSize, pnl));
        }

        return calculatePerformanceMetrics(initialCapital, currentCapital, trades, equityCurve);
    }

    @Nonnull
    private BackTestResult calculatePerformanceMetrics(
            double initialCapital,
            double finalCapital,
            @Nonnull List<Trade> trades,
            @Nonnull List<Double> equityCurve) {
        // Calculate metrics:
        // - Total return percentage
        // - Win/loss ratio
        // - Sharpe ratio
        // - Maximum drawdown
        // - etc.

        double totalReturn = (finalCapital - initialCapital) / initialCapital * 100;
        int winCount = (int) trades.stream().filter(t -> t.pnl() > 0).count();
        double winRatio = trades.isEmpty() ? 0 : (double) winCount / trades.size();

        // Find max drawdown
        double maxDrawdown = calculateMaxDrawn(equityCurve);

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

    private double calculateMaxDrawn(List<Double> equity) {
        double maxDrawn = 0;
        double peak = equity.getFirst();

        for (double value : equity) {
            if (value > peak) {
                peak = value;
            }

            double drawn = (peak - value) / peak;
            if (drawn > maxDrawn) {
                maxDrawn = drawn;
            }
        }

        return maxDrawn;
    }
}