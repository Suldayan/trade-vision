package com.example.trade_vision_backend.ingestion.internal.infrastructure.exception;

public class IngestionException extends Exception {
    public IngestionException(String msg, Throwable e) {
        super(msg, e);
    }
}
