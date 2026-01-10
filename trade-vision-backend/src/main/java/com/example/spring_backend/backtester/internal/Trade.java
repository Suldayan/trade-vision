package com.example.spring_backend.backtester.internal;

import java.time.LocalDateTime;

public record Trade(
        double entryPrice,
        double exitPrice,
        double positionSize,
        double pnl,
        LocalDateTime date
) {
}
