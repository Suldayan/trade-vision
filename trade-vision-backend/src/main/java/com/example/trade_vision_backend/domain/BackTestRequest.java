package com.example.trade_vision_backend.domain;


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

// Example json from a user p
/*
{
  "initialCapital": 10000.0,
  "riskPerTrade": 0.02,
  "commissionRate": 0.001,
  "slippagePercent": 0.1,
  "allowShort": false,

  "entryConditions": [
    {
      "type": "OR",
      "parameters": {
        "conditions": [
          {
            "type": "AND",
            "parameters": {
              "conditions": [
                {
                  "type": "RSI_THRESHOLD",
                  "parameters": {
                    "period": 14,
                    "upperThreshold": 70,
                    "lowerThreshold": 30,
                    "checkOverbought": false
                  }
                },
                {
                  "type": "SMA_CROSSOVER",
                  "parameters": {
                    "fastPeriod": 5,
                    "slowPeriod": 20,
                    "crossAbove": true
                  }
                }
              ]
            }
          },
          {
            "type": "MACD_CROSSOVER",
            "parameters": {
              "fastPeriod": 12,
              "slowPeriod": 26,
              "signalPeriod": 9,
              "crossAbove": true
            }
          }
        ]
      }
    }
  ],

  "exitConditions": [
    {
      "type": "RSI_THRESHOLD",
      "parameters": {
        "period": 14,
        "upperThreshold": 70,
        "lowerThreshold": 30,
        "checkOverbought": true
      }
    }
  ],

  "requireAllEntryConditions": true,
  "requireAllExitConditions": false
}
* */