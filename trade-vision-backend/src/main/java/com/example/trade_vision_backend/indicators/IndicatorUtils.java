package com.example.trade_vision_backend.indicators;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class IndicatorUtils {
    private IndicatorUtils() {
        throw new AssertionError("TechnicalIndicators is a utility class and should not be instantiated");
    }

    public static double[] sma(double[] prices, int window) {
        validateInputs(prices, window);

        double[] result = new double[prices.length];
        Arrays.fill(result, 0, window - 1, Double.NaN);

        double sum = 0;
        for (int i = 0; i < window - 1; i++) {
            sum += prices[i];
        }

        for (int i = window - 1; i < prices.length; i++) {
            sum += prices[i];
            result[i] = sum / window;
            if (i - window + 1 >= 0) {
                sum -= prices[i - window + 1];
            }
        }

        return result;
    }

    public static double[] ema(double[] prices, int window) {
        validateInputs(prices, window);

        double[] ema = new double[prices.length];
        double alpha = 2.0 / (window + 1);

        if (prices.length >= window) {
            double sum = 0;
            for (int i = 0; i < window; i++) {
                sum += prices[i];
            }
            ema[window - 1] = sum / window;

            for (int i = window; i < prices.length; i++) {
                ema[i] = alpha * prices[i] + (1 - alpha) * ema[i - 1];
            }

            Arrays.fill(ema, 0, window - 1, Double.NaN);
        } else {
            ema[0] = prices[0];
            for (int i = 1; i < prices.length; i++) {
                ema[i] = alpha * prices[i] + (1 - alpha) * ema[i - 1];
            }
        }

        return ema;
    }

    public static double[] rsi(double[] prices) {
        return rsi(prices, 14);
    }

    public static double[] rsi(double[] prices, int window) {
        validateInputs(prices, window);
        if (prices.length <= window) {
            throw new IllegalArgumentException("Prices array must contain more elements than window size for RSI calculation");
        }

        double[] rsi = new double[prices.length];
        double[] gains = new double[prices.length - 1];
        double[] losses = new double[prices.length - 1];

        for (int i = 0; i < prices.length - 1; i++) {
            double delta = prices[i + 1] - prices[i];
            gains[i] = Math.max(0, delta);
            losses[i] = Math.max(0, -delta);
        }

        double avgGain = 0, avgLoss = 0;
        for (int i = 0; i < window; i++) {
            avgGain += gains[i];
            avgLoss += losses[i];
        }
        avgGain /= window;
        avgLoss /= window;

        double rs = avgGain / (avgLoss + 1e-10);
        rsi[window] = 100 - (100 / (1 + rs));

        for (int i = window + 1; i < prices.length; i++) {
            avgGain = ((window - 1) * avgGain + gains[i - 1]) / window;
            avgLoss = ((window - 1) * avgLoss + losses[i - 1]) / window;

            rs = avgGain / (avgLoss + 1e-10);
            rsi[i] = 100 - (100 / (1 + rs));
        }

        Arrays.fill(rsi, 0, window, Double.NaN);

        return rsi;
    }

    public static Map<String, double[]> macd(double[] prices) {
        return macd(prices, 12, 26, 9);
    }

    public static Map<String, double[]> macd(double[] prices, int fast, int slow, int signal) {
        validateInputs(prices, Math.min(Math.min(fast, slow), signal));
        if (slow <= fast) {
            throw new IllegalArgumentException("Slow period must be greater than fast period for MACD calculation");
        }

        double[] emaFast = ema(prices, fast);
        double[] emaSlow = ema(prices, slow);

        double[] macdLine = new double[prices.length];
        for (int i = 0; i < prices.length; i++) {
            if (Double.isNaN(emaFast[i]) || Double.isNaN(emaSlow[i])) {
                macdLine[i] = Double.NaN;
            } else {
                macdLine[i] = emaFast[i] - emaSlow[i];
            }
        }

        double[] signalLine = ema(macdLine, signal);

        double[] histogram = new double[prices.length];
        for (int i = 0; i < prices.length; i++) {
            if (Double.isNaN(macdLine[i]) || Double.isNaN(signalLine[i])) {
                histogram[i] = Double.NaN;
            } else {
                histogram[i] = macdLine[i] - signalLine[i];
            }
        }

        Map<String, double[]> result = new HashMap<>();
        result.put("macdLine", macdLine);
        result.put("signalLine", signalLine);
        result.put("histogram", histogram);

        return result;
    }

    public static double[] atr(double[] high, double[] low, double[] close) {
        return atr(high, low, close, 14);
    }

    public static double[] atr(double[] high, double[] low, double[] close, int window) {
        validateInputs(high, window);
        validateInputs(low, window);
        validateInputs(close, window);

        if (high.length != low.length || high.length != close.length) {
            throw new IllegalArgumentException("High, low, and close arrays must be of the same length");
        }

        double[] atr = new double[close.length];
        double[] tr = new double[close.length];

        tr[0] = high[0] - low[0];

        for (int i = 1; i < close.length; i++) {
            double prevClose = close[i - 1];
            double range1 = high[i] - low[i];
            double range2 = Math.abs(high[i] - prevClose);
            double range3 = Math.abs(low[i] - prevClose);

            tr[i] = Math.max(range1, Math.max(range2, range3));
        }

        double sum = 0;
        for (int i = 0; i < window; i++) {
            sum += tr[i];
        }
        atr[window - 1] = sum / window;

        for (int i = window; i < tr.length; i++) {
            atr[i] = ((window - 1) * atr[i - 1] + tr[i]) / window;
        }

        Arrays.fill(atr, 0, window - 1, Double.NaN);

        return atr;
    }

    public static Map<String, double[]> bollingerBands(double[] prices) {
        return bollingerBands(prices, 20, 2.0);
    }

    public static Map<String, double[]> bollingerBands(double[] prices, int window, double numStd) {
        validateInputs(prices, window);

        double[] sma = sma(prices, window);
        double[] upper = new double[prices.length];
        double[] lower = new double[prices.length];

        for (int i = window - 1; i < prices.length; i++) {
            double sumSquaredDiff = 0;
            for (int j = 0; j < window; j++) {
                double diff = prices[i - j] - sma[i];
                sumSquaredDiff += diff * diff;
            }
            double stdDev = Math.sqrt(sumSquaredDiff / window);

            upper[i] = sma[i] + (numStd * stdDev);
            lower[i] = sma[i] - (numStd * stdDev);
        }

        Arrays.fill(upper, 0, window - 1, Double.NaN);
        Arrays.fill(lower, 0, window - 1, Double.NaN);

        Map<String, double[]> result = new HashMap<>();
        result.put("upper", upper);
        result.put("middle", sma);
        result.put("lower", lower);

        return result;
    }

    public static double[] roc(double[] prices, int period) {
        validateInputs(prices, period);

        double[] roc = new double[prices.length];
        Arrays.fill(roc, 0, period, Double.NaN);

        for (int i = period; i < prices.length; i++) {
            roc[i] = ((prices[i] - prices[i - period]) / prices[i - period]) * 100;
        }

        return roc;
    }

    public static Map<String, double[]> stochastic(double[] high, double[] low, double[] close, int kPeriod, int dPeriod) {
        validateInputs(high, kPeriod);
        validateInputs(low, kPeriod);
        validateInputs(close, kPeriod);

        if (high.length != low.length || high.length != close.length) {
            throw new IllegalArgumentException("High, low, and close arrays must be of the same length");
        }

        double[] percentK = new double[close.length];
        Arrays.fill(percentK, 0, kPeriod - 1, Double.NaN);

        for (int i = kPeriod - 1; i < close.length; i++) {
            double highestHigh = Double.NEGATIVE_INFINITY;
            double lowestLow = Double.POSITIVE_INFINITY;

            for (int j = 0; j < kPeriod; j++) {
                highestHigh = Math.max(highestHigh, high[i - j]);
                lowestLow = Math.min(lowestLow, low[i - j]);
            }

            percentK[i] = ((close[i] - lowestLow) / (highestHigh - lowestLow)) * 100;
        }

        double[] percentD = sma(percentK, dPeriod);

        Map<String, double[]> result = new HashMap<>();
        result.put("%K", percentK);
        result.put("%D", percentD);

        return result;
    }

    private static void validateInputs(double[] prices, int window) {
        if (prices == null) {
            throw new IllegalArgumentException("Price array cannot be null");
        }
        if (window <= 0) {
            throw new IllegalArgumentException("Window size must be greater than 0");
        }
        if (prices.length == 0) {
            throw new IllegalArgumentException("Price array cannot be empty");
        }
    }
}