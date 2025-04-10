package com.example.trade_vision_backend.data;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public record Bar(
        LocalDateTime dateTime,
        double open, double high,
        double low,
        double close,
        double adjClose,
        double volume) {

    @Override
    public String toString() {
        return String.format(
                "Bar[date=%s, OHLC=%.2f/%.2f/%.2f/%.2f, volume=%.0f]",
                dateTime,
                open, high, low, close,
                volume
        );
    }
}
