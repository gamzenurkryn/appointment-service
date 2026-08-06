package com.gamzenur.notificationservice.controller;

import com.gamzenur.notificationservice.dto.SendSmsRequest;
import com.gamzenur.notificationservice.dto.SendSmsResponse;
import com.gamzenur.notificationservice.service.SmsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications/sms")
@Tag(name = "SMS Bildirimleri", description = "Sağlayıcı bağımsız SMS gönderim işlemleri")
public class SmsController {

    private final SmsService smsService;

    public SmsController(SmsService smsService) {
        this.smsService = smsService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "SMS gönder", description = "Geliştirme ortamında DEMO sağlayıcısı isteği kabul eder; gerçek SMS göndermez.")
    public SendSmsResponse send(
            @Valid @RequestBody SendSmsRequest request,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId
    ) {
        String effectiveCorrelationId = correlationId == null || correlationId.isBlank()
                ? UUID.randomUUID().toString()
                : correlationId;
        return smsService.send(request, effectiveCorrelationId);
    }
}
