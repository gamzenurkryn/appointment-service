package com.gamzenur.appointmentservice.whatsapp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamzenur.appointmentservice.config.WhatsAppProperties;
import com.gamzenur.appointmentservice.exception.BadRequestException;
import com.gamzenur.appointmentservice.exception.WebhookSignatureException;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class WhatsAppWebhookService {

    private static final String SIGNATURE_PREFIX = "sha256=";

    private final WhatsAppProperties properties;
    private final WhatsAppMessagePublisher messagePublisher;
    private final ObjectMapper objectMapper;

    public WhatsAppWebhookService(
            WhatsAppProperties properties,
            WhatsAppMessagePublisher messagePublisher,
            ObjectMapper objectMapper
    ) {
        this.properties = properties;
        this.messagePublisher = messagePublisher;
        this.objectMapper = objectMapper;
    }

    public void process(String rawBody, String signatureHeader, String correlationId) {
        verifySignature(rawBody, signatureHeader);
        parseMessages(rawBody, correlationId).forEach(messagePublisher::publish);
    }

    private void verifySignature(String rawBody, String signatureHeader) {
        if (signatureHeader == null || !signatureHeader.startsWith(SIGNATURE_PREFIX)) {
            throw new WebhookSignatureException("WhatsApp webhook imzası eksik veya geçersiz.");
        }

        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    properties.getAppSecret().getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            ));
            byte[] expectedSignature = mac.doFinal(rawBody.getBytes(StandardCharsets.UTF_8));
            byte[] receivedSignature = HexFormat.of().parseHex(
                    signatureHeader.substring(SIGNATURE_PREFIX.length())
            );

            if (!MessageDigest.isEqual(expectedSignature, receivedSignature)) {
                throw new WebhookSignatureException("WhatsApp webhook imzası doğrulanamadı.");
            }
        } catch (GeneralSecurityException | IllegalArgumentException exception) {
            throw new WebhookSignatureException("WhatsApp webhook imzası doğrulanamadı.");
        }
    }

    private List<MessageReceivedEvent> parseMessages(String rawBody, String correlationId) {
        try {
            JsonNode root = objectMapper.readTree(rawBody);
            List<MessageReceivedEvent> events = new ArrayList<>();

            for (JsonNode entry : root.path("entry")) {
                for (JsonNode change : entry.path("changes")) {
                    for (JsonNode message : change.path("value").path("messages")) {
                        events.add(toEvent(message, correlationId));
                    }
                }
            }
            return events;
        } catch (JsonProcessingException exception) {
            throw new BadRequestException("WhatsApp webhook JSON gövdesi okunamadı.");
        }
    }

    private MessageReceivedEvent toEvent(JsonNode message, String correlationId) {
        String messageType = message.path("type").asText();
        String text = "text".equals(messageType)
                ? message.path("text").path("body").asText(null)
                : null;

        return new MessageReceivedEvent(
                UUID.randomUUID(),
                "message.received",
                OffsetDateTime.now(),
                correlationId,
                message.path("id").asText(),
                message.path("from").asText(),
                messageType,
                text,
                message.deepCopy()
        );
    }
}
