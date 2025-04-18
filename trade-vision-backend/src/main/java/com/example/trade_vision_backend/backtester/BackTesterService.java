package com.example.trade_vision_backend.backtester;

import com.example.trade_vision_backend.backtester.internal.BackTestRequest;
import com.example.trade_vision_backend.backtester.internal.BackTestResult;
import com.example.trade_vision_backend.data.MarketData;
import com.example.trade_vision_backend.strategies.internal.Strategy;

public interface BackTesterService {
    BackTestResult runBackTest(Strategy strategy, MarketData marketData, BackTestRequest request);
}
