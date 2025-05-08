package com.example.trade_vision_backend.strategies.internal;

import com.example.trade_vision_backend.domain.BackTestRequest;
import com.example.trade_vision_backend.indicators.PivotType;
import com.example.trade_vision_backend.strategies.Condition;
import com.example.trade_vision_backend.domain.ConditionConfig;
import com.example.trade_vision_backend.strategies.Strategy;
import com.example.trade_vision_backend.strategies.StrategyService;
import com.example.trade_vision_backend.strategies.internal.conditions.*;
import com.example.trade_vision_backend.strategies.internal.enums.DMISignalType;
import com.example.trade_vision_backend.strategies.internal.enums.Direction;
import com.example.trade_vision_backend.strategies.internal.enums.IchimokuSignalType;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class StrategyServiceImpl implements StrategyService {

    @Nonnull
    @Override
    public Strategy buildStrategyFromRequest(@Nonnull BackTestRequest request) {
        log.info("Building strategy from request with {} entry conditions and {} exit conditions",
                request.getEntryConditions() != null ? request.getEntryConditions().size() : 0,
                request.getExitConditions() != null ? request.getExitConditions().size() : 0);

        Strategy strategy = new Strategy();

        if (request.getEntryConditions() != null) {
            log.debug("Processing {} entry conditions", request.getEntryConditions().size());
            for (ConditionConfig config : request.getEntryConditions()) {
                log.debug("Creating entry condition of type: {}", config.getType());
                try {
                    Condition condition = createConditionFromConfig(config);
                    strategy.addEntryCondition(condition);
                    log.debug("Successfully added entry condition of type: {}", config.getType());
                } catch (Exception e) {
                    log.error("Failed to create entry condition of type: {}", config.getType(), e);
                    throw e;
                }
            }
        }

        if (request.getExitConditions() != null) {
            log.debug("Processing {} exit conditions", request.getExitConditions().size());
            for (ConditionConfig config : request.getExitConditions()) {
                log.debug("Creating exit condition of type: {}", config.getType());
                try {
                    Condition condition = createConditionFromConfig(config);
                    strategy.addExitCondition(condition);
                    log.debug("Successfully added exit condition of type: {}", config.getType());
                } catch (Exception e) {
                    log.error("Failed to create exit condition of type: {}", config.getType(), e);
                    throw e;
                }
            }
        }

        strategy.setRequireAllEntryConditions(request.isRequireAllEntryConditions());
        strategy.setRequireAllExitConditions(request.isRequireAllExitConditions());

        log.info("Strategy built successfully. Entry conditions: {}, Exit conditions: {}",
                strategy.getEntryConditions().size(), strategy.getExitConditions().size());

        return strategy;
    }

    @Nonnull
    @Override
    public Condition createConditionFromConfig(@Nonnull ConditionConfig config) {
        log.debug("Creating condition from config of type: {}", config.getType());

        try {
            return switch (config.getType()) {
                case "SMA_CROSSOVER" -> createSmaCrossover(config);
                case "RSI_THRESHOLD" -> createRsiThreshold(config);
                case "MACD_CROSSOVER" -> createMacdCrossover(config);
                case "BOLLINGER_BANDS" -> createBollingerBands(config);
                case "STOCHASTIC" -> createStochastic(config);
                case "FIBONACCI_RETRACEMENT" -> createFibonacciRetracement(config);
                case "ROC" -> createRateOfChange(config);
                case "ROC_CROSSOVER" -> createRateOfChangeCrossover(config);
                case "ROC_DIVERGENCE" -> createRateOfChangeDivergence(config);
                case "OBV" -> createObv(config);
                case "OBV_CROSSOVER" -> createObvCrossover(config);
                case "OBV_POSITION" -> createObvPosition(config);
                case "ICHIMOKU_CLOUD" -> createIchimokuCloud(config);
                //case "PIVOT_POINTS" -> createPivotPoints(config);
                case "ATR" -> createAtr(config);
                case "DMI" -> createDmi(config);
                case "AND" -> createAndComposite(config);
                case "OR" -> createOrComposite(config);
                case "NOT" -> createNotComposite(config);
                default -> {
                    log.error("Unknown condition type: {}", config.getType());
                    throw new IllegalArgumentException("Unknown condition type: " + config.getType());
                }
            };
        } catch (Exception e) {
            log.error("Error creating condition of type: {}", config.getType(), e);
            throw e;
        }
    }

    @Nonnull
    private SMACrossoverCondition createSmaCrossover(@Nonnull ConditionConfig config) {
        int fastPeriod = getIntParam(config, "fastPeriod");
        int slowPeriod = getIntParam(config, "slowPeriod");
        boolean crossAbove = getBooleanParam(config, "crossAbove");

        log.debug("Creating SMA crossover condition with fastPeriod={}, slowPeriod={}, crossAbove={}",
                fastPeriod, slowPeriod, crossAbove);

        return new SMACrossoverCondition(fastPeriod, slowPeriod, crossAbove);
    }

    @Nonnull
    private RSICondition createRsiThreshold(@Nonnull ConditionConfig config) {
        int period = getIntParam(config, "period");
        double upperThreshold = getDoubleParam(config, "upperThreshold");
        double lowerThreshold = getDoubleParam(config, "lowerThreshold");
        boolean checkOverbought = getBooleanParam(config, "checkOverbought");

        log.debug("Creating RSI condition with period={}, upperThreshold={}, lowerThreshold={}, checkOverbought={}",
                period, upperThreshold, lowerThreshold, checkOverbought);

        return new RSICondition(period, upperThreshold, lowerThreshold, checkOverbought);
    }

    @Nonnull
    private MACDCrossoverCondition createMacdCrossover(@Nonnull ConditionConfig config) {
        int fastPeriod = getIntParam(config, "fastPeriod");
        int slowPeriod = getIntParam(config, "slowPeriod");
        int signalPeriod = getIntParam(config, "signalPeriod");
        boolean crossAbove = getBooleanParam(config, "crossAbove");

        log.debug("Creating MACD crossover condition with fastPeriod={}, slowPeriod={}, signalPeriod={}, crossAbove={}",
                fastPeriod, slowPeriod, signalPeriod, crossAbove);

        return new MACDCrossoverCondition(fastPeriod, slowPeriod, signalPeriod, crossAbove);
    }

    @Nonnull
    private BollingerBandsCondition createBollingerBands(@Nonnull ConditionConfig config) {
        int period = getIntParam(config, "period");
        double numStd = getDoubleParam(config, "numStd");
        boolean checkUpper = getBooleanParam(config, "checkUpper");

        log.debug("Creating Bollinger Bands condition with period={}, numStd={}, checkUpper={}",
                period, numStd, checkUpper);

        return new BollingerBandsCondition(period, numStd, checkUpper);
    }

    @Nonnull
    private StochasticCondition createStochastic(@Nonnull ConditionConfig config) {
        int kPeriod = getIntParam(config, "kPeriod");
        int dPeriod = getIntParam(config, "dPeriod");
        double upperThreshold = getDoubleParam(config, "upperThreshold");
        double lowerThreshold = getDoubleParam(config, "lowerThreshold");
        boolean checkOverbought = getBooleanParam(config, "checkOverbought");

        log.debug("Creating Stochastic condition with kPeriod={}, dPeriod={}, upperThreshold={}, lowerThreshold={}, checkOverbought={}",
                kPeriod, dPeriod, upperThreshold, lowerThreshold, checkOverbought);

        return new StochasticCondition(kPeriod, dPeriod, upperThreshold, lowerThreshold, checkOverbought);
    }

    @Nonnull
    private FibonacciRetracementCondition createFibonacciRetracement(@Nonnull ConditionConfig config) {
        int lookbackPeriod = getIntParam(config, "lookbackPeriod");
        double level = getDoubleParam(config, "level");
        boolean isBullish = getBooleanParam(config, "isBullish");
        double tolerance = getDoubleParam(config, "tolerance");

        log.debug("Creating Fibonacci Retracement condition with lookbackPeriod={}, level={}, bullish={}, tolerance={}",
                lookbackPeriod, level, isBullish, tolerance);

        return new FibonacciRetracementCondition(lookbackPeriod, level, isBullish, tolerance);
    }

    @Nonnull
    private ROCCondition createRateOfChange(@Nonnull ConditionConfig config) {
        int period = getIntParam(config, "period");
        double threshold = getDoubleParam(config, "threshold");
        Direction direction = getEnumParam(config, "direction", Direction.class);

        log.debug("Creating Rate of Change condition with period={}, threshold={}, direction={}",
                period, threshold, direction);

        return new ROCCondition(period, threshold, direction);
    }

    @Nonnull
    private ROCCrossoverCondition createRateOfChangeCrossover(@Nonnull ConditionConfig config) {
        int period = getIntParam(config, "period");
        double threshold = getDoubleParam(config, "threshold");
        boolean crossAbove = getBooleanParam(config, "crossAbove");

        log.debug("Creating Rate of Change Crossover condition with period={}, threshold={}, crossAbove={}",
                period, threshold, crossAbove);

        return new ROCCrossoverCondition(period, threshold, crossAbove);
    }

    @Nonnull
    private ROCDivergenceCondition createRateOfChangeDivergence(@Nonnull ConditionConfig config) {
        int period = getIntParam(config, "period");
        int divergencePeriod = getIntParam(config, "divergencePeriod");
        boolean bullish = getBooleanParam(config, "bullish");

        log.debug("Creating Rate of Change Divergence condition with period={}, divergencePeriod={}, bullish={}",
                period, divergencePeriod, bullish);

        return new ROCDivergenceCondition(period, divergencePeriod, bullish);
    }

    @Nonnull
    private OBVCondition createObv(@Nonnull ConditionConfig config) {
        int period = getIntParam(config, "period");
        String conditionType = config.getParameters().get("conditionType").toString();

        log.debug("Creating OBV condition with period={}, conditionType={}",
                period, conditionType);

        return new OBVCondition(period, conditionType);
    }

    @Nonnull
    private OBVCondition createObvCrossover(@Nonnull ConditionConfig config) {
        int period = getIntParam(config, "period");
        boolean crossAbove = getBooleanParam(config, "crossAbove");

        log.debug("Creating OBV crossover condition with period={}, crossAbove={}",
                period, crossAbove);

        return new OBVCondition(period, crossAbove);
    }

    @Nonnull
    private OBVCondition createObvPosition(@Nonnull ConditionConfig config) {
        int period = getIntParam(config, "period");
        boolean isAbove = getBooleanParam(config, "isAbove");

        log.debug("Creating OBV position condition with period={}, isAbove={}",
                period, isAbove);

        return new OBVCondition(period, isAbove, false);
    }

    @Nonnull
    private IchimokuCloudCondition createIchimokuCloud(@Nonnull ConditionConfig config) {
        int tenkanPeriod = getIntParam(config, "tenkanPeriod", 9);
        int kijunPeriod = getIntParam(config, "kijunPeriod", 26);
        int chikouPeriod = getIntParam(config, "chikouPeriod", 52);
        IchimokuSignalType signalType = getEnumParam(config, "signalType", IchimokuSignalType.class);

        log.debug("Creating Ichimoku Cloud condition with tenkanPeriod={}, kijunPeriod={}, chikouPeriod={}, signalType={}",
                tenkanPeriod, kijunPeriod, chikouPeriod, signalType);

        return new IchimokuCloudCondition(tenkanPeriod, kijunPeriod, chikouPeriod, signalType);
    }

    @Nonnull
    private DMICondition createDmi(@Nonnull ConditionConfig config) {
        int period = getIntParam(config, "period", 14);
        DMISignalType signalType = getEnumParam(config, "dmiSignalType", DMISignalType.class);
        double threshold = getDoubleParam(config, "threshold", 25.0);
        double divergenceThreshold = getDoubleParam(config, "divergenceThreshold", 10.0);

        log.debug("Creating DMI condition with period={}, signalType={}, threshold={}, divergenceThreshold={}",
                period, signalType, threshold, divergenceThreshold);

        return new DMICondition(period, signalType, threshold, divergenceThreshold);
    }

    @Nonnull
    private ATRCondition createAtr(@Nonnull ConditionConfig config) {
        int period = getIntParam(config, "period");
        double multiplier = getDoubleParam(config, "multiplier");
        boolean isAbove = getBooleanParam(config, "isAbove");
        boolean compareWithPrice = getBooleanParam(config, "compareWithPrice");

        log.debug("Creating ATR condition with period={}, multiplier={}, isAbove={}, compareWithPrice={}",
                period, multiplier, isAbove, compareWithPrice);

        return new ATRCondition(period, multiplier, isAbove, compareWithPrice);
    }

    /* TODO: Implement pivot points
    @Nonnull
    private PivotPointsCondition createPivotPoints(@Nonnull ConditionConfig config) {
        PivotType pivotType = getEnumParam(config, "pivotType", PivotType.class);
        String level = getStringParam(config, "level");
        boolean crossAbove = getBooleanParam(config, "crossAbove");
        boolean useClose = getBooleanParam(config, "useClose");

        log.debug("Creating Pivot Points condition with level={}, pivotType={}, crossAbove={}, useClose={}",
                level, pivotType, crossAbove, useClose);

        return new PivotPointsCondition(pivotType, level, crossAbove, useClose);

    }
     */

    @Nonnull
    private CompositeCondition createAndComposite(@Nonnull ConditionConfig config) {
        log.debug("Creating AND composite condition");
        CompositeCondition composite = new CompositeCondition(CompositeCondition.LogicalOperator.AND);

        addChildConditions(config, composite, "AND");

        return composite;
    }

    @Nonnull
    private CompositeCondition createOrComposite(@Nonnull ConditionConfig config) {
        log.debug("Creating OR composite condition");
        CompositeCondition composite = new CompositeCondition(CompositeCondition.LogicalOperator.OR);

        addChildConditions(config, composite, "OR");

        return composite;
    }

    @Nonnull
    private CompositeCondition createNotComposite(@Nonnull ConditionConfig config) {
        log.debug("Creating NOT composite condition");
        Object childConfigObj = config.getParameters().get("condition");

        if (childConfigObj == null) {
            log.error("NOT condition is missing the child condition to negate");
            throw new IllegalArgumentException("NOT condition requires a child condition parameter");
        }

        try {
            Condition childCondition = createChildCondition(childConfigObj);
            return new CompositeCondition(childCondition);
        } catch (Exception e) {
            log.error("Failed to create child condition for NOT operator", e);
            throw e;
        }
    }

    private void addChildConditions(
            @Nonnull ConditionConfig config,
            @Nonnull CompositeCondition composite,
            @Nonnull String operatorName) {

        Object conditionsObj = config.getParameters().get("conditions");
        if (conditionsObj != null) {
            List<Object> conditionsList;
            if (conditionsObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> tempList = (List<Object>) conditionsObj;
                conditionsList = tempList;

                log.debug("Processing {} child conditions for {} operator", conditionsList.size(), operatorName);
                for (Object childConfigObj : conditionsList) {
                    try {
                        Condition childCondition = createChildCondition(childConfigObj);
                        composite.addCondition(childCondition);
                    } catch (Exception e) {
                        log.error("Failed to create child condition for {} operator", operatorName, e);
                        throw e;
                    }
                }
            } else {
                log.warn("{} composite condition has unexpected conditions format", operatorName);
            }
        } else {
            log.warn("{} composite condition has no child conditions", operatorName);
        }
    }

    @Nonnull
    private Condition createChildCondition(@Nonnull Object childConfigObj) {
        if (childConfigObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) childConfigObj;
            String type = (String) map.get("type");
            Map<String, Object> parameters = (Map<String, Object>) map.get("parameters");
            ConditionConfig tempConfig = ConditionConfig.builder()
                    .type(type)
                    .parameters(parameters)
                    .build();

            return createConditionFromConfig(tempConfig);
        } else {
            return createConditionFromConfig((ConditionConfig) childConfigObj);
        }
    }

    private int getIntParam(
            @Nonnull ConditionConfig config,
            @Nonnull String paramName) {
        log.trace("Getting integer parameter: {} from condition type: {}", paramName, config.getType());
        Object value = config.getParameters().get(paramName);
        return switch (value) {
            case null -> {
                log.error("Missing required int parameter: {} for condition type: {}", paramName, config.getType());
                throw new IllegalArgumentException("Missing required parameter: " + paramName);
            }
            case Integer i -> i;
            case Number number -> number.intValue();
            case String s -> {
                try {
                    yield Integer.parseInt(s);
                } catch (NumberFormatException e) {
                    log.error("Failed to parse {} as integer for parameter: {}", s, paramName, e);
                    throw new IllegalArgumentException("Parameter '" + paramName + "' is not a valid integer: " + s, e);
                }
            }
            default -> {
                log.error("Parameter '{}' is not a valid integer: {}", paramName, value);
                throw new IllegalArgumentException("Parameter '" + paramName + "' is not a valid integer: " + value);
            }
        };
    }

    private int getIntParam(
            @Nonnull ConditionConfig config,
            @Nonnull String paramName,
            int defaultValue) {
        log.trace("Getting integer parameter with default: {} from condition type: {}", paramName, config.getType());
        Object value = config.getParameters().get(paramName);
        if (value == null) {
            log.debug("Using default int value {} for parameter: {}", defaultValue, paramName);
            return defaultValue;
        }

        return switch (value) {
            case Integer i -> i;
            case Number number -> number.intValue();
            case String s -> {
                try {
                    yield Integer.parseInt(s);
                } catch (NumberFormatException e) {
                    log.error("Failed to parse {} as integer for parameter: {}", s, paramName, e);
                    throw new IllegalArgumentException("Parameter '" + paramName + "' is not a valid integer: " + s, e);
                }
            }
            default -> {
                log.error("Parameter '{}' is not a valid integer: {}", paramName, value);
                throw new IllegalArgumentException("Parameter '" + paramName + "' is not a valid integer: " + value);
            }
        };
    }

    private double getDoubleParam(
            @Nonnull ConditionConfig config,
            @Nonnull String paramName) {
        log.trace("Getting double parameter: {} from condition type: {}", paramName, config.getType());
        Object value = config.getParameters().get(paramName);
        return switch (value) {
            case null -> {
                log.error("Missing required double parameter: {} for condition type: {}", paramName, config.getType());
                throw new IllegalArgumentException("Missing required parameter: " + paramName);
            }
            case Double v -> v;
            case Number number -> number.doubleValue();
            case String s -> {
                try {
                    yield Double.parseDouble(s);
                } catch (NumberFormatException e) {
                    log.error("Failed to parse {} as double for parameter: {}", s, paramName, e);
                    throw new IllegalArgumentException("Parameter '" + paramName + "' is not a valid double: " + s, e);
                }
            }
            default -> {
                log.error("Parameter '{}' is not a valid double: {}", paramName, value);
                throw new IllegalArgumentException("Parameter '" + paramName + "' is not a valid double: " + value);
            }
        };
    }

    private double getDoubleParam(
            @Nonnull ConditionConfig config,
            @Nonnull String paramName,
            double defaultValue) {
        log.trace("Getting double parameter with default: {} from condition type: {}", paramName, config.getType());
        Object value = config.getParameters().get(paramName);
        if (value == null) {
            log.debug("Using default double value {} for parameter: {}", defaultValue, paramName);
            return defaultValue;
        }

        return switch (value) {
            case Double v -> v;
            case Number number -> number.doubleValue();
            case String s -> {
                try {
                    yield Double.parseDouble(s);
                } catch (NumberFormatException e) {
                    log.error("Failed to parse {} as double for parameter: {}", s, paramName, e);
                    throw new IllegalArgumentException("Parameter '" + paramName + "' is not a valid double: " + s, e);
                }
            }
            default -> {
                log.error("Parameter '{}' is not a valid double: {}", paramName, value);
                throw new IllegalArgumentException("Parameter '" + paramName + "' is not a valid double: " + value);
            }
        };
    }

    private boolean getBooleanParam(
            @Nonnull ConditionConfig config,
            @Nonnull String paramName) {
        log.trace("Getting boolean parameter: {} from condition type: {}", paramName, config.getType());
        Object value = config.getParameters().get(paramName);
        return switch (value) {
            case null -> {
                log.error("Missing required boolean parameter: {} for condition type: {}", paramName, config.getType());
                throw new IllegalArgumentException("Missing required parameter: " + paramName);
            }
            case Boolean b -> b;
            case String s -> Boolean.parseBoolean(s);
            default -> {
                log.error("Parameter '{}' is not a valid boolean: {}", paramName, value);
                throw new IllegalArgumentException("Parameter '" + paramName + "' is not a valid boolean: " + value);
            }
        };
    }

    private <T extends Enum<T>> T getEnumParam(
            @Nonnull ConditionConfig config,
            @Nonnull String paramName,
            @Nonnull Class<T> enumClass) {
        log.trace("Getting enum parameter: {} from condition type: {}", paramName, config.getType());
        Object value = config.getParameters().get(paramName);
        if (value == null) {
            log.error("Missing required enum parameter: {} for condition type: {}", paramName, config.getType());
            throw new IllegalArgumentException("Missing required parameter: " + paramName);
        }

        if (value instanceof String enumName) {
            try {
                return Enum.valueOf(enumClass, enumName);
            } catch (IllegalArgumentException e) {
                log.error("Invalid enum value: {} for enum class: {}", enumName, enumClass.getSimpleName(), e);
                throw new IllegalArgumentException("Parameter '" + paramName +
                        "' is not a valid value for enum type " + enumClass.getSimpleName() + ": " + enumName, e);
            }
        } else if (enumClass.isInstance(value)) {
            @SuppressWarnings("unchecked")
            T enumValue = (T) value;
            return enumValue;
        } else {
            log.error("Parameter '{}' is not a valid enum value for type {}: {}",
                    paramName, enumClass.getSimpleName(), value);
            throw new IllegalArgumentException("Parameter '" + paramName +
                    "' is not a valid value for enum type " + enumClass.getSimpleName() + ": " + value);
        }
    }
}