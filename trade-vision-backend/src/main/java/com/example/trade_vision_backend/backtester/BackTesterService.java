package com.example.trade_vision_backend.backtester;

import com.example.trade_vision_backend.backtester.internal.BackTestResult;
import com.example.trade_vision_backend.common.BackTestRequest;
import com.example.trade_vision_backend.market.MarketData;
import com.example.trade_vision_backend.strategies.Strategy;

import java.util.concurrent.CompletableFuture;

public interface BackTesterService {
    BackTestResult runBackTest(Strategy strategy, MarketData marketData, BackTestRequest request);
    CompletableFuture<BackTestResult> runConcurrentBackTests(Strategy strategy, MarketData marketData, BackTestRequest request) throws InterruptedException;
}
