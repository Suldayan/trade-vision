import { BackTestRequest, ConditionConfig } from '../types/backtest';

export const DEFAULT_PARAMETERS: Record<string, Record<string, any>> = {
  SMA_CROSSOVER: {
    fastPeriod: 5,
    slowPeriod: 20,
    crossAbove: true
  },
  RSI_THRESHOLD: {
    period: 14,
    upperThreshold: 70,
    lowerThreshold: 30,
    checkOverbought: true
  },
  MACD_CROSSOVER: {
    fastPeriod: 12,
    slowPeriod: 26,
    signalPeriod: 9,
    crossAbove: true
  },
  BOLLINGER_BANDS: {  
    period: 20,
    numStd: 2.0,      
    checkUpper: true
  },
  STOCHASTIC: {
    kPeriod: 14,
    dPeriod: 3,
    upperThreshold: 80,
    lowerThreshold: 20,
    checkOverbought: true
  },
  FIBONACCI_RETRACEMENT: {
    lookbackPeriod: 20,
    level: 0.618,
    isBullish: true,
    tolerance: 0.02
  },
  ROC: {
    period: 12,
    threshold: 0,
    direction: 'ABOVE'
  },
  ROC_DIVERGENCE: {
    period: 14,
    lookbackPeriods: 3,
    divergenceType: 'bullish'
  },
  ROC_CROSSOVER: {
    fastPeriod: 5,
    slowPeriod: 20,
    crossAbove: true
  },
  ATR: {
    period: 14,
    multiplier: 2.0,
    isAbove: true,
    compareWithPrice: false
  },
  OBV: {
    period: 20,
    threshold: 0,
    isIncreasing: true
  },
  ICHIMOKU_CLOUD: {
    conversionPeriod: 9,
    basePeriod: 26,
    spanPeriod: 52,
    displacement: 26,
    cloudComponent: 'price_above_cloud',
    isBullish: true
  },
  PIVOT_POINTS: {
    type: 'standard',
    level: 'S1',
    tolerance: 0.01,
    above: false,
    below: false,
    crossAbove: false,
    crossBelow: false,
    equal: false
  },
  DMI: {
    adxPeriod: 14,
    diPeriod: 14,
    adxThreshold: 25,
    dmiComponent: 'di_plus_above'
  },
  AND: {
    conditions: []
  },
  OR: {
    conditions: []
  },
  NOT: {
    condition: null
  }
};

export const createCondition = (type: string): ConditionConfig => {
  const params = { ...DEFAULT_PARAMETERS[type] };
  
  // If it's a NOT condition, we need to ensure it has a default child condition
  if (type === 'NOT' && params.condition === null) {
    params.condition = createCondition('SMA_CROSSOVER');
  }
  
  return {
    type,
    parameters: params
  };
};

const checkRequiredParams = (condition: ConditionConfig, requiredParams: string[]): void => {
  for (const param of requiredParams) {
    if (condition.parameters[param] === undefined || condition.parameters[param] === null) {
      throw new Error(`Missing required parameter '${param}' for condition type '${condition.type}'`);
    }
  }
};

// Validate a single condition recursively
export const validateCondition = (condition: ConditionConfig): void => {
  // Check that all required parameters are present
  switch (condition.type) {
    case 'AND':
    case 'OR':
      if (!condition.parameters.conditions || !Array.isArray(condition.parameters.conditions)) {
        throw new Error(`${condition.type} condition requires an array of child conditions`);
      }
      // Recursively validate child conditions
      condition.parameters.conditions.forEach(validateCondition);
      break;
      
    case 'NOT':
      if (!condition.parameters.condition) {
        throw new Error('NOT condition requires a child condition');
      }
      validateCondition(condition.parameters.condition);
      break;
      
    case 'SMA_CROSSOVER':
      checkRequiredParams(condition, ['fastPeriod', 'slowPeriod', 'crossAbove']);
      break;
      
    case 'RSI_THRESHOLD':
      checkRequiredParams(condition, ['period', 'upperThreshold', 'lowerThreshold', 'checkOverbought']);
      break;
      
    case 'MACD_CROSSOVER':
      checkRequiredParams(condition, ['fastPeriod', 'slowPeriod', 'signalPeriod', 'crossAbove']);
      break;
      
    case 'BOLLINGER_BANDS':
      checkRequiredParams(condition, ['period', 'numStd', 'checkUpper']);
      break;
      
    case 'STOCHASTIC':
      checkRequiredParams(condition, ['kPeriod', 'dPeriod', 'upperThreshold', 'lowerThreshold', 'checkOverbought']);
      break;
      
    case 'FIBONACCI_RETRACEMENT':
      checkRequiredParams(condition, ['lookbackPeriod', 'level', 'isBullish', 'tolerance']);
      break;
      
    case 'ROC':
      checkRequiredParams(condition, ['period', 'threshold']);
      break;
      
    case 'ROC_DIVERGENCE':
      checkRequiredParams(condition, ['period', 'lookbackPeriods', 'divergenceType']);
      break;
      
    case 'ROC_CROSSOVER':
      checkRequiredParams(condition, ['fastPeriod', 'slowPeriod', 'crossAbove']);
      break;
      
    case 'ATR':
      checkRequiredParams(condition, ['period', 'multiplier', 'isAbove', 'compareWithPrice']);
      break;
      
    case 'OBV':
      checkRequiredParams(condition, ['period', 'threshold', 'isIncreasing']);
      break;
      
    case 'ICHIMOKU_CLOUD':
      checkRequiredParams(condition, ['conversionPeriod', 'basePeriod', 'spanPeriod', 'displacement', 'cloudComponent']);
      break;
      
    case 'PIVOT_POINTS':
      checkRequiredParams(condition, ['type', 'level', 'tolerance']);
      break;
      
    case 'DMI':
      checkRequiredParams(condition, ['adxPeriod', 'diPeriod', 'adxThreshold', 'dmiComponent']);
      break;
      
    default:
      throw new Error(`Unknown condition type: ${condition.type}`);
  }
};

export const validateStrategy = (strategy: BackTestRequest): void => {
  strategy.entryConditions.forEach(validateCondition);
  strategy.exitConditions.forEach(validateCondition);
};

// Get a simplified display text for conditions
export const getStrategyDisplayText = (conditions: ConditionConfig[], requireAll: boolean): string => {
  if (conditions.length === 0) return 'No conditions defined';
  
  return conditions.map(c => {
    if (c.type === 'NOT') {
      const childType = c.parameters.condition?.type || 'Unknown';
      return `NOT(${childType.replace('_', ' ')})`;
    }
    if (c.type === 'AND' || c.type === 'OR') {
      const children = c.parameters.conditions || [];
      const childText = children.map((child: { type: string; }) => child.type.replace('_', ' ')).join(` ${c.type} `);
      return `(${childText})`;
    }
    return c.type.replace('_', ' ');
  }).join(requireAll ? ' AND ' : ' OR ');
};

// Default initial backtest request
export const DEFAULT_BACKTEST_REQUEST: BackTestRequest = {
  initialCapital: 10000,
  riskPerTrade: 0.02,
  commissionRate: 0.001,
  slippagePercent: 0.1,
  allowShort: false,
  entryConditions: [
    createCondition('SMA_CROSSOVER')
  ],
  exitConditions: [
    createCondition('RSI_THRESHOLD')
  ],
  requireAllEntryConditions: true,
  requireAllExitConditions: false
};