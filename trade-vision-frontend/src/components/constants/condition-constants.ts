export const CONDITION_TYPES = [
  'SMA_CROSSOVER',
  'RSI_THRESHOLD',
  'MACD_CROSSOVER',
  'BOLLINGER_BANDS',  
  'STOCHASTIC',
  'FIBONACCI_RETRACEMENT',
  'ROC',
  'ROC_DIVERGENCE',
  'ROC_CROSSOVER',
  'ATR',
  'OBV',
  'ICHIMOKU_CLOUD',
  'PIVOT_POINTS',
  'DMI',
  'AND',
  'OR',
  'NOT'
];

export const getConditionColor = (type: string): string => {
  switch(type) {
    case 'AND': return 'from-blue-600 to-indigo-800';
    case 'OR': return 'from-indigo-600 to-purple-800';
    case 'NOT': return 'from-red-600 to-rose-800';
    case 'SMA_CROSSOVER': return 'from-emerald-600 to-teal-800';
    case 'RSI_THRESHOLD': return 'from-amber-600 to-orange-800';
    case 'MACD_CROSSOVER': return 'from-cyan-600 to-sky-800';
    case 'BOLLINGER_BANDS': return 'from-fuchsia-600 to-pink-800';
    case 'STOCHASTIC': return 'from-violet-600 to-purple-800';
    case 'FIBONACCI_RETRACEMENT': return 'from-lime-600 to-green-800';
    case 'ROC': return 'from-rose-600 to-red-800';
    case 'ROC_DIVERGENCE': return 'from-pink-600 to-rose-800';
    case 'ROC_CROSSOVER': return 'from-orange-600 to-amber-800';
    case 'ATR': return 'from-green-600 to-emerald-800';
    case 'OBV': return 'from-teal-600 to-cyan-800';
    case 'ICHIMOKU_CLOUD': return 'from-sky-600 to-blue-800';
    case 'PIVOT_POINTS': return 'from-yellow-600 to-amber-800';
    case 'DMI': return 'from-purple-600 to-violet-800';
    default: return 'from-gray-600 to-gray-800';
  }
};

export const PARAMETER_CONSTRAINTS = {
  min: {
    default: 0,
    period: 1,
    upperThreshold: 50,
    lowerThreshold: 0
  },
  step: {
    default: 1,
    numStd: 0.1,
    level: 0.001,
    tolerance: 0.001,
    rate: 0.001,
    percent: 0.001
  },
  direction: {
    above: false,
    below: false,
    crossing_above: false,
    crossing_below: false,
    equal: false
  },
  max: {
    default: 20,
    period: 200,
    threshold: 100
  }
};

export interface ParamOption {
value: string;
label: string;
}

// Map of condition types to their parameter options
export const PARAMETER_OPTIONS: Record<string, Record<string, ParamOption[]>> = {
PIVOT_POINTS: {
  pivotType: [
    { value: 'standard', label: 'Standard' },
    { value: 'fibonacci', label: 'Fibonacci' },
    { value: 'woodie', label: 'Woodie' },
    { value: 'camarilla', label: 'Camarilla' },
    { value: 'demark', label: 'DeMark' }
  ],
  level: [
    { value: 'PP', label: 'Pivot Point (PP)' },
    { value: 'R1', label: 'Resistance 1 (R1)' },
    { value: 'R2', label: 'Resistance 2 (R2)' },
    { value: 'R3', label: 'Resistance 3 (R3)' },
    { value: 'S1', label: 'Support 1 (S1)' },
    { value: 'S2', label: 'Support 2 (S2)' },
    { value: 'S3', label: 'Support 3 (S3)' }
  ],
  direction: [
    { value: 'ABOVE', label: 'Above' },
    { value: 'BELOW', label: 'Below' },
    { value: 'EQUAL', label: 'Equal' },
    { value: 'CROSSING_ABOVE', label: 'Crossing Above' },
    { value: 'CROSSING_BELOW', label: 'Crossing Below' }
  ]
},
ICHIMOKU_CLOUD: {
  signalType: [
    { value: 'TENKAN_CROSSES_ABOVE_KIJUN', label: 'Tenkan Crosses Above Kijun (Bullish)' },
    { value: 'TENKAN_CROSSES_BELOW_KIJUN', label: 'Tenkan Crosses Below Kijun (Bearish)' },
    { value: 'PRICE_ABOVE_CLOUD', label: 'Price Above Cloud (Bullish)' },
    { value: 'PRICE_BELOW_CLOUD', label: 'Price Below Cloud (Bearish)' },
    { value: 'PRICE_IN_CLOUD', label: 'Price In Cloud (Indecision)' },
    { value: 'BULLISH_CLOUD', label: 'Bullish Cloud (Senkou A > B)' },
    { value: 'BEARISH_CLOUD', label: 'Bearish Cloud (Senkou A < B)' },
    { value: 'CHIKOU_ABOVE_PRICE', label: 'Chikou Above Price (Bullish)' },
    { value: 'CHIKOU_BELOW_PRICE', label: 'Chikou Below Price (Bearish)' },
    { value: 'STRONG_BULLISH', label: 'Strong Bullish Signal' },
    { value: 'STRONG_BEARISH', label: 'Strong Bearish Signal' }
  ]
},
ROC_DIVERGENCE: {
  divergenceType: [
    { value: 'bullish', label: 'Bullish Divergence' },
    { value: 'bearish', label: 'Bearish Divergence' },
    { value: 'hidden_bullish', label: 'Hidden Bullish Divergence' },
    { value: 'hidden_bearish', label: 'Hidden Bearish Divergence' }
  ]
},
DMI: {
  dmiSignalType: [
    { value: 'PLUS_DI_ABOVE_MINUS_DI', label: '+DI Above -DI (Bullish)' },
    { value: 'MINUS_DI_ABOVE_PLUS_DI', label: '-DI Above +DI (Bearish)' },
    { value: 'PLUS_DI_CROSSES_ABOVE_MINUS_DI', label: '+DI Crosses Above -DI (Bullish Signal)' },
    { value: 'MINUS_DI_CROSSES_ABOVE_PLUS_DI', label: '-DI Crosses Above +DI (Bearish Signal)' },
    { value: 'ADX_ABOVE_THRESHOLD', label: 'ADX Above Threshold (Strong Trend)' },
    { value: 'ADX_BELOW_THRESHOLD', label: 'ADX Below Threshold (Weak Trend)' },
    { value: 'ADX_RISING', label: 'ADX Rising (Strengthening Trend)' },
    { value: 'ADX_FALLING', label: 'ADX Falling (Weakening Trend)' },
    { value: 'STRONG_TREND', label: 'Strong Trend (ADX > Threshold, Large DI Separation)' },
    { value: 'STRONG_BULLISH', label: 'Strong Bullish (ADX > Threshold, +DI > -DI)' },
    { value: 'STRONG_BEARISH', label: 'Strong Bearish (ADX > Threshold, -DI > +DI)' },
    { value: 'WEAK_TREND', label: 'Weak Trend (ADX < Threshold)' },
    { value: 'DI_DIVERGENCE', label: 'DI Divergence (Increasing Separation)' }
  ],
  threshold: [],
  period: []
}
};

export const DIRECTION_OPTIONS: ParamOption[] = [
{ value: 'ABOVE', label: 'Above' },
{ value: 'BELOW', label: 'Below' },
{ value: 'EQUAL', label: 'Equal' },
{ value: 'CROSSING_ABOVE', label: 'Crossing Above' },
{ value: 'CROSSING_BELOW', label: 'Crossing Below' }
];

export const getParamOptions = (conditionType: string, paramName: string): ParamOption[] | undefined => {
if (paramName === 'direction') {
  return DIRECTION_OPTIONS;
}

if (PARAMETER_OPTIONS[conditionType] && PARAMETER_OPTIONS[conditionType][paramName]) {
  return PARAMETER_OPTIONS[conditionType][paramName];
}

return undefined;
};