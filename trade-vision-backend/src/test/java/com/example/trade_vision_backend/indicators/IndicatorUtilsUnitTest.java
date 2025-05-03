package com.example.trade_vision_backend.indicators;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class IndicatorUtilsUnitTest {
    private static final double DELTA = 0.0001;

    @Test
    @DisplayName("Test Simple Moving Average (SMA)")
    public void testSMA() {
        double[] prices = {10, 11, 12, 13, 14, 15, 16, 17, 18, 19};
        double[] expected = {Double.NaN, Double.NaN, 11, 12, 13, 14, 15, 16, 17, 18};
        double[] result = IndicatorUtils.sma(prices, 3);

        for (int i = 0; i < prices.length; i++) {
            if (Double.isNaN(expected[i])) {
                assertTrue(Double.isNaN(result[i]));
            } else {
                assertEquals(expected[i], result[i], DELTA);
            }
        }

        double[] resultEdge = IndicatorUtils.sma(prices, prices.length);
        assertEquals(14.5, resultEdge[prices.length - 1], DELTA);
        for (int i = 0; i < prices.length - 1; i++) {
            assertTrue(Double.isNaN(resultEdge[i]));
        }

        assertThrows(IllegalArgumentException.class, () -> IndicatorUtils.sma(null, 3));
        assertThrows(IllegalArgumentException.class, () -> IndicatorUtils.sma(prices, 0));
        assertThrows(IllegalArgumentException.class, () -> IndicatorUtils.sma(new double[0], 3));
    }

    @Test
    @DisplayName("Test Exponential Moving Average (EMA)")
    public void testEMA() {
        double[] prices = {10, 11, 12, 13, 14, 15, 16, 17, 18, 19};
        double[] result = IndicatorUtils.ema(prices, 3);

        assertTrue(Double.isNaN(result[0]));
        assertTrue(Double.isNaN(result[1]));

        assertEquals(11, result[2], DELTA);
        assertEquals(12, result[3], DELTA);
        assertEquals(13, result[4], DELTA);

        double[] smallPrices = {10, 11};
        double[] smallResult = IndicatorUtils.ema(smallPrices, 3);
        assertEquals(10, smallResult[0], DELTA);
        assertEquals(10.5, smallResult[1], DELTA);
    }

    @Test
    @DisplayName("Test Relative Strength Index (RSI) with Clear Price Movements")
    public void testRSI() {
        double[] prices = {
                50.0, 51.0, 52.5, 53.1, 54.2, 53.5, 52.0, 53.0, 54.5, 55.0,
                56.8, 57.9, 58.5, 57.2, 56.0, 57.5, 58.0, 57.0, 56.5, 57.0,
                58.5, 59.0, 60.2, 61.5, 60.0, 59.5, 61.0, 62.5, 63.0, 62.0
        };

        final double DELTA = 0.01;
        double[] defaultResult = IndicatorUtils.rsi(prices);

        for (int i = 0; i < 14; i++) {
            assertTrue(Double.isNaN(defaultResult[i]));
        }

        assertEquals(69.48, defaultResult[14], DELTA);
        assertEquals(72.37793851717902, defaultResult[15], DELTA);
        assertEquals(73.288270110304, defaultResult[16], DELTA);
        assertEquals(68.43080326950204, defaultResult[17], DELTA);
        assertEquals(66.0727561199989, defaultResult[18], DELTA);
        assertEquals(67.28673039258629, defaultResult[19], DELTA);
        assertEquals(70.67658720871765, defaultResult[20], DELTA);

        double[] smallWindowResult = IndicatorUtils.rsi(prices, 5);

        for (int i = 0; i < 5; i++) {
            assertTrue(Double.isNaN(smallWindowResult[i]));
        }

        assertEquals(85.71428571428567, smallWindowResult[5], DELTA);
        assertEquals(61.992619926199254, smallWindowResult[6], DELTA);
        assertEquals(69.11544227886054, smallWindowResult[7], DELTA);
        assertEquals(77.1460269033421, smallWindowResult[8], DELTA);
        assertEquals(79.38002439863617, smallWindowResult[9], DELTA);

        double[] smallPrices = {10, 11};
        assertThrows(IllegalArgumentException.class, () -> IndicatorUtils.rsi(smallPrices, 3));
    }

    @Test
    @DisplayName("Test Moving Average Convergence Divergence (MACD)")
    public void testMACD() {
        double[] prices = {10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30,
                31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45};

        Map<String, double[]> defaultResult = IndicatorUtils.macd(prices);

        int slowPeriod = 26;
        assertTrue(Double.isNaN(defaultResult.get("macdLine")[slowPeriod - 2]));
        assertFalse(Double.isNaN(defaultResult.get("macdLine")[slowPeriod - 1]));

        Map<String, double[]> customResult = IndicatorUtils.macd(prices, 5, 10, 3);

        int signalPeriod = 3;
        int startIdx = 10 + signalPeriod - 1;

        for (int i = startIdx; i < prices.length; i++) {
            assertEquals(
                    customResult.get("macdLine")[i] - customResult.get("signalLine")[i],
                    customResult.get("histogram")[i],
                    DELTA
            );
        }

        assertThrows(IllegalArgumentException.class, () -> IndicatorUtils.macd(prices, 26, 12, 9));
    }

    @Test
    @DisplayName("Test Average True Range (ATR)")
    public void testATR() {
        double[] high = {10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24};
        double[] low = {9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23};
        double[] close = {9.5, 10.5, 11.5, 12.5, 13.5, 14.5, 15.5, 16.5, 17.5, 18.5, 19.5, 20.5, 21.5, 22.5, 23.5};

        double[] defaultResult = IndicatorUtils.atr(high, low, close);

        assertTrue(Double.isNaN(defaultResult[12]));

        assertFalse(Double.isNaN(defaultResult[13]));

        double[] customResult = IndicatorUtils.atr(high, low, close, 5);
        assertTrue(Double.isNaN(customResult[3]));
        assertFalse(Double.isNaN(customResult[4]));

        assertEquals(1.4, customResult[4], DELTA);

        assertThrows(IllegalArgumentException.class, () -> IndicatorUtils.atr(high, low, new double[5], 5));
    }

    @Test
    @DisplayName("Test Bollinger Bands")
    public void testBollingerBands() {
        double[] prices = {10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30};

        Map<String, double[]> defaultResult = IndicatorUtils.bollingerBands(prices);

        assertTrue(Double.isNaN(defaultResult.get("upper")[18]));
        assertTrue(Double.isNaN(defaultResult.get("middle")[18]));
        assertTrue(Double.isNaN(defaultResult.get("lower")[18]));

        assertFalse(Double.isNaN(defaultResult.get("upper")[19]));
        assertFalse(Double.isNaN(defaultResult.get("middle")[19]));
        assertFalse(Double.isNaN(defaultResult.get("lower")[19]));

        Map<String, double[]> customResult = IndicatorUtils.bollingerBands(prices, 5, 1.0);

        for (int i = 4; i < prices.length; i++) {
            double expectedSMA = (prices[i-4] + prices[i-3] + prices[i-2] + prices[i-1] + prices[i]) / 5;
            assertEquals(expectedSMA, customResult.get("middle")[i], DELTA);

            double stdDev = customResult.get("stdDev")[i];
            assertEquals(customResult.get("middle")[i] + stdDev, customResult.get("upper")[i], DELTA);
            assertEquals(customResult.get("middle")[i] - stdDev, customResult.get("lower")[i], DELTA);
        }
    }

    @Test
    @DisplayName("Test Rate of Change (ROC)")
    public void testROC() {
        double[] prices = {10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20};
        double[] result = IndicatorUtils.roc(prices, 3);

        assertTrue(Double.isNaN(result[2]));

        assertEquals(30.0, result[3], DELTA);
        assertEquals(27.27, result[4], 0.01);
        assertEquals(25.0, result[5], DELTA);
    }

    @Test
    @DisplayName("Test Stochastic Oscillator")
    public void testStochastic() {
        double[] high =  {15, 16, 15, 16, 16, 15, 15, 16, 16, 15};
        double[] low =   {10, 10, 11, 11, 10, 10, 11, 10, 10, 11};
        double[] close = {12, 13, 12, 14, 13, 12, 13, 15, 14, 13};

        Map<String, double[]> result = IndicatorUtils.stochastic(high, low, close, 5, 3);

        assertTrue(Double.isNaN(result.get("%K")[3]));
        assertFalse(Double.isNaN(result.get("%K")[4]));
        assertFalse(Double.isNaN(result.get("%D")[6]));
        assertFalse(Double.isNaN(result.get("%D")[7]));

        assertEquals(50.0, result.get("%K")[4], DELTA);

        double expectedD = (result.get("%K")[5] + result.get("%K")[6] + result.get("%K")[7]) / 3;
        assertEquals(expectedD, result.get("%D")[7], DELTA);

        assertThrows(IllegalArgumentException.class,
                () -> IndicatorUtils.stochastic(high, low, new double[5], 5, 3));
    }

    @Test
    @DisplayName("Test Ichimoku Cloud")
    public void testIchimokuCloud() {
        int size = 100;
        double[] high = new double[size];
        double[] low = new double[size];
        double[] close = new double[size];

        for (int i = 0; i < size; i++) {
            high[i] = i + 10;
            low[i] = i + 9;
            close[i] = i + 9.5;
        }

        Map<String, double[]> defaultResult = IndicatorUtils.ichimokuCloud(high, low, close);

        for (int i = 0; i < 8; i++) {
            assertTrue(Double.isNaN(defaultResult.get("tenkanSen")[i]), "tenkanSen at index " + i + " should be NaN");
        }

        assertFalse(Double.isNaN(defaultResult.get("tenkanSen")[8]), "tenkanSen at index 8 should not be NaN");
        assertEquals(13.5, defaultResult.get("tenkanSen")[8], 0.0001);

        for (int i = 0; i < 25; i++) {
            assertTrue(Double.isNaN(defaultResult.get("kijunSen")[i]), "kijunSen at index " + i + " should be NaN");
        }

        assertFalse(Double.isNaN(defaultResult.get("kijunSen")[25]), "kijunSen at index 25 should not be NaN");
        assertEquals(22.0, defaultResult.get("kijunSen")[25], 0.0001);

        for (int i = 0; i < 51; i++) {
            assertTrue(Double.isNaN(defaultResult.get("senkouSpanA")[i]), "senkouSpanA at index " + i + " should be NaN");
        }

        assertFalse(Double.isNaN(defaultResult.get("senkouSpanA")[51]), "senkouSpanA at index 51 should not be NaN");

        for (int i = 0; i < 77; i++) {
            assertTrue(Double.isNaN(defaultResult.get("senkouSpanB")[i]), "senkouSpanB at index " + i + " should be NaN");
        }

        assertFalse(Double.isNaN(defaultResult.get("senkouSpanB")[77]), "senkouSpanB at index 77 should not be NaN");


        Map<String, double[]> customResult = IndicatorUtils.ichimokuCloud(high, low, close, 3, 6, 12);


        assertTrue(Double.isNaN(customResult.get("tenkanSen")[1]), "Custom tenkanSen at index 1 should be NaN");
        assertFalse(Double.isNaN(customResult.get("tenkanSen")[2]), "Custom tenkanSen at index 2 should not be NaN");
        assertEquals(10.5, customResult.get("tenkanSen")[2], 0.0001);

        assertTrue(Double.isNaN(customResult.get("kijunSen")[4]), "Custom kijunSen at index 4 should be NaN");
        assertFalse(Double.isNaN(customResult.get("kijunSen")[5]), "Custom kijunSen at index 5 should not be NaN");
        assertEquals(12.0, customResult.get("kijunSen")[5], 0.0001);

        assertTrue(Double.isNaN(customResult.get("senkouSpanA")[10]), "Custom senkouSpanA at index 10 should be NaN");
        assertFalse(Double.isNaN(customResult.get("senkouSpanA")[11]), "Custom senkouSpanA at index 11 should not be NaN");

        assertFalse(Double.isNaN(customResult.get("senkouSpanB")[17]), "Custom senkouSpanB at index 17 should not be NaN");
        assertFalse(Double.isNaN(customResult.get("senkouSpanB")[18]), "Custom senkouSpanB at index 18 should not be NaN");

        assertTrue(Double.isNaN(customResult.get("chikouSpan")[size - 1]), "chikouSpan at last index should be NaN");
        assertFalse(Double.isNaN(customResult.get("chikouSpan")[size - 7]), "chikouSpan at size-7 should not be NaN");
    }

    @Test
    @DisplayName("Test On-Balance Volume (OBV)")
    public void testOBV() {
        double[] close = {10, 11, 10.5, 11.5, 11.75, 11.5, 12, 12.5, 12.25};
        double[] volume = {1000, 1500, 1200, 1400, 1300, 1000, 1100, 1200, 900};

        double[] result = IndicatorUtils.obv(close, volume);

        assertEquals(1000, result[0], DELTA);
        assertEquals(1000 + 1500, result[1], DELTA);
        assertEquals(2500 - 1200, result[2], DELTA);
        assertEquals(1300 + 1400, result[3], DELTA);

        double expectedObv = 1000;
        double[] expectedResult = new double[close.length];
        expectedResult[0] = expectedObv;

        for (int i = 1; i < close.length; i++) {
            if (close[i] > close[i-1]) {
                expectedObv += volume[i];
            } else if (close[i] < close[i-1]) {
                expectedObv -= volume[i];
            }
            expectedResult[i] = expectedObv;
        }

        for (int i = 0; i < close.length; i++) {
            assertEquals(expectedResult[i], result[i], DELTA);
        }

        assertThrows(IllegalArgumentException.class, () -> IndicatorUtils.obv(close, new double[5]));
    }

    @Test
    @DisplayName("Test Pivot Points")
    public void testPivotPoints() {
        double[] high = {12, 13, 14, 15};
        double[] low = {8, 9, 10, 11};
        double[] close = {10, 11, 12, 13};
        double[] open = {9, 10, 11, 12};

        Map<String, double[]> standardResult = IndicatorUtils.pivotPoints(high, low, close, open, PivotType.STANDARD);

        assertTrue(Double.isNaN(standardResult.get("PP")[0]));

        assertEquals(10, standardResult.get("PP")[1], DELTA);
        assertEquals(12, standardResult.get("R1")[1], DELTA);
        assertEquals(8, standardResult.get("S1")[1], DELTA);

        Map<String, double[]> fibResult = IndicatorUtils.pivotPoints(high, low, close, open, PivotType.FIBONACCI);

        assertEquals(standardResult.get("PP")[1], fibResult.get("PP")[1], DELTA);
        assertEquals(11.528, fibResult.get("R1")[1], DELTA);

        assertThrows(IllegalArgumentException.class,
                () -> IndicatorUtils.pivotPoints(high, low, close, new double[2], PivotType.STANDARD));
    }

    @Test
    @DisplayName("Test Fibonacci Retracement")
    public void testFibonacciRetracement() {
        double[] high = {10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20};
        double[] low = {8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18};

        Map<String, double[]> uptrendResult = IndicatorUtils.fibonacciRetracement(high, low, true, 5);

        assertTrue(Double.isNaN(uptrendResult.get("level_0.0")[4]));
        assertFalse(Double.isNaN(uptrendResult.get("level_0.0")[5]));

        assertEquals(15, uptrendResult.get("level_0.0")[5], DELTA);
        assertEquals(12, uptrendResult.get("level_0.5")[5], DELTA);
        assertEquals(9, uptrendResult.get("level_1.0")[5], DELTA);

        Map<String, double[]> downtrendResult = IndicatorUtils.fibonacciRetracement(high, low, false, 5);

        assertEquals(9, downtrendResult.get("level_0.0")[5], DELTA);
        assertEquals(12, downtrendResult.get("level_0.5")[5], DELTA);
        assertEquals(15, downtrendResult.get("level_1.0")[5], DELTA);
    }

    @Test
    @DisplayName("Test Directional Movement Index (DMI)")
    public void testDMI() {
        double[] high = {10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26};
        double[] low = {8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24};
        double[] close = {9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25};

        Map<String, double[]> result = IndicatorUtils.dmi(high, low, close, 5);

        assertTrue(Double.isNaN(result.get("plusDI")[3]));
        assertTrue(Double.isNaN(result.get("minusDI")[3]));
        assertTrue(Double.isNaN(result.get("DX")[3]));

        assertFalse(Double.isNaN(result.get("plusDI")[4]));
        assertFalse(Double.isNaN(result.get("minusDI")[4]));
        assertFalse(Double.isNaN(result.get("DX")[4]));

        assertEquals(40, result.get("plusDI")[4], DELTA);
        assertEquals(0, result.get("minusDI")[4], DELTA);

        assertEquals(100, result.get("DX")[4], DELTA);

        assertTrue(Double.isNaN(result.get("ADX")[7]));
        assertFalse(Double.isNaN(result.get("ADX")[8]));
        assertFalse(Double.isNaN(result.get("ADX")[9]));

        assertEquals(100, result.get("ADX")[9], DELTA);
    }
}