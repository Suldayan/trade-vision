export const indicators = {
  'RSI_THRESHOLD': { 
    name: 'RSI Threshold',
    description: 'Triggers when RSI crosses above or below specified threshold levels',
    parameters: {
      period: { type: 'number' as const, default: 14, label: 'Period' },
      upperThreshold: { type: 'number' as const, default: 70, label: 'Upper Threshold' },
      lowerThreshold: { type: 'number' as const, default: 30, label: 'Lower Threshold' },
      checkOverbought: { type: 'boolean' as const, default: false, label: 'Check Overbought' }
    }
  },
  'SMA_CROSSOVER': {
    name: 'SMA Crossover',
    description: 'Signals when fast moving average crosses above or below slow moving average',
    parameters: {
      fastPeriod: { type: 'number' as const, default: 5, label: 'Fast Period' },
      slowPeriod: { type: 'number' as const, default: 20, label: 'Slow Period' },
      crossAbove: { type: 'boolean' as const, default: true, label: 'Cross Above' }
    }
  },
  'MACD_CROSSOVER': {
    name: 'MACD Crossover',
    description: 'Detects when MACD line crosses above or below the signal line',
    parameters: {
      fastPeriod: { type: 'number' as const, default: 12, label: 'Fast Period' },
      slowPeriod: { type: 'number' as const, default: 26, label: 'Slow Period' },
      signalPeriod: { type: 'number' as const, default: 9, label: 'Signal Period' },
      crossAbove: { type: 'boolean' as const, default: true, label: 'Cross Above' }
    }
  },
  'BOLLINGER_BANDS': {
    name: 'Bollinger Bands',
    description: 'Triggers when price touches or crosses Bollinger Band boundaries',
    parameters: {
      period: { type: 'number' as const, default: 20, label: 'Period' },
      numStd: { type: 'number' as const, default: 2, label: 'Standard Deviations', step: 0.1 },
      checkUpper: { type: 'boolean' as const, default: false, label: 'Check Upper Band' }
    }
  },
  'ATR': {
    name: 'ATR Condition',
    description: 'Measures volatility using Average True Range for trend strength',
    parameters: {
      period: { type: 'number' as const, default: 14, label: 'Period' },
      multiplier: { type: 'number' as const, default: 1.5, label: 'Multiplier', step: 0.1 },
      isAbove: { type: 'boolean' as const, default: true, label: 'Above Threshold' },
      compareWithPrice: { type: 'boolean' as const, default: true, label: 'Compare with Price Movement' }
    }
  },
  'STOCHASTIC': {
    name: 'Stochastic Oscillator',
    description: 'Identifies overbought/oversold conditions using momentum oscillator',
    parameters: {
      kPeriod: { type: 'number' as const, default: 14, label: 'K Period' },
      dPeriod: { type: 'number' as const, default: 3, label: 'D Period' },
      upperThreshold: { type: 'number' as const, default: 80, label: 'Upper Threshold' },
      lowerThreshold: { type: 'number' as const, default: 20, label: 'Lower Threshold' },
      checkOverbought: { type: 'boolean' as const, default: false, label: 'Check Overbought' }
    }
  },
  'DMI': {
    name: 'DMI/ADX Condition',
    description: 'Analyzes trend strength and direction using Directional Movement Index',
    parameters: {
      period: { type: 'number' as const, default: 14, label: 'Period' },
      signalType: { 
        type: 'string' as const, 
        default: 'PLUS_DI_ABOVE_MINUS_DI', 
        label: 'Signal Type',
        options: [
          { value: 'PLUS_DI_ABOVE_MINUS_DI', label: '+DI Above -DI' },
          { value: 'MINUS_DI_ABOVE_PLUS_DI', label: '-DI Above +DI' },
          { value: 'PLUS_DI_CROSSES_ABOVE_MINUS_DI', label: '+DI Crosses Above -DI' },
          { value: 'MINUS_DI_CROSSES_ABOVE_PLUS_DI', label: '-DI Crosses Above +DI' },
          { value: 'ADX_ABOVE_THRESHOLD', label: 'ADX Above Threshold' },
          { value: 'ADX_BELOW_THRESHOLD', label: 'ADX Below Threshold' },
          { value: 'ADX_RISING', label: 'ADX Rising' },
          { value: 'ADX_FALLING', label: 'ADX Falling' },
          { value: 'STRONG_TREND', label: 'Strong Trend' },
          { value: 'STRONG_BULLISH', label: 'Strong Bullish' },
          { value: 'STRONG_BEARISH', label: 'Strong Bearish' },
          { value: 'WEAK_TREND', label: 'Weak Trend' },
          { value: 'DI_DIVERGENCE', label: 'DI Divergence' }
        ]
      },
      threshold: { type: 'number' as const, default: 25.0, label: 'ADX Threshold', step: 0.1 },
      divergenceThreshold: { type: 'number' as const, default: 10.0, label: 'Divergence Threshold', step: 0.1 }
    }
  },
  'ICHIMOKU_CLOUD': {
    name: 'Ichimoku Cloud',
    description: 'Comprehensive trend analysis using multiple Ichimoku components',
    parameters: {
      tenkanPeriod: { type: 'number' as const, default: 9, label: 'Tenkan Period' },
      kijunPeriod: { type: 'number' as const, default: 26, label: 'Kijun Period' },
      chikouPeriod: { type: 'number' as const, default: 52, label: 'Chikou Period' },
      signalType: {
        type: 'string' as const,
        default: 'PRICE_ABOVE_CLOUD',
        label: 'Signal Type',
        options: [
          { value: 'TENKAN_CROSSES_ABOVE_KIJUN', label: 'Tenkan Crosses Above Kijun' },
          { value: 'TENKAN_CROSSES_BELOW_KIJUN', label: 'Tenkan Crosses Below Kijun' },
          { value: 'PRICE_ABOVE_CLOUD', label: 'Price Above Cloud' },
          { value: 'PRICE_BELOW_CLOUD', label: 'Price Below Cloud' },
          { value: 'PRICE_IN_CLOUD', label: 'Price In Cloud' },
          { value: 'BULLISH_CLOUD', label: 'Bullish Cloud' },
          { value: 'BEARISH_CLOUD', label: 'Bearish Cloud' },
          { value: 'CHIKOU_ABOVE_PRICE', label: 'Chikou Above Price' },
          { value: 'CHIKOU_BELOW_PRICE', label: 'Chikou Below Price' },
          { value: 'STRONG_BULLISH', label: 'Strong Bullish' },
          { value: 'STRONG_BEARISH', label: 'Strong Bearish' }
        ]
      }
    }
  },
  'OBV': {
    name: 'On-Balance Volume',
    description: 'Tracks volume flow to predict price movements and confirm trends',
    parameters: {
      period: { type: 'number' as const, default: 20, label: 'Period' },
      conditionType: {
        type: 'string' as const,
        default: 'ABOVE_MA',
        label: 'Condition Type',
        options: [
          { value: 'ABOVE_MA', label: 'Above Moving Average' },
          { value: 'BELOW_MA', label: 'Below Moving Average' },
          { value: 'CROSS_ABOVE_MA', label: 'Cross Above Moving Average' },
          { value: 'CROSS_BELOW_MA', label: 'Cross Below Moving Average' },
          { value: 'INCREASING', label: 'Increasing' },
          { value: 'DECREASING', label: 'Decreasing' }
        ]
      }
    }
  },
  'PIVOT_POINTS': {
    name: 'Pivot Points',
    description: 'Identifies key support and resistance levels based on previous period',
    parameters: {
      pivotType: {
        type: 'string' as const,
        default: 'STANDARD',
        label: 'Pivot Type',
        options: [
          { value: 'STANDARD', label: 'Standard' },
          { value: 'FIBONACCI', label: 'Fibonacci' },
          { value: 'WOODIE', label: 'Woodie' },
          { value: 'CAMARILLA', label: 'Camarilla' },
          { value: 'DEMARK', label: 'DeMark' }
        ]
      },
      pivotLevel: {
        type: 'string' as const,
        default: 'PP',
        label: 'Pivot Level',
        options: [
          { value: 'PP', label: 'Pivot Point' },
          { value: 'R1', label: 'Resistance 1' },
          { value: 'R2', label: 'Resistance 2' },
          { value: 'R3', label: 'Resistance 3' },
          { value: 'S1', label: 'Support 1' },
          { value: 'S2', label: 'Support 2' },
          { value: 'S3', label: 'Support 3' }
        ]
      },
      crossAbove: { type: 'boolean' as const, default: true, label: 'Cross Above' },
      useClose: { type: 'boolean' as const, default: true, label: 'Use Close Price' }
    }
  },
  'ROC': {
    name: 'Rate of Change',
    description: 'Measures momentum by comparing current price to price N periods ago',
    parameters: {
      period: { type: 'number' as const, default: 12, label: 'Period' },
      threshold: { type: 'number' as const, default: 5, label: 'Threshold %', step: 0.1 },
      direction: {
        type: 'string' as const,
        default: 'ABOVE',
        label: 'Direction',
        options: [
          { value: 'ABOVE', label: 'Above' },
          { value: 'BELOW', label: 'Below' },
          { value: 'EQUAL', label: 'Equal' },
          { value: 'CROSSING_ABOVE', label: 'Crossing Above' },
          { value: 'CROSSING_BELOW', label: 'Crossing Below' }
        ]
      }
    }
  },
  'ROC_CROSSOVER': {
    name: 'ROC Crossover',
    description: 'Detects when Rate of Change crosses above or below threshold level',
    parameters: {
      period: { type: 'number' as const, default: 12, label: 'Period' },
      threshold: { type: 'number' as const, default: 0, label: 'Threshold %', step: 0.1 },
      crossAbove: { type: 'boolean' as const, default: true, label: 'Cross Above' }
    }
  },
  'ROC_DIVERGENCE': {
    name: 'ROC Divergence',
    description: 'Identifies bullish/bearish divergences between price and ROC momentum',
    parameters: {
      period: { type: 'number' as const, default: 12, label: 'ROC Period' },
      divergencePeriod: { type: 'number' as const, default: 20, label: 'Divergence Period' },
      bullish: { type: 'boolean' as const, default: true, label: 'Bullish Divergence' }
    }
  },
  'FIBONACCI_RETRACEMENT': {
    name: 'Fibonacci Retracement',
    description: 'Identifies potential support/resistance levels using Fibonacci ratios',
    parameters: {
      lookbackPeriod: { type: 'number' as const, default: 50, label: 'Lookback Period' },
      level: { 
        type: 'number' as const, 
        default: 0.618, 
        label: 'Fibonacci Level',
        options: [
          { value: 0.236, label: '23.6%' },
          { value: 0.382, label: '38.2%' },
          { value: 0.5, label: '50%' },
          { value: 0.618, label: '61.8%' },
          { value: 0.786, label: '78.6%' }
        ]
      },
      isBullish: { type: 'boolean' as const, default: true, label: 'Bullish Retracement' },
      tolerance: { type: 'number' as const, default: 0.01, label: 'Tolerance', step: 0.001 }
    }
  }
};
