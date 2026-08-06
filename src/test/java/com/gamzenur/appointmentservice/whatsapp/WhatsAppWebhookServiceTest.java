package com.gamzenur.appointmentservice.whatsapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamzenur.appointmentservice.config.WhatsAppProperties;
import com.gamzenur.appointmentservice.exception.WebhookSignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class WhatsAppWebhookServiceTest {

    private static final String APP_SECRET = "test-app-secret";

    private WhatsAppMessagePublisher publisher;
    private WhatsAppWebhookService service;

    @BeforeEach
    void setUp() {
        WhatsAppProperties properties = new WhatsAppProperties();
        properties.setAppSecret(APP_SECRET);
        properties.setVerifyToken("test-verify-token");
        publisher = mock(WhatsAppMessagePublisher.class);
        service = new WhatsAppWebhookService(properties, publisher, new ObjectMapper());
    }

    @Test
    void validSignaturePublishesMessageReceivedEvent() throws Exception {
        String payload = """
                {
                  "entry": [{
                    "changes": [{
                      "value": {
                        "messages": [{
                          "from": "905321234567",
                          "id": "wamid.test-001",
                          "timestamp": "1912345678",
                          "type": "text",
                          "text": {"body": "Yarın saat 10 için randevu istiyorum"}
                        }]
                      }
                    }]
                  }]
                }
                """;

        service.process(payload, sign(payload), "correlation-test-001");

        ArgumentCaptor<MessageReceivedEvent> eventCaptor =
                ArgumentCaptor.forClass(MessageReceivedEvent.class);
        verify(publisher).publish(eventCaptor.capture());
        MessageReceivedEvent event = eventCaptor.getValue();
        assertEquals("message.received", event.eventType());
        assertEquals("wamid.test-001", event.messageId());
        assertEquals("905321234567", event.customerPhone());
        assertEquals("Yarın saat 10 için randevu istiyorum", event.text());
        assertEquals("correlation-test-001", event.correlationId());
    }

    @Test
    void invalidSignatureIsRejected() {
        assertThrows(
                WebhookSignatureException.class,
                () -> service.process("{}", "sha256=00", "correlation-test-002")
        );
    }

    private String sign(String payload) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(APP_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return "sha256=" + HexFormat.of().formatHex(
                mac.doFinal(payload.getBytes(StandardCharsets.UTF_8))
        );
    }
}
