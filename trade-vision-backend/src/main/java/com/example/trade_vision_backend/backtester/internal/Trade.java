package com.example.trade_vision_backend.backtester.internal;

import java.time.LocalDateTime;

public record Trade(
        double entryPrice,
        double exitPrice,
        double positionSize,
        double pnl,
        LocalDateTime date
) {
}
