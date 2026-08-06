package com.gamzenur.appointmentservice.controller;

import com.gamzenur.appointmentservice.config.CorrelationIdFilter;
import com.gamzenur.appointmentservice.config.WhatsAppProperties;
import com.gamzenur.appointmentservice.whatsapp.WhatsAppWebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/whatsapp/webhook")
@Tag(name = "WhatsApp Webhook", description = "Meta webhook doğrulama ve gelen mesaj işlemleri")
public class WhatsAppWebhookController {

    private final WhatsAppProperties properties;
    private final WhatsAppWebhookService webhookService;

    public WhatsAppWebhookController(
            WhatsAppProperties properties,
            WhatsAppWebhookService webhookService
    ) {
        this.properties = properties;
        this.webhookService = webhookService;
    }

    @GetMapping(produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "Meta webhook adresini doğrula", description = "Verify token doğruysa Meta tarafından gönderilen challenge değerini döndürür.")
    public ResponseEntity<String> verifyWebhook(
            @RequestParam(name = "hub.mode") String mode,
            @RequestParam(name = "hub.verify_token") String verifyToken,
            @RequestParam(name = "hub.challenge") String challenge
    ) {
        if ("subscribe".equals(mode) && properties.getVerifyToken().equals(verifyToken)) {
            return ResponseEntity.ok(challenge);
        }
        return ResponseEntity.status(403).body("Webhook verification failed");
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "WhatsApp webhook olayını al", description = "HMAC imzasını doğrular ve gelen mesaj olayını RabbitMQ'ya yayınlar.")
    public ResponseEntity<Void> receiveWebhook(
            @RequestBody String rawBody,
            @RequestHeader(name = "X-Hub-Signature-256", required = false) String signature,
            HttpServletRequest request
    ) {
        String correlationId = (String) request.getAttribute(CorrelationIdFilter.ATTRIBUTE_NAME);
        webhookService.process(rawBody, signature, correlationId);
        return ResponseEntity.ok().build();
    }
}
