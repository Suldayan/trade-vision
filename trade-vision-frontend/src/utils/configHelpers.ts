import type { IndicatorDefinition } from "../types/strategy";
import type { AnyCondition, LogicalGroupCondition } from "../types/strategy";

export const updateNestedObject = <T>(obj: T, path: string, value: any): T => {
  const keys = path.split('.');
  const newObj = { ...obj } as any;
  let current = newObj;
  
  for (let i = 0; i < keys.length - 1; i++) {
    const key = keys[i];
    if (key.includes('[') && key.includes(']')) {
      const [arrayKey, indexStr] = key.split(/[\[\]]/);
      const index = parseInt(indexStr, 10);
      current[arrayKey] = [...current[arrayKey]];
      current = current[arrayKey][index] = { ...current[arrayKey][index] };
    } else {
      current = current[key] = { ...current[key] };
    }
  }
  
  const lastKey = keys[keys.length - 1];
  if (lastKey.includes('[') && lastKey.includes(']')) {
    const [arrayKey, indexStr] = lastKey.split(/[\[\]]/);
    const index = parseInt(indexStr, 10);
    current[arrayKey][index] = value;
  } else {
    current[lastKey] = value;
  }
  
  return newObj;
};

export const createDefaultCondition = (type: string, indicators?: Record<string, IndicatorDefinition>): AnyCondition => {
  // Handle logical groups first
  switch (type) {
    case 'AND':
      return {
        type: 'AND',
        parameters: {
          conditions: [
            createDefaultCondition('RSI_THRESHOLD', indicators)
          ]
        }
      };
    
    case 'OR':
      return {
        type: 'OR',
        parameters: {
          conditions: [
            createDefaultCondition('RSI_THRESHOLD', indicators)
          ]
        }
      };
    
    case 'NOT':
      return {
        type: 'NOT',
        parameters: {
          condition: createDefaultCondition('RSI_THRESHOLD', indicators) // Single condition, not array
        }
      };
  }

  // Handle regular conditions
  const basicParameters = {
    RSI_THRESHOLD: {
      period: 14,
      upperThreshold: 70,
      lowerThreshold: 30,
      checkOverbought: false
    },
    SMA_CROSS: {
      fastPeriod: 10,
      slowPeriod: 20,
      checkCrossAbove: true
    },
    PRICE_THRESHOLD: {
      threshold: 100,
      checkAbove: true
    }
  };

  const parameters = basicParameters[type as keyof typeof basicParameters] || {};
  
  if (indicators && indicators[type]) {
    const indicatorDef = indicators[type];
    const params: Record<string, any> = {};
    
    Object.entries(indicatorDef.parameters).forEach(([key, def]) => {
      params[key] = def.default;
    });
    
    return { type, parameters: params };
  }

  return { type, parameters };
};

export const isLogicalGroup = (condition: AnyCondition): condition is LogicalGroupCondition => {
  return ['AND', 'OR', 'NOT'].includes(condition.type);
};