package com.example.trade_vision_backend.strategies.internal;

public class StrategyCalculationException extends RuntimeException {
    StrategyCalculationException(String msg) {
        super(msg);
    }

    StrategyCalculationException(String msg, Throwable t) {
        super(msg, t);
    }
}
