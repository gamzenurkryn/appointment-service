package com.gamzenur.callservice.exception;

public class LiveKitIntegrationException extends RuntimeException {

    public LiveKitIntegrationException(String message) {
        super(message);
    }

    public LiveKitIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
