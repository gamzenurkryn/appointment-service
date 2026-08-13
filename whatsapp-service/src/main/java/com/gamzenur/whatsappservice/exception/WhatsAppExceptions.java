package com.gamzenur.whatsappservice.exception;

public final class WhatsAppExceptions {
    private WhatsAppExceptions() { }

    public static class BadRequest extends RuntimeException {
        public BadRequest(String message) { super(message); }
    }
    public static class InvalidSignature extends RuntimeException {
        public InvalidSignature(String message) { super(message); }
    }
    public static class Integration extends RuntimeException {
        public Integration(String message) { super(message); }
        public Integration(String message, Throwable cause) { super(message, cause); }
    }
    public static class NotConfigured extends RuntimeException {
        public NotConfigured(String message) { super(message); }
    }
}
