package com.example.trade_vision_backend.processing.internal.infrastructure.exception;

public class ProcessingException extends Exception {
    public ProcessingException(String msg, Throwable e) {
        super(msg, e);
    }
}
