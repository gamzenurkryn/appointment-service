package com.gamzenur.appointmentservice.exception;

import com.gamzenur.appointmentservice.config.CorrelationIdFilter;
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

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", exception.getMessage(), request);
    }

    @ExceptionHandler(SlotAlreadyBookedException.class)
    public ResponseEntity<ApiError> handleSlotAlreadyBooked(
            SlotAlreadyBookedException exception,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.CONFLICT, "SLOT_ALREADY_BOOKED", exception.getMessage(), request);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequest(
            BadRequestException exception,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.UNPROCESSABLE_ENTITY, "BUSINESS_RULE_VIOLATION", exception.getMessage(), request);
    }

    @ExceptionHandler(CalendarIntegrationException.class)
    public ResponseEntity<ApiError> handleCalendarIntegration(
            CalendarIntegrationException exception,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.BAD_GATEWAY,
                "GOOGLE_CALENDAR_ERROR",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(WebhookSignatureException.class)
    public ResponseEntity<ApiError> handleWebhookSignature(
            WebhookSignatureException exception,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.UNAUTHORIZED,
                "INVALID_WEBHOOK_SIGNATURE",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(WhatsAppIntegrationException.class)
    public ResponseEntity<ApiError> handleWhatsAppIntegration(
            WhatsAppIntegrationException exception,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.BAD_GATEWAY, "WHATSAPP_API_ERROR", exception.getMessage(), request);
    }

    @ExceptionHandler(WhatsAppNotConfiguredException.class)
    public ResponseEntity<ApiError> handleWhatsAppNotConfigured(
            WhatsAppNotConfiguredException exception,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.SERVICE_UNAVAILABLE, "WHATSAPP_NOT_CONFIGURED", exception.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return buildError(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, request);
    }

    private ResponseEntity<ApiError> buildError(
            HttpStatus status,
            String errorCode,
            String message,
            HttpServletRequest request
    ) {
        ApiError error = new ApiError();
        error.setTimestamp(OffsetDateTime.now());
        error.setStatus(status.value());
        error.setError(errorCode);
        error.setMessage(message);
        error.setCorrelationId((String) request.getAttribute(CorrelationIdFilter.ATTRIBUTE_NAME));
        return ResponseEntity.status(status).body(error);
    }
}
