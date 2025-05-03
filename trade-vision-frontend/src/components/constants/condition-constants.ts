// Available condition types
export const CONDITION_TYPES = [
    'SMA_CROSSOVER',
    'RSI_THRESHOLD',
    'MACD_CROSSOVER',
    'BOLLINGER_BANDS',  
    'STOCHASTIC',
    'FIBONACCI_RETRACEMENT',
//TODO: implement these ones in both backend and frontend
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
  
  // Get gradient color based on condition type
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
  
  // Parameter type constraints
  export const PARAMETER_CONSTRAINTS = {
    // Min values
    min: {
      default: 0,
      period: 1,
      upperThreshold: 50,
      lowerThreshold: 0
    },
    // Step values
    step: {
      default: 1,
      numStd: 0.1,
      level: 0.001,
      tolerance: 0.001,
      rate: 0.001,
      percent: 0.001
    },
    // Direction values
    direction: {
      above: false,
      below: false,
      crossing_above: false,
      crossing_below: false,
      equal: false
    },
    // Max values
    max: {
      default: 20,
      period: 200,
      threshold: 100
    }
  };