import { PARAMETER_CONSTRAINTS } from '../../constants/condition-constants';

export const formatParamLabel = (paramName: string): string => {
  const words = paramName.replace(/([A-Z])/g, ' $1');
  
  return words.charAt(0).toUpperCase() + words.slice(1);
};

export const getParamMinValue = (paramName: string): number => {
  if (Object.keys(PARAMETER_CONSTRAINTS.min).includes(paramName)) {
    return PARAMETER_CONSTRAINTS.min[paramName as keyof typeof PARAMETER_CONSTRAINTS.min];
  }
  
  // Special cases
  if (paramName.includes('Period')) return PARAMETER_CONSTRAINTS.min.period;
  if (paramName.includes('Threshold') && paramName.includes('upper')) return PARAMETER_CONSTRAINTS.min.upperThreshold;
  if (paramName.includes('Threshold') && paramName.includes('lower')) return PARAMETER_CONSTRAINTS.min.lowerThreshold;
  
  return PARAMETER_CONSTRAINTS.min.default;
};

export const getParamStepValue = (paramName: string): number => {
  if (Object.keys(PARAMETER_CONSTRAINTS.step).includes(paramName)) {
    return PARAMETER_CONSTRAINTS.step[paramName as keyof typeof PARAMETER_CONSTRAINTS.step];
  }
  
  // Special cases
  if (paramName.includes('numStd')) return PARAMETER_CONSTRAINTS.step.numStd;
  if (paramName.includes('level') || paramName.includes('Level')) return PARAMETER_CONSTRAINTS.step.level;
  if (paramName.includes('tolerance')) return PARAMETER_CONSTRAINTS.step.tolerance;
  if (paramName.includes('rate') || paramName.includes('Rate')) return PARAMETER_CONSTRAINTS.step.rate;
  if (paramName.includes('percent') || paramName.includes('Percent')) return PARAMETER_CONSTRAINTS.step.percent;
  
  return PARAMETER_CONSTRAINTS.step.default;
};


export const getParamMaxValue = (paramName: string): number => {
  if (Object.keys(PARAMETER_CONSTRAINTS.max).includes(paramName)) {
    return PARAMETER_CONSTRAINTS.max[paramName as keyof typeof PARAMETER_CONSTRAINTS.max];
  }
  
  // Special cases
  if (paramName.includes('Period')) return PARAMETER_CONSTRAINTS.max.period;
  if (paramName.includes('Threshold')) return PARAMETER_CONSTRAINTS.max.threshold;
  
  return PARAMETER_CONSTRAINTS.max.default;
};

export const isStringOptionParam = (conditionType: string, paramName: string): boolean => {
  const stringOptionParams: Record<string, string[]> = {
    'PIVOT_POINTS': ['pivotType', 'level'],
    'ICHIMOKU_CLOUD': ['signalType'],
    'ROC_DIVERGENCE': ['divergenceType'],
    'DMI': ['dmiSignalType']
  };
  
  return stringOptionParams[conditionType]?.includes(paramName) || false;
};

export const shouldHideParam = (paramName: string): boolean => {
  return paramName === 'conditions' || paramName === 'condition';
};