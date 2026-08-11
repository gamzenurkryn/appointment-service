package com.gamzenur.whatsappservice.controller;

import com.gamzenur.whatsappservice.config.CorrelationIdFilter;
import com.gamzenur.whatsappservice.config.WhatsAppProperties;
import com.gamzenur.whatsappservice.service.WhatsAppWebhookService;
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
public class WhatsAppWebhookController {

    private final WhatsAppProperties properties;
    private final WhatsAppWebhookService service;

    public WhatsAppWebhookController(
            WhatsAppProperties properties,
            WhatsAppWebhookService service) {
        this.properties = properties;
        this.service = service;
    }

    @GetMapping(produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> verify(
            @RequestParam(name = "hub.mode") String mode,
            @RequestParam(name = "hub.verify_token") String token,
            @RequestParam(name = "hub.challenge") String challenge) {

        boolean verified =
                "subscribe".equals(mode)
                        && properties.getVerifyToken().equals(token);

        if (verified) {
            return ResponseEntity.ok(challenge);
        }

        return ResponseEntity
                .status(403)
                .body("Webhook verification failed");
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> receive(
            @RequestBody String body,
            @RequestHeader(
                    name = "X-Hub-Signature-256",
                    required = false)
            String signature,
            HttpServletRequest request) {

        String correlationId =
                (String) request.getAttribute(CorrelationIdFilter.ATTRIBUTE_NAME);

        service.process(body, signature, correlationId);

        return ResponseEntity.ok().build();
    }
}