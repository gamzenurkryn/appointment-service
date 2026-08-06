package com.gamzenur.callservice.exception;

public class CallServiceException extends RuntimeException {

    public CallServiceException(String message) {
        super(message);
    }

    public CallServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
