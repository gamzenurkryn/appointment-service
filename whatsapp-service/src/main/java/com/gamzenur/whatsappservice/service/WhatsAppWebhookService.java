package com.gamzenur.whatsappservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamzenur.whatsappservice.config.WhatsAppProperties;
import com.gamzenur.whatsappservice.exception.WhatsAppExceptions;
import com.gamzenur.whatsappservice.messaging.MessageReceivedEvent;
import com.gamzenur.whatsappservice.messaging.WhatsAppMessagePublisher;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class WhatsAppWebhookService {
    private static final String PREFIX = "sha256=";
    private final WhatsAppProperties properties;
    private final WhatsAppMessagePublisher publisher;
    private final ObjectMapper mapper;

    public WhatsAppWebhookService(WhatsAppProperties properties, WhatsAppMessagePublisher publisher,
                                  ObjectMapper mapper) {
        this.properties = properties; this.publisher = publisher; this.mapper = mapper;
    }

    public void process(String body, String signature, String correlationId) {
        verifySignature(body, signature);
        try {
            JsonNode root = mapper.readTree(body);
            for (JsonNode entry : root.path("entry")) {
                for (JsonNode change : entry.path("changes")) {
                    for (JsonNode message : change.path("value").path("messages")) {
                        String type = message.path("type").asText();
                        String text = "text".equals(type) ? message.path("text").path("body").asText(null) : null;
                        publisher.publish(new MessageReceivedEvent(UUID.randomUUID(), "message.received",
                                OffsetDateTime.now(), correlationId, message.path("id").asText(),
                                message.path("from").asText(), type, text, message.deepCopy()));
                    }
                }
            }
        } catch (JsonProcessingException ex) {
            throw new WhatsAppExceptions.BadRequest("WhatsApp webhook JSON gövdesi okunamadı.");
        }
    }

    private void verifySignature(String body, String signature) {
        if (signature == null || !signature.startsWith(PREFIX))
            throw new WhatsAppExceptions.InvalidSignature("WhatsApp webhook imzası eksik veya geçersiz.");
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(properties.getAppSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] expected = mac.doFinal(body.getBytes(StandardCharsets.UTF_8));
            byte[] received = HexFormat.of().parseHex(signature.substring(PREFIX.length()));
            if (!MessageDigest.isEqual(expected, received))
                throw new WhatsAppExceptions.InvalidSignature("WhatsApp webhook imzası doğrulanamadı.");
        } catch (GeneralSecurityException | IllegalArgumentException ex) {
            throw new WhatsAppExceptions.InvalidSignature("WhatsApp webhook imzası doğrulanamadı.");
        }
    }
}
