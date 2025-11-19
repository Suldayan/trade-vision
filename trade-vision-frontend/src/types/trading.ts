export interface Condition {
  type: string;
  parameters: Record<string, any>;
}

export interface LogicalGroup {
  type: 'AND' | 'OR' | 'NOT';
  parameters: {
    conditions: Condition[];
  };
}

export type ConditionOrGroup = Condition | LogicalGroup;

export interface BacktestConfig {
  initialCapital: number;
  riskPerTrade: number;
  commissionRate: number;
  slippagePercent: number;
  allowShort: boolean;
  requireAllEntryConditions: boolean;
  requireAllExitConditions: boolean;
  entryConditions: ConditionOrGroup[];
  exitConditions: ConditionOrGroup[];
}

export type ConditionsPath = 'entryConditions' | 'exitConditions';
export type LogicalOperator = 'AND' | 'OR' | 'NOT';
