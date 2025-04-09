package com.example.trade_vision_backend.indicators;

import java.util.Map;

public interface IndicatorService {

    /**
     * Calculate Simple Moving Average (SMA)
     * @param prices array of price data
     * @param window the period for calculation
     * @return array of SMA values (first window-1 values will be NaN)
     */
    double[] calculateSMA(double[] prices, int window);

    /**
     * Calculate Exponential Moving Average (EMA)
     * @param prices array of price data
     * @param window the period for calculation
     * @return array of EMA values
     */
    double[] calculateEMA(double[] prices, int window);

    /**
     * Calculate Relative Strength Index (RSI)
     * @param prices array of price data
     * @param window the period for calculation (default 14)
     * @return array of RSI values
     */
    double[] calculateRSI(double[] prices, int window);

    /**
     * Calculate Moving Average Convergence Divergence (MACD)
     * @param prices array of price data
     * @param fast fast EMA period (default 12)
     * @param slow slow EMA period (default 26)
     * @param signal signal line period (default 9)
     * @return Map containing 'macdLine', 'signalLine', and 'histogram' arrays
     */
    Map<String, double[]> calculateMACD(double[] prices, int fast, int slow, int signal);

    /**
     * Calculate Average True Range (ATR)
     * @param high array of high prices
     * @param low array of low prices
     * @param close array of close prices
     * @param window the period for calculation (default 14)
     * @return array of ATR values
     */
    double[] calculateATR(double[] high, double[] low, double[] close, int window);

    /**
     * Calculate Bollinger Bands
     * @param prices array of price data
     * @param window the period for calculation (default 20)
     * @param numStd number of standard deviations (default 2)
     * @return Map containing 'upper', 'middle', and 'lower' band arrays
     */
    Map<String, double[]> calculateBollingerBands(double[] prices, int window, double numStd);
}