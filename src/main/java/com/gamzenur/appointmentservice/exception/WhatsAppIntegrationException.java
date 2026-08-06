package com.gamzenur.appointmentservice.exception;

public class WhatsAppIntegrationException extends RuntimeException {

    public WhatsAppIntegrationException(String message) {
        super(message);
    }

    public WhatsAppIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
