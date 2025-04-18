package com.example.trade_vision_backend.backtester.internal;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class BackTestRequest {
    // Data source information
    private String dataFilePath;

    // Initial capital and position sizing
    private double initialCapital;
    private double riskPerTrade;

    // Strategy configuration
    private List<ConditionConfig> entryConditions;
    private List<ConditionConfig> exitConditions;
    private boolean requireAllEntryConditions; // AND logic
    private boolean requireAllExitConditions; // OR logic

    // Trading parameters
    private boolean allowShort;
    private double commissionRate;
    private double slippagePercent;
}