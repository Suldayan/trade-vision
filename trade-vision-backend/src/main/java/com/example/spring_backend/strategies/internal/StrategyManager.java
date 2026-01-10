package com.example.spring_backend.strategies.internal;

import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class StrategyManager {

    @Nonnull
    public List<StrategyModel> getAllStrategies() {
        List<StrategyModel> strategies = new ArrayList<>();

        strategies.add(createRsiThresholdStrategy());
        strategies.add(createSmaStrategy());
        strategies.add(createMacdStrategy());
        strategies.add(createBollingerBandsStrategy());
        strategies.add(createAtrStrategy());
        strategies.add(createStochasticStrategy());
        strategies.add(createDmiStrategy());
        strategies.add(createIchimokuStrategy());
        strategies.add(createObvStrategy());
        strategies.add(createPivotPointsStrategy());
        strategies.add(createRocStrategy());
        strategies.add(createRocCrossoverStrategy());
        strategies.add(createRocDivergenceStrategy());
        strategies.add(createFibonacciStrategy());

        return strategies;
    }

    @Nonnull
    private StrategyModel createRsiThresholdStrategy() {
        Map<String, StrategyModel.ParameterDefinition> params = new HashMap<>();
        params.put("period", new StrategyModel.ParameterDefinition("number", 14, "Period"));
        params.put("upperThreshold", new StrategyModel.ParameterDefinition("number", 70, "Upper Threshold"));
        params.put("lowerThreshold", new StrategyModel.ParameterDefinition("number", 30, "Lower Threshold"));
        params.put("checkOverbought", new StrategyModel.ParameterDefinition("boolean", false, "Check Overbought"));

        return new StrategyModel("RSI_THRESHOLD", "RSI Threshold",
                "Triggers when RSI crosses above or below specified threshold levels", params);
    }

    @Nonnull
    private StrategyModel createSmaStrategy() {
        Map<String, StrategyModel.ParameterDefinition> params = new HashMap<>();
        params.put("fastPeriod", new StrategyModel.ParameterDefinition("number", 5, "Fast Period"));
        params.put("slowPeriod", new StrategyModel.ParameterDefinition("number", 20, "Slow Period"));
        params.put("crossAbove", new StrategyModel.ParameterDefinition("boolean", true, "Cross Above"));

        return new StrategyModel("SMA_CROSSOVER", "SMA Crossover",
                "Signals when fast moving average crosses above or below slow moving average", params);
    }

    @Nonnull
    private StrategyModel createMacdStrategy() {
        Map<String, StrategyModel.ParameterDefinition> params = new HashMap<>();
        params.put("fastPeriod", new StrategyModel.ParameterDefinition("number", 12, "Fast Period"));
        params.put("slowPeriod", new StrategyModel.ParameterDefinition("number", 26, "Slow Period"));
        params.put("signalPeriod", new StrategyModel.ParameterDefinition("number", 9, "Signal Period"));
        params.put("crossAbove", new StrategyModel.ParameterDefinition("boolean", true, "Cross Above"));

        return new StrategyModel("MACD_CROSSOVER", "MACD Crossover",
                "Detects when MACD line crosses above or below the signal line", params);
    }

    @Nonnull
    private StrategyModel createBollingerBandsStrategy() {
        Map<String, StrategyModel.ParameterDefinition> params = new HashMap<>();
        params.put("period", new StrategyModel.ParameterDefinition("number", 20, "Period"));
        params.put("numStd", new StrategyModel.ParameterDefinition("number", 2.0, "Standard Deviations", 0.1));
        params.put("checkUpper", new StrategyModel.ParameterDefinition("boolean", false, "Check Upper Band"));

        return new StrategyModel("BOLLINGER_BANDS", "Bollinger Bands",
                "Triggers when price touches or crosses Bollinger Band boundaries", params);
    }

    @Nonnull
    private StrategyModel createAtrStrategy() {
        Map<String, StrategyModel.ParameterDefinition> params = new HashMap<>();
        params.put("period", new StrategyModel.ParameterDefinition("number", 14, "Period"));
        params.put("multiplier", new StrategyModel.ParameterDefinition("number", 1.5, "Multiplier", 0.1));
        params.put("isAbove", new StrategyModel.ParameterDefinition("boolean", true, "Above Threshold"));
        params.put("compareWithPrice", new StrategyModel.ParameterDefinition("boolean", true, "Compare with Price Movement"));

        return new StrategyModel("ATR", "ATR Condition",
                "Measures volatility using Average True Range for trend strength", params);
    }

    @Nonnull
    private StrategyModel createStochasticStrategy() {
        Map<String, StrategyModel.ParameterDefinition> params = new HashMap<>();
        params.put("kPeriod", new StrategyModel.ParameterDefinition("number", 14, "K Period"));
        params.put("dPeriod", new StrategyModel.ParameterDefinition("number", 3, "D Period"));
        params.put("upperThreshold", new StrategyModel.ParameterDefinition("number", 80, "Upper Threshold"));
        params.put("lowerThreshold", new StrategyModel.ParameterDefinition("number", 20, "Lower Threshold"));
        params.put("checkOverbought", new StrategyModel.ParameterDefinition("boolean", false, "Check Overbought"));

        return new StrategyModel("STOCHASTIC", "Stochastic Oscillator",
                "Identifies overbought/oversold conditions using momentum oscillator", params);
    }

    @Nonnull
    private StrategyModel createDmiStrategy() {
        Map<String, StrategyModel.ParameterDefinition> params = new HashMap<>();
        params.put("period", new StrategyModel.ParameterDefinition("number", 14, "Period"));

        List<StrategyModel.ParameterOption> options = Arrays.asList(
                new StrategyModel.ParameterOption("PLUS_DI_ABOVE_MINUS_DI", "+DI Above -DI"),
                new StrategyModel.ParameterOption("MINUS_DI_ABOVE_PLUS_DI", "-DI Above +DI"),
                new StrategyModel.ParameterOption("PLUS_DI_CROSSES_ABOVE_MINUS_DI", "+DI Crosses Above -DI"),
                new StrategyModel.ParameterOption("MINUS_DI_CROSSES_ABOVE_PLUS_DI", "-DI Crosses Above +DI"),
                new StrategyModel.ParameterOption("ADX_ABOVE_THRESHOLD", "ADX Above Threshold"),
                new StrategyModel.ParameterOption("ADX_BELOW_THRESHOLD", "ADX Below Threshold"),
                new StrategyModel.ParameterOption("ADX_RISING", "ADX Rising"),
                new StrategyModel.ParameterOption("ADX_FALLING", "ADX Falling"),
                new StrategyModel.ParameterOption("STRONG_TREND", "Strong Trend"),
                new StrategyModel.ParameterOption("STRONG_BULLISH", "Strong Bullish"),
                new StrategyModel.ParameterOption("STRONG_BEARISH", "Strong Bearish"),
                new StrategyModel.ParameterOption("WEAK_TREND", "Weak Trend"),
                new StrategyModel.ParameterOption("DI_DIVERGENCE", "DI Divergence")
        );
        params.put("signalType", new StrategyModel.ParameterDefinition("string", "PLUS_DI_ABOVE_MINUS_DI", "Signal Type", options));
        params.put("threshold", new StrategyModel.ParameterDefinition("number", 25.0, "ADX Threshold", 0.1));
        params.put("divergenceThreshold", new StrategyModel.ParameterDefinition("number", 10.0, "Divergence Threshold", 0.1));

        return new StrategyModel("DMI", "DMI/ADX Condition",
                "Analyzes trend strength and direction using Directional Movement Index", params);
    }

    @Nonnull
    private StrategyModel createIchimokuStrategy() {
        Map<String, StrategyModel.ParameterDefinition> params = new HashMap<>();
        params.put("tenkanPeriod", new StrategyModel.ParameterDefinition("number", 9, "Tenkan Period"));
        params.put("kijunPeriod", new StrategyModel.ParameterDefinition("number", 26, "Kijun Period"));
        params.put("chikouPeriod", new StrategyModel.ParameterDefinition("number", 52, "Chikou Period"));

        List<StrategyModel.ParameterOption> options = Arrays.asList(
                new StrategyModel.ParameterOption("TENKAN_CROSSES_ABOVE_KIJUN", "Tenkan Crosses Above Kijun"),
                new StrategyModel.ParameterOption("TENKAN_CROSSES_BELOW_KIJUN", "Tenkan Crosses Below Kijun"),
                new StrategyModel.ParameterOption("PRICE_ABOVE_CLOUD", "Price Above Cloud"),
                new StrategyModel.ParameterOption("PRICE_BELOW_CLOUD", "Price Below Cloud"),
                new StrategyModel.ParameterOption("PRICE_IN_CLOUD", "Price In Cloud"),
                new StrategyModel.ParameterOption("BULLISH_CLOUD", "Bullish Cloud"),
                new StrategyModel.ParameterOption("BEARISH_CLOUD", "Bearish Cloud"),
                new StrategyModel.ParameterOption("CHIKOU_ABOVE_PRICE", "Chikou Above Price"),
                new StrategyModel.ParameterOption("CHIKOU_BELOW_PRICE", "Chikou Below Price"),
                new StrategyModel.ParameterOption("STRONG_BULLISH", "Strong Bullish"),
                new StrategyModel.ParameterOption("STRONG_BEARISH", "Strong Bearish")
        );
        params.put("signalType", new StrategyModel.ParameterDefinition("string", "PRICE_ABOVE_CLOUD", "Signal Type", options));

        return new StrategyModel("ICHIMOKU_CLOUD", "Ichimoku Cloud",
                "Comprehensive trend analysis using multiple Ichimoku components", params);
    }

    @Nonnull
    private StrategyModel createObvStrategy() {
        Map<String, StrategyModel.ParameterDefinition> params = new HashMap<>();
        params.put("period", new StrategyModel.ParameterDefinition("number", 20, "Period"));

        List<StrategyModel.ParameterOption> options = Arrays.asList(
                new StrategyModel.ParameterOption("ABOVE_MA", "Above Moving Average"),
                new StrategyModel.ParameterOption("BELOW_MA", "Below Moving Average"),
                new StrategyModel.ParameterOption("CROSS_ABOVE_MA", "Cross Above Moving Average"),
                new StrategyModel.ParameterOption("CROSS_BELOW_MA", "Cross Below Moving Average"),
                new StrategyModel.ParameterOption("INCREASING", "Increasing"),
                new StrategyModel.ParameterOption("DECREASING", "Decreasing")
        );
        params.put("conditionType", new StrategyModel.ParameterDefinition("string", "ABOVE_MA", "Condition Type", options));

        return new StrategyModel("OBV", "On-Balance Volume",
                "Tracks volume flow to predict price movements and confirm trends", params);
    }

    @Nonnull
    private StrategyModel createPivotPointsStrategy() {
        Map<String, StrategyModel.ParameterDefinition> params = new HashMap<>();

        List<StrategyModel.ParameterOption> typeOptions = Arrays.asList(
                new StrategyModel.ParameterOption("STANDARD", "Standard"),
                new StrategyModel.ParameterOption("FIBONACCI", "Fibonacci"),
                new StrategyModel.ParameterOption("WOODIE", "Woodie"),
                new StrategyModel.ParameterOption("CAMARILLA", "Camarilla"),
                new StrategyModel.ParameterOption("DEMARK", "DeMark")
        );
        params.put("pivotType", new StrategyModel.ParameterDefinition("string", "STANDARD", "Pivot Type", typeOptions));

        List<StrategyModel.ParameterOption> levelOptions = Arrays.asList(
                new StrategyModel.ParameterOption("PP", "Pivot Point"),
                new StrategyModel.ParameterOption("R1", "Resistance 1"),
                new StrategyModel.ParameterOption("R2", "Resistance 2"),
                new StrategyModel.ParameterOption("R3", "Resistance 3"),
                new StrategyModel.ParameterOption("S1", "Support 1"),
                new StrategyModel.ParameterOption("S2", "Support 2"),
                new StrategyModel.ParameterOption("S3", "Support 3")
        );
        params.put("pivotLevel", new StrategyModel.ParameterDefinition("string", "PP", "Pivot Level", levelOptions));
        params.put("crossAbove", new StrategyModel.ParameterDefinition("boolean", true, "Cross Above"));
        params.put("useClose", new StrategyModel.ParameterDefinition("boolean", true, "Use Close Price"));

        return new StrategyModel("PIVOT_POINTS", "Pivot Points",
                "Identifies key support and resistance levels based on previous period", params);
    }

    @Nonnull
    private StrategyModel createRocStrategy() {
        Map<String, StrategyModel.ParameterDefinition> params = new HashMap<>();
        params.put("period", new StrategyModel.ParameterDefinition("number", 12, "Period"));
        params.put("threshold", new StrategyModel.ParameterDefinition("number", 5.0, "Threshold %", 0.1));

        List<StrategyModel.ParameterOption> options = Arrays.asList(
                new StrategyModel.ParameterOption("ABOVE", "Above"),
                new StrategyModel.ParameterOption("BELOW", "Below"),
                new StrategyModel.ParameterOption("EQUAL", "Equal"),
                new StrategyModel.ParameterOption("CROSSING_ABOVE", "Crossing Above"),
                new StrategyModel.ParameterOption("CROSSING_BELOW", "Crossing Below")
        );
        params.put("direction", new StrategyModel.ParameterDefinition("string", "ABOVE", "Direction", options));

        return new StrategyModel("ROC", "Rate of Change",
                "Measures momentum by comparing current price to price N periods ago", params);
    }

    @Nonnull
    private StrategyModel createRocCrossoverStrategy() {
        Map<String, StrategyModel.ParameterDefinition> params = new HashMap<>();
        params.put("period", new StrategyModel.ParameterDefinition("number", 12, "Period"));
        params.put("threshold", new StrategyModel.ParameterDefinition("number", 0.0, "Threshold %", 0.1));
        params.put("crossAbove", new StrategyModel.ParameterDefinition("boolean", true, "Cross Above"));

        return new StrategyModel("ROC_CROSSOVER", "ROC Crossover",
                "Detects when Rate of Change crosses above or below threshold level", params);
    }

    @Nonnull
    private StrategyModel createRocDivergenceStrategy() {
        Map<String, StrategyModel.ParameterDefinition> params = new HashMap<>();
        params.put("period", new StrategyModel.ParameterDefinition("number", 12, "ROC Period"));
        params.put("divergencePeriod", new StrategyModel.ParameterDefinition("number", 20, "Divergence Period"));
        params.put("bullish", new StrategyModel.ParameterDefinition("boolean", true, "Bullish Divergence"));

        return new StrategyModel("ROC_DIVERGENCE", "ROC Divergence",
                "Identifies bullish/bearish divergences between price and ROC momentum", params);
    }

    @Nonnull
    private StrategyModel createFibonacciStrategy() {
        Map<String, StrategyModel.ParameterDefinition> params = new HashMap<>();
        params.put("lookbackPeriod", new StrategyModel.ParameterDefinition("number", 50, "Lookback Period"));

        List<StrategyModel.ParameterOption> options = Arrays.asList(
                new StrategyModel.ParameterOption("0.236", "23.6%"),
                new StrategyModel.ParameterOption("0.382", "38.2%"),
                new StrategyModel.ParameterOption("0.5", "50%"),
                new StrategyModel.ParameterOption("0.618", "61.8%"),
                new StrategyModel.ParameterOption("0.786", "78.6%")
        );
        params.put("level", new StrategyModel.ParameterDefinition("number", 0.618, "Fibonacci Level", options));
        params.put("isBullish", new StrategyModel.ParameterDefinition("boolean", true, "Bullish Retracement"));
        params.put("tolerance", new StrategyModel.ParameterDefinition("number", 0.01, "Tolerance", 0.001));

        return new StrategyModel("FIBONACCI_RETRACEMENT", "Fibonacci Retracement",
                "Identifies potential support/resistance levels using Fibonacci ratios", params);
    }

    public StrategyModel getStrategy(@Nonnull String key) {
        return getAllStrategies().stream()
                .filter(strategy -> strategy.getKey().equals(key))
                .findFirst()
                .orElse(null);
    }
}