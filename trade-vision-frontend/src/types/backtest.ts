export interface ConditionConfig {
    type: string;
    parameters: Record<string, any>;
}
  
export interface BackTestRequest {
    initialCapital: number;
    riskPerTrade: number;
    commissionRate: number;
    slippagePercent: number;
    allowShort: boolean;
    entryConditions: ConditionConfig[];
    exitConditions: ConditionConfig[];
    requireAllEntryConditions: boolean;
    requireAllExitConditions: boolean;
}

export interface Trade {
    entryPrice: number;
    exitPrice: number;
    positionSize: number;
    pnl: number;
}

export interface BackTestResult {
    totalReturn: number;
    finalCapital: number;
    tradeCount: number;
    winRatio: number;
    maxDrawdown: number;
    trades: Trade[];
    equityCurve: number[];
}