import { PARAMETER_CONSTRAINTS } from "../../constants/condition-constants";

// Format parameter name for display
export const formatParamLabel = (param: string): string => {
  return param
    .replace(/([A-Z])/g, ' $1')
    .replace(/^./, (str) => str.toUpperCase());
};

// Determine min value for a parameter
export const getParamMinValue = (paramName: string): number => {
  if (paramName.includes('Period')) return PARAMETER_CONSTRAINTS.min.period;
  if (paramName === 'upperThreshold') return PARAMETER_CONSTRAINTS.min.upperThreshold;
  if (paramName === 'lowerThreshold') return PARAMETER_CONSTRAINTS.min.lowerThreshold;
  return PARAMETER_CONSTRAINTS.min.default;
};

// Determine step value for a parameter
export const getParamStepValue = (paramName: string): number => {
  if (paramName === 'numStd') return PARAMETER_CONSTRAINTS.step.numStd;
  if (paramName === 'level' || paramName === 'tolerance') {
    return PARAMETER_CONSTRAINTS.step.level;
  }
  if (paramName.includes('Rate') || paramName.includes('Percent')) {
    return PARAMETER_CONSTRAINTS.step.rate;
  }
  return PARAMETER_CONSTRAINTS.step.default;
};

// Determine max value for a parameter
export const getParamMaxValue = (paramName: string): number => {
  if (paramName.includes('Period')) return PARAMETER_CONSTRAINTS.max.period;
  if (paramName.includes('Threshold')) return PARAMETER_CONSTRAINTS.max.threshold;
  return PARAMETER_CONSTRAINTS.max.default;
};

// Check if a param should be hidden from UI
export const shouldHideParam = (paramName: string): boolean => {
  return paramName === 'conditions' || paramName === 'condition';
};