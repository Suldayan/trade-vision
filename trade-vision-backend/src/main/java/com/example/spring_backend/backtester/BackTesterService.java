package com.example.spring_backend.backtester;

import com.example.spring_backend.backtester.internal.BackTestResult;
import com.example.spring_backend.common.BackTestRequest;
import com.example.spring_backend.market.MarketData;
import com.example.spring_backend.strategies.Strategy;

public interface BackTesterService {
    BackTestResult runBackTest(Strategy strategy, MarketData marketData, BackTestRequest request);
}
