export interface IndicatorParameter {
  label: string;
  type: 'number' | 'boolean' | 'string';
  default: number | boolean | string;
  step?: number;
  min?: number;
  max?: number;
  options?: Array<{ value: any; label: string }>;
}

export interface IndicatorDefinition {
  name: string;
  description: string;
  parameters: Record<string, IndicatorParameter>;
}

export interface ConditionParameters {
  [key: string]: number | boolean | string | AnyCondition | AnyCondition[];
}

export interface Condition {
  type: string;
  parameters: ConditionParameters;
}

export interface AndOrGroup {
  type: 'AND' | 'OR';
  parameters: {
    conditions: AnyCondition[];
  };
}

export interface NotGroup {
  type: 'NOT';
  parameters: {
    condition: AnyCondition; // Single condition, can be any condition or logical group
  };
}

export type LogicalGroup = AndOrGroup | NotGroup;

export type LogicalGroupCondition = LogicalGroup;

export type AnyCondition = Condition | LogicalGroup;

export type ConditionOrGroup = AnyCondition;

export interface StrategyConfig {
  initialCapital: number;
  riskPerTrade: number;
  commissionRate: number;
  slippagePercent: number;
  allowShort: boolean;
  requireAllEntryConditions: boolean;
  requireAllExitConditions: boolean;
  entryConditions: AnyCondition[];
  exitConditions: AnyCondition[];
}

export interface BacktestResults {
  totalReturn: number;
  finalCapital: number;
  tradeCount: number;
  winRatio: number;
  maxDrawdown: number;
  trades: Trade[];
  metrics: PerformanceMetrics;
  equityCurve: number[];
}

export interface Trade {
  entryPrice: number;
  exitPrice: number;
  positionSize: number;
  pnl: number;
  date: string; // The exit date, refers to when the trade is executed 
}

export interface PerformanceMetrics {
  totalReturn: number;
  totalReturnPercent: number;
  sharpeRatio: number;
  maxDrawdown: number;
  winRate: number;
  profitFactor: number;
  totalTrades: number;
  winningTrades: number;
  losingTrades: number;
}

export interface EquityPoint {
  date: string;
  equity: number;
  drawdown: number;
}

export interface ApiError {
  message: string;
  code?: string;
  details?: Record<string, any>;
}