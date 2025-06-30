package com.example.trade_vision_backend.backtester.internal;

public class BackTesterExceptions {

    public static class BackTestException extends RuntimeException {
        public BackTestException(String message, Throwable cause) {
            super(message, cause);
        }
        public BackTestException(String message) {
            super(message);
        }
    }

    public static class InvalidRequestException extends BackTestException {
        public InvalidRequestException(String message, Throwable cause) {
            super(message, cause);
        }
        public InvalidRequestException(String message) {
            super(message);
        }
    }

    public static class BackTestOrchestrationException extends BackTestException {
        public BackTestOrchestrationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}