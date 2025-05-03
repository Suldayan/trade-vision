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
        Arrays.fill(result, Double.NaN);

        for (int i = window - 1; i < prices.length; i++) {
            boolean hasNaN = false;
            double sum = 0;

            for (int j = 0; j < window; j++) {
                if (Double.isNaN(prices[i - j])) {
                    hasNaN = true;
                    break;
                }
                sum += prices[i - j];
            }

            if (!hasNaN) {
                result[i] = sum / window;
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

        double rs = avgGain / (avgLoss == 0 ? 1e-10 : avgLoss);
        rsi[window] = 100 - (100 / (1 + rs));

        for (int i = window + 1; i < prices.length; i++) {
            avgGain = ((window - 1) * avgGain + gains[i - 1]) / window;
            avgLoss = ((window - 1) * avgLoss + losses[i - 1]) / window;

            rs = avgGain / (avgLoss == 0 ? 1e-10 : avgLoss);
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
        double[] stdDevs = new double[prices.length];

        for (int i = window - 1; i < prices.length; i++) {
            double sumSquaredDiff = 0;
            for (int j = i - window + 1; j <= i; j++) {
                double diff = prices[j] - sma[i];
                sumSquaredDiff += diff * diff;
            }
            double stdDev = Math.sqrt(sumSquaredDiff / window);
            stdDevs[i] = stdDev;

            upper[i] = sma[i] + (numStd * stdDev);
            lower[i] = sma[i] - (numStd * stdDev);
        }

        Arrays.fill(upper, 0, window - 1, Double.NaN);
        Arrays.fill(lower, 0, window - 1, Double.NaN);

        Map<String, double[]> result = new HashMap<>();
        result.put("upper", upper);
        result.put("middle", sma);
        result.put("lower", lower);
        result.put("stdDev", stdDevs);

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

    public static Map<String, double[]> ichimokuCloud(double[] high, double[] low, double[] close) {
        return ichimokuCloud(high, low, close, 9, 26, 52);
    }

    public static Map<String, double[]> ichimokuCloud(double[] high, double[] low, double[] close,
                                                      int tenkanPeriod, int kijunPeriod, int chikouPeriod) {
        int max = Math.max(Math.max(tenkanPeriod, kijunPeriod), chikouPeriod);
        validateInputs(high, max);
        validateInputs(low, max);
        validateInputs(close, max);

        if (high.length != low.length || high.length != close.length) {
            throw new IllegalArgumentException("High, low, and close arrays must be of the same length");
        }

        int length = high.length;
        double[] tenkanSen = new double[length];
        double[] kijunSen = new double[length];
        double[] senkouSpanA = new double[length];
        double[] senkouSpanB = new double[length];
        double[] chikouSpan = new double[length];

        for (int i = tenkanPeriod - 1; i < length; i++) {
            double highestHigh = Double.NEGATIVE_INFINITY;
            double lowestLow = Double.POSITIVE_INFINITY;

            for (int j = 0; j < tenkanPeriod; j++) {
                highestHigh = Math.max(highestHigh, high[i - j]);
                lowestLow = Math.min(lowestLow, low[i - j]);
            }

            tenkanSen[i] = (highestHigh + lowestLow) / 2;
        }

        for (int i = kijunPeriod - 1; i < length; i++) {
            double highestHigh = Double.NEGATIVE_INFINITY;
            double lowestLow = Double.POSITIVE_INFINITY;

            for (int j = 0; j < kijunPeriod; j++) {
                highestHigh = Math.max(highestHigh, high[i - j]);
                lowestLow = Math.min(lowestLow, low[i - j]);
            }

            kijunSen[i] = (highestHigh + lowestLow) / 2;
        }

        for (int i = 0; i < length - kijunPeriod; i++) {
            int index = i + kijunPeriod - 1;
            if (index >= kijunPeriod - 1 && index >= tenkanPeriod - 1) {
                senkouSpanA[i + kijunPeriod] = (tenkanSen[index] + kijunSen[index]) / 2;
            }
        }

        for (int i = chikouPeriod - 1; i < length - kijunPeriod; i++) {
            double highestHigh = Double.NEGATIVE_INFINITY;
            double lowestLow = Double.POSITIVE_INFINITY;

            for (int j = 0; j < chikouPeriod; j++) {
                highestHigh = Math.max(highestHigh, high[i - j]);
                lowestLow = Math.min(lowestLow, low[i - j]);
            }

            senkouSpanB[i + kijunPeriod] = (highestHigh + lowestLow) / 2;
        }

        if (length - kijunPeriod >= 0)
            System.arraycopy(close, kijunPeriod, chikouSpan, 0, length - kijunPeriod);

        Arrays.fill(tenkanSen, 0, tenkanPeriod - 1, Double.NaN);
        Arrays.fill(kijunSen, 0, kijunPeriod - 1, Double.NaN);
        Arrays.fill(senkouSpanA, 0, 2 * kijunPeriod - 1, Double.NaN);
        Arrays.fill(senkouSpanB, 0, kijunPeriod + chikouPeriod - 1, Double.NaN);
        Arrays.fill(chikouSpan, length - kijunPeriod, length, Double.NaN);

        Map<String, double[]> result = new HashMap<>();
        result.put("tenkanSen", tenkanSen);
        result.put("kijunSen", kijunSen);
        result.put("senkouSpanA", senkouSpanA);
        result.put("senkouSpanB", senkouSpanB);
        result.put("chikouSpan", chikouSpan);

        return result;
    }

    public static double[] obv(double[] close, double[] volume) {
        validateInputs(close, 1);
        validateInputs(volume, 1);

        if (close.length != volume.length) {
            throw new IllegalArgumentException("Close price and volume arrays must be of the same length");
        }

        double[] obv = new double[close.length];
        obv[0] = volume[0];

        for (int i = 1; i < close.length; i++) {
            if (close[i] > close[i - 1]) {
                obv[i] = obv[i - 1] + volume[i];
            } else if (close[i] < close[i - 1]) {
                obv[i] = obv[i - 1] - volume[i];
            } else {
                obv[i] = obv[i - 1];
            }
        }

        return obv;
    }

    public static Map<String, double[]> pivotPoints(double[] high, double[] low, double[] close, double[] open, PivotType type) {
        validateInputs(high, 1);
        validateInputs(low, 1);
        validateInputs(close, 1);
        validateInputs(open, 1);

        if (high.length != low.length || high.length != close.length || high.length != open.length) {
            throw new IllegalArgumentException("High, low, close, and open arrays must be of the same length");
        }

        int length = high.length;

        double[] pp = new double[length];
        double[] r1 = new double[length];
        double[] r2 = new double[length];
        double[] r3 = new double[length];
        double[] s1 = new double[length];
        double[] s2 = new double[length];
        double[] s3 = new double[length];

        Arrays.fill(pp, 0, 1, Double.NaN);
        Arrays.fill(r1, 0, 1, Double.NaN);
        Arrays.fill(r2, 0, 1, Double.NaN);
        Arrays.fill(r3, 0, 1, Double.NaN);
        Arrays.fill(s1, 0, 1, Double.NaN);
        Arrays.fill(s2, 0, 1, Double.NaN);
        Arrays.fill(s3, 0, 1, Double.NaN);

        for (int i = 1; i < length; i++) {
            double prevHigh = high[i - 1];
            double prevLow = low[i - 1];
            double prevClose = close[i - 1];
            double prevOpen = open[i - 1];
            double range = prevHigh - prevLow;

            switch (type) {
                case STANDARD:
                    pp[i] = (prevHigh + prevLow + prevClose) / 3;
                    r1[i] = 2 * pp[i] - prevLow;
                    s1[i] = 2 * pp[i] - prevHigh;
                    r2[i] = pp[i] + range;
                    s2[i] = pp[i] - range;
                    r3[i] = prevHigh + 2 * (pp[i] - prevLow);
                    s3[i] = prevLow - 2 * (prevHigh - pp[i]);
                    break;

                case FIBONACCI:
                    pp[i] = (prevHigh + prevLow + prevClose) / 3;
                    r1[i] = pp[i] + 0.382 * range;
                    s1[i] = pp[i] - 0.382 * range;
                    r2[i] = pp[i] + 0.618 * range;
                    s2[i] = pp[i] - 0.618 * range;
                    r3[i] = pp[i] + range;
                    s3[i] = pp[i] - range;
                    break;

                case CAMARILLA:
                    pp[i] = (prevHigh + prevLow + prevClose) / 3;
                    double factor = 1.1 * range;
                    r1[i] = prevClose + factor / 12;
                    s1[i] = prevClose - factor / 12;
                    r2[i] = prevClose + factor / 6;
                    s2[i] = prevClose - factor / 6;
                    r3[i] = prevClose + factor / 4;
                    s3[i] = prevClose - factor / 4;
                    break;

                case WOODIE:
                    pp[i] = (prevHigh + prevLow + 2 * prevClose) / 4;
                    r1[i] = 2 * pp[i] - prevLow;
                    s1[i] = 2 * pp[i] - prevHigh;
                    r2[i] = pp[i] + range;
                    s2[i] = pp[i] - range;
                    r3[i] = prevHigh + 2 * (pp[i] - prevLow);
                    s3[i] = prevLow - 2 * (prevHigh - pp[i]);
                    break;

                case DEMARK:
                    double x;
                    if (prevClose < prevOpen) {
                        x = prevHigh + 2 * prevLow + prevClose;
                    } else if (prevClose > prevOpen) {
                        x = 2 * prevHigh + prevLow + prevClose;
                    } else {
                        x = prevHigh + prevLow + 2 * prevClose;
                    }
                    pp[i] = x / 4;
                    r1[i] = x / 2 - prevLow;
                    s1[i] = x / 2 - prevHigh;
                    r2[i] = pp[i] + (r1[i] - pp[i]);
                    s2[i] = pp[i] - (pp[i] - s1[i]);
                    r3[i] = r1[i] + (r1[i] - pp[i]);
                    s3[i] = s1[i] - (pp[i] - s1[i]);
                    break;

                default:
                    throw new IllegalArgumentException("Unsupported pivot point type: " + type);
            }
        }

        Map<String, double[]> result = new HashMap<>();
        result.put("PP", pp);
        result.put("R1", r1);
        result.put("R2", r2);
        result.put("R3", r3);
        result.put("S1", s1);
        result.put("S2", s2);
        result.put("S3", s3);

        return result;
    }

    public static Map<String, double[]> fibonacciRetracement(double[] high, double[] low, boolean isUptrend, int period) {
        validateInputs(high, period);
        validateInputs(low, period);

        if (high.length != low.length) {
            throw new IllegalArgumentException("High and low arrays must be of the same length");
        }

        int length = high.length;

        double[] levels = {0.0, 0.236, 0.382, 0.5, 0.618, 0.786, 1.0};
        Map<String, double[]> result = new HashMap<>();

        for (double level : levels) {
            result.put("level_" + level, new double[length]);
        }

        for (double level : levels) {
            Arrays.fill(result.get("level_" + level), 0, period, Double.NaN);
        }

        for (int i = period; i < length; i++) {
            double highestHigh = Double.NEGATIVE_INFINITY;
            double lowestLow = Double.POSITIVE_INFINITY;

            for (int j = i - period + 1; j <= i; j++) {
                highestHigh = Math.max(highestHigh, high[j]);
                lowestLow = Math.min(lowestLow, low[j]);
            }

            double range = highestHigh - lowestLow;

            for (double level : levels) {
                if (isUptrend) {
                    result.get("level_" + level)[i] = highestHigh - (range * level);
                } else {
                    result.get("level_" + level)[i] = lowestLow + (range * level);
                }
            }
        }

        return result;
    }

    public static Map<String, double[]> dmi(double[] high, double[] low, double[] close, int period) {
        validateInputs(high, period);
        validateInputs(low, period);
        validateInputs(close, period);

        if (high.length != low.length || high.length != close.length) {
            throw new IllegalArgumentException("High, low, and close arrays must be of the same length");
        }

        int length = high.length;

        double[] tr = new double[length];
        double[] plusDM = new double[length];
        double[] minusDM = new double[length];

        tr[0] = high[0] - low[0];
        plusDM[0] = 0;
        minusDM[0] = 0;

        for (int i = 1; i < length; i++) {
            double highLow = high[i] - low[i];
            double highClose = Math.abs(high[i] - close[i-1]);
            double lowClose = Math.abs(low[i] - close[i-1]);
            tr[i] = Math.max(highLow, Math.max(highClose, lowClose));

            double upMove = high[i] - high[i-1];
            double downMove = low[i-1] - low[i];

            if (upMove > downMove && upMove > 0) {
                plusDM[i] = upMove;
            } else {
                plusDM[i] = 0;
            }

            if (downMove > upMove && downMove > 0) {
                minusDM[i] = downMove;
            } else {
                minusDM[i] = 0;
            }
        }

        double[] trPeriod = new double[length];
        double[] plusDMPeriod = new double[length];
        double[] minusDMPeriod = new double[length];
        double[] plusDI = new double[length];
        double[] minusDI = new double[length];
        double[] dx = new double[length];
        double[] adx = new double[length];

        Arrays.fill(plusDI, Double.NaN);
        Arrays.fill(minusDI, Double.NaN);
        Arrays.fill(dx, Double.NaN);
        Arrays.fill(adx, Double.NaN);

        if (length >= period) {
            double sumTR = 0;
            double sumPlusDM = 0;
            double sumMinusDM = 0;

            for (int i = 0; i < period; i++) {
                sumTR += tr[i];
                sumPlusDM += plusDM[i];
                sumMinusDM += minusDM[i];
            }

            trPeriod[period-1] = sumTR;
            plusDMPeriod[period-1] = sumPlusDM;
            minusDMPeriod[period-1] = sumMinusDM;

            if (trPeriod[period-1] > 0) {
                plusDI[period-1] = 100 * (plusDMPeriod[period-1] / trPeriod[period-1]);
                minusDI[period-1] = 100 * (minusDMPeriod[period-1] / trPeriod[period-1]);
            } else {
                plusDI[period-1] = 0;
                minusDI[period-1] = 0;
            }

            double totalDI = plusDI[period - 1] + minusDI[period - 1];
            if (totalDI > 0) {
                dx[period-1] = 100 * Math.abs(plusDI[period-1] - minusDI[period-1]) / totalDI;
            } else {
                dx[period-1] = 0;
            }

            for (int i = period; i < length; i++) {
                trPeriod[i] = trPeriod[i-1] - (trPeriod[i-1] / period) + tr[i];
                plusDMPeriod[i] = plusDMPeriod[i-1] - (plusDMPeriod[i-1] / period) + plusDM[i];
                minusDMPeriod[i] = minusDMPeriod[i-1] - (minusDMPeriod[i-1] / period) + minusDM[i];

                if (trPeriod[i] > 0) {
                    plusDI[i] = 100 * (plusDMPeriod[i] / trPeriod[i]);
                    minusDI[i] = 100 * (minusDMPeriod[i] / trPeriod[i]);
                } else {
                    plusDI[i] = 0;
                    minusDI[i] = 0;
                }

                if (plusDI[i] + minusDI[i] > 0) {
                    dx[i] = 100 * Math.abs(plusDI[i] - minusDI[i]) / (plusDI[i] + minusDI[i]);
                } else {
                    dx[i] = 0;
                }
            }
        }

        if (length >= 2 * period - 1) {
            double sumDX = 0;
            for (int i = period - 1; i < 2 * period - 1; i++) {
                sumDX += dx[i];
            }

            adx[2 * period - 2] = sumDX / period;

            for (int i = 2 * period - 1; i < length; i++) {
                adx[i] = ((period - 1) * adx[i-1] + dx[i]) / period;
            }
        }

        Map<String, double[]> result = new HashMap<>();
        result.put("plusDI", plusDI);
        result.put("minusDI", minusDI);
        result.put("DX", dx);
        result.put("ADX", adx);
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