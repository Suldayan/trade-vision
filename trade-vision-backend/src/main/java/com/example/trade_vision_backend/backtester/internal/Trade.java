package com.example.trade_vision_backend.backtester.internal;

public record Trade(
        double entryPrice,
        double exitPrice,
        double positionSize,
        double pnl) {
}
