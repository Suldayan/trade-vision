package com.example.trade_vision_backend.backtester.internal;

import lombok.Getter;

@Getter
public record Trade(double entryPrice, double exitPrice, double positionSize, double pnl) {
}
