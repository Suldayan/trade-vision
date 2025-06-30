package com.example.trade_vision_backend.strategies.internal;

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

        // RSI_THRESHOLD
        Map<String, StrategyModel.ParameterDefinition> rsiParams = new HashMap<>();
        rsiParams.put("period", new StrategyModel.ParameterDefinition("number", 14, "Period"));
        rsiParams.put("upperThreshold", new StrategyModel.ParameterDefinition("number", 70, "Upper Threshold"));
        rsiParams.put("lowerThreshold", new StrategyModel.ParameterDefinition("number", 30, "Lower Threshold"));
        rsiParams.put("checkOverbought", new StrategyModel.ParameterDefinition("boolean", false, "Check Overbought"));

        strategies.add(new StrategyModel("RSI_THRESHOLD", "RSI Threshold",
                "Triggers when RSI crosses above or below specified threshold levels", rsiParams));

        // SMA_CROSSOVER
        Map<String, StrategyModel.ParameterDefinition> smaParams = new HashMap<>();
        smaParams.put("fastPeriod", new StrategyModel.ParameterDefinition("number", 5, "Fast Period"));
        smaParams.put("slowPeriod", new StrategyModel.ParameterDefinition("number", 20, "Slow Period"));
        smaParams.put("crossAbove", new StrategyModel.ParameterDefinition("boolean", true, "Cross Above"));

        strategies.add(new StrategyModel("SMA_CROSSOVER", "SMA Crossover",
                "Signals when fast moving average crosses above or below slow moving average", smaParams));

        // MACD_CROSSOVER
        Map<String, StrategyModel.ParameterDefinition> macdParams = new HashMap<>();
        macdParams.put("fastPeriod", new StrategyModel.ParameterDefinition("number", 12, "Fast Period"));
        macdParams.put("slowPeriod", new StrategyModel.ParameterDefinition("number", 26, "Slow Period"));
        macdParams.put("signalPeriod", new StrategyModel.ParameterDefinition("number", 9, "Signal Period"));
        macdParams.put("crossAbove", new StrategyModel.ParameterDefinition("boolean", true, "Cross Above"));

        strategies.add(new StrategyModel("MACD_CROSSOVER", "MACD Crossover",
                "Detects when MACD line crosses above or below the signal line", macdParams));

        // BOLLINGER_BANDS
        Map<String, StrategyModel.ParameterDefinition> bbParams = new HashMap<>();
        bbParams.put("period", new StrategyModel.ParameterDefinition("number", 20, "Period"));
        bbParams.put("numStd", new StrategyModel.ParameterDefinition("number", 2.0, "Standard Deviations", 0.1));
        bbParams.put("checkUpper", new StrategyModel.ParameterDefinition("boolean", false, "Check Upper Band"));

        strategies.add(new StrategyModel("BOLLINGER_BANDS", "Bollinger Bands",
                "Triggers when price touches or crosses Bollinger Band boundaries", bbParams));

        // ATR
        Map<String, StrategyModel.ParameterDefinition> atrParams = new HashMap<>();
        atrParams.put("period", new StrategyModel.ParameterDefinition("number", 14, "Period"));
        atrParams.put("multiplier", new StrategyModel.ParameterDefinition("number", 1.5, "Multiplier", 0.1));
        atrParams.put("isAbove", new StrategyModel.ParameterDefinition("boolean", true, "Above Threshold"));
        atrParams.put("compareWithPrice", new StrategyModel.ParameterDefinition("boolean", true, "Compare with Price Movement"));

        strategies.add(new StrategyModel("ATR", "ATR Condition",
                "Measures volatility using Average True Range for trend strength", atrParams));

        // STOCHASTIC
        Map<String, StrategyModel.ParameterDefinition> stochParams = new HashMap<>();
        stochParams.put("kPeriod", new StrategyModel.ParameterDefinition("number", 14, "K Period"));
        stochParams.put("dPeriod", new StrategyModel.ParameterDefinition("number", 3, "D Period"));
        stochParams.put("upperThreshold", new StrategyModel.ParameterDefinition("number", 80, "Upper Threshold"));
        stochParams.put("lowerThreshold", new StrategyModel.ParameterDefinition("number", 20, "Lower Threshold"));
        stochParams.put("checkOverbought", new StrategyModel.ParameterDefinition("boolean", false, "Check Overbought"));

        strategies.add(new StrategyModel("STOCHASTIC", "Stochastic Oscillator",
                "Identifies overbought/oversold conditions using momentum oscillator", stochParams));

        // DMI
        Map<String, StrategyModel.ParameterDefinition> dmiParams = new HashMap<>();
        dmiParams.put("period", new StrategyModel.ParameterDefinition("number", 14, "Period"));

        List<StrategyModel.ParameterOption> dmiOptions = Arrays.asList(
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
        dmiParams.put("signalType", new StrategyModel.ParameterDefinition("string", "PLUS_DI_ABOVE_MINUS_DI", "Signal Type", dmiOptions));
        dmiParams.put("threshold", new StrategyModel.ParameterDefinition("number", 25.0, "ADX Threshold", 0.1));
        dmiParams.put("divergenceThreshold", new StrategyModel.ParameterDefinition("number", 10.0, "Divergence Threshold", 0.1));

        strategies.add(new StrategyModel("DMI", "DMI/ADX Condition",
                "Analyzes trend strength and direction using Directional Movement Index", dmiParams));

        // ICHIMOKU_CLOUD
        Map<String, StrategyModel.ParameterDefinition> ichimokuParams = new HashMap<>();
        ichimokuParams.put("tenkanPeriod", new StrategyModel.ParameterDefinition("number", 9, "Tenkan Period"));
        ichimokuParams.put("kijunPeriod", new StrategyModel.ParameterDefinition("number", 26, "Kijun Period"));
        ichimokuParams.put("chikouPeriod", new StrategyModel.ParameterDefinition("number", 52, "Chikou Period"));

        List<StrategyModel.ParameterOption> ichimokuOptions = Arrays.asList(
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
        ichimokuParams.put("signalType", new StrategyModel.ParameterDefinition("string", "PRICE_ABOVE_CLOUD", "Signal Type", ichimokuOptions));

        strategies.add(new StrategyModel("ICHIMOKU_CLOUD", "Ichimoku Cloud",
                "Comprehensive trend analysis using multiple Ichimoku components", ichimokuParams));

        // OBV
        Map<String, StrategyModel.ParameterDefinition> obvParams = new HashMap<>();
        obvParams.put("period", new StrategyModel.ParameterDefinition("number", 20, "Period"));

        List<StrategyModel.ParameterOption> obvOptions = Arrays.asList(
                new StrategyModel.ParameterOption("ABOVE_MA", "Above Moving Average"),
                new StrategyModel.ParameterOption("BELOW_MA", "Below Moving Average"),
                new StrategyModel.ParameterOption("CROSS_ABOVE_MA", "Cross Above Moving Average"),
                new StrategyModel.ParameterOption("CROSS_BELOW_MA", "Cross Below Moving Average"),
                new StrategyModel.ParameterOption("INCREASING", "Increasing"),
                new StrategyModel.ParameterOption("DECREASING", "Decreasing")
        );
        obvParams.put("conditionType", new StrategyModel.ParameterDefinition("string", "ABOVE_MA", "Condition Type", obvOptions));

        strategies.add(new StrategyModel("OBV", "On-Balance Volume",
                "Tracks volume flow to predict price movements and confirm trends", obvParams));

        // PIVOT_POINTS
        Map<String, StrategyModel.ParameterDefinition> pivotParams = new HashMap<>();

        List<StrategyModel.ParameterOption> pivotTypeOptions = Arrays.asList(
                new StrategyModel.ParameterOption("STANDARD", "Standard"),
                new StrategyModel.ParameterOption("FIBONACCI", "Fibonacci"),
                new StrategyModel.ParameterOption("WOODIE", "Woodie"),
                new StrategyModel.ParameterOption("CAMARILLA", "Camarilla"),
                new StrategyModel.ParameterOption("DEMARK", "DeMark")
        );
        pivotParams.put("pivotType", new StrategyModel.ParameterDefinition("string", "STANDARD", "Pivot Type", pivotTypeOptions));

        List<StrategyModel.ParameterOption> pivotLevelOptions = Arrays.asList(
                new StrategyModel.ParameterOption("PP", "Pivot Point"),
                new StrategyModel.ParameterOption("R1", "Resistance 1"),
                new StrategyModel.ParameterOption("R2", "Resistance 2"),
                new StrategyModel.ParameterOption("R3", "Resistance 3"),
                new StrategyModel.ParameterOption("S1", "Support 1"),
                new StrategyModel.ParameterOption("S2", "Support 2"),
                new StrategyModel.ParameterOption("S3", "Support 3")
        );
        pivotParams.put("pivotLevel", new StrategyModel.ParameterDefinition("string", "PP", "Pivot Level", pivotLevelOptions));
        pivotParams.put("crossAbove", new StrategyModel.ParameterDefinition("boolean", true, "Cross Above"));
        pivotParams.put("useClose", new StrategyModel.ParameterDefinition("boolean", true, "Use Close Price"));

        strategies.add(new StrategyModel("PIVOT_POINTS", "Pivot Points",
                "Identifies key support and resistance levels based on previous period", pivotParams));

        // ROC
        Map<String, StrategyModel.ParameterDefinition> rocParams = new HashMap<>();
        rocParams.put("period", new StrategyModel.ParameterDefinition("number", 12, "Period"));
        rocParams.put("threshold", new StrategyModel.ParameterDefinition("number", 5.0, "Threshold %", 0.1));

        List<StrategyModel.ParameterOption> rocDirectionOptions = Arrays.asList(
                new StrategyModel.ParameterOption("ABOVE", "Above"),
                new StrategyModel.ParameterOption("BELOW", "Below"),
                new StrategyModel.ParameterOption("EQUAL", "Equal"),
                new StrategyModel.ParameterOption("CROSSING_ABOVE", "Crossing Above"),
                new StrategyModel.ParameterOption("CROSSING_BELOW", "Crossing Below")
        );
        rocParams.put("direction", new StrategyModel.ParameterDefinition("string", "ABOVE", "Direction", rocDirectionOptions));

        strategies.add(new StrategyModel("ROC", "Rate of Change",
                "Measures momentum by comparing current price to price N periods ago", rocParams));

        // ROC_CROSSOVER
        Map<String, StrategyModel.ParameterDefinition> rocCrossParams = new HashMap<>();
        rocCrossParams.put("period", new StrategyModel.ParameterDefinition("number", 12, "Period"));
        rocCrossParams.put("threshold", new StrategyModel.ParameterDefinition("number", 0.0, "Threshold %", 0.1));
        rocCrossParams.put("crossAbove", new StrategyModel.ParameterDefinition("boolean", true, "Cross Above"));

        strategies.add(new StrategyModel("ROC_CROSSOVER", "ROC Crossover",
                "Detects when Rate of Change crosses above or below threshold level", rocCrossParams));

        // ROC_DIVERGENCE
        Map<String, StrategyModel.ParameterDefinition> rocDivParams = new HashMap<>();
        rocDivParams.put("period", new StrategyModel.ParameterDefinition("number", 12, "ROC Period"));
        rocDivParams.put("divergencePeriod", new StrategyModel.ParameterDefinition("number", 20, "Divergence Period"));
        rocDivParams.put("bullish", new StrategyModel.ParameterDefinition("boolean", true, "Bullish Divergence"));

        strategies.add(new StrategyModel("ROC_DIVERGENCE", "ROC Divergence",
                "Identifies bullish/bearish divergences between price and ROC momentum", rocDivParams));

        // FIBONACCI_RETRACEMENT
        Map<String, StrategyModel.ParameterDefinition> fibParams = new HashMap<>();
        fibParams.put("lookbackPeriod", new StrategyModel.ParameterDefinition("number", 50, "Lookback Period"));

        List<StrategyModel.ParameterOption> fibLevelOptions = Arrays.asList(
                new StrategyModel.ParameterOption("0.236", "23.6%"),
                new StrategyModel.ParameterOption("0.382", "38.2%"),
                new StrategyModel.ParameterOption("0.5", "50%"),
                new StrategyModel.ParameterOption("0.618", "61.8%"),
                new StrategyModel.ParameterOption("0.786", "78.6%")
        );
        fibParams.put("level", new StrategyModel.ParameterDefinition("number", 0.618, "Fibonacci Level", fibLevelOptions));
        fibParams.put("isBullish", new StrategyModel.ParameterDefinition("boolean", true, "Bullish Retracement"));
        fibParams.put("tolerance", new StrategyModel.ParameterDefinition("number", 0.01, "Tolerance", 0.001));

        strategies.add(new StrategyModel("FIBONACCI_RETRACEMENT", "Fibonacci Retracement",
                "Identifies potential support/resistance levels using Fibonacci ratios", fibParams));

        return strategies;
    }

    public StrategyModel getStrategy(String key) {
        return getAllStrategies().stream()
                .filter(strategy -> strategy.getKey().equals(key))
                .findFirst()
                .orElse(null);
    }
}