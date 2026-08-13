package com.gamzenur.callservice.exception;

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

    @ExceptionHandler(CallNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(CallNotFoundException exception, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "CALL_NOT_FOUND", exception.getMessage(), request);
    }

    @ExceptionHandler(CallServiceException.class)
    public ResponseEntity<ApiError> handleIntegration(CallServiceException exception, HttpServletRequest request) {
        return build(HttpStatus.BAD_GATEWAY, "APPOINTMENT_SERVICE_ERROR", exception.getMessage(), request);
    }

    @ExceptionHandler(LiveKitIntegrationException.class)
    public ResponseEntity<ApiError> handleLiveKit(
            LiveKitIntegrationException exception,
            HttpServletRequest request
    ) {
        return build(HttpStatus.BAD_GATEWAY, "LIVEKIT_ERROR", exception.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, request);
    }

    private ResponseEntity<ApiError> build(
            HttpStatus status,
            String code,
            String message,
            HttpServletRequest request
    ) {
        ApiError error = new ApiError();
        error.setTimestamp(OffsetDateTime.now());
        error.setStatus(status.value());
        error.setError(code);
        error.setMessage(message);
        error.setCorrelationId(request.getHeader("X-Correlation-Id"));
        return ResponseEntity.status(status).body(error);
    }
}
