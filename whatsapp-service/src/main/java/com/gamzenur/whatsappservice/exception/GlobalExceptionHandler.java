package com.gamzenur.whatsappservice.exception;

import com.gamzenur.whatsappservice.config.CorrelationIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(WhatsAppExceptions.BadRequest.class)
    ResponseEntity<ApiError> badRequest(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, "BUSINESS_RULE_VIOLATION", ex.getMessage(), req);
    }
    @ExceptionHandler(WhatsAppExceptions.InvalidSignature.class)
    ResponseEntity<ApiError> signature(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, "INVALID_WEBHOOK_SIGNATURE", ex.getMessage(), req);
    }
    @ExceptionHandler(WhatsAppExceptions.Integration.class)
    ResponseEntity<ApiError> integration(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_GATEWAY, "WHATSAPP_API_ERROR", ex.getMessage(), req);
    }
    @ExceptionHandler(WhatsAppExceptions.NotConfigured.class)
    ResponseEntity<ApiError> notConfigured(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, "WHATSAPP_NOT_CONFIGURED", ex.getMessage(), req);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage()).collect(Collectors.joining(", "));
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, req);
    }
    private ResponseEntity<ApiError> build(HttpStatus status, String code, String message, HttpServletRequest req) {
        return ResponseEntity.status(status).body(new ApiError(OffsetDateTime.now(), status.value(), code,
                message, (String) req.getAttribute(CorrelationIdFilter.ATTRIBUTE_NAME)));
    }
}
