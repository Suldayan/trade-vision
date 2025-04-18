package com.example.trade_vision_backend.strategies.internal;

public class StrategyCalculationException extends RuntimeException {
    public StrategyCalculationException(String msg) {
        super(msg);
    }

    public StrategyCalculationException(String msg, Throwable t) {
        super(msg, t);
    }
}
