package com.gamzenur.whatsappservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamzenur.whatsappservice.config.WhatsAppProperties;
import com.gamzenur.whatsappservice.messaging.MessageReceivedEvent;
import com.gamzenur.whatsappservice.messaging.WhatsAppMessagePublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class WhatsAppWebhookServiceTest {

    private static final String APP_SECRET = "test-app-secret";
    private static final String PHONE_NUMBER_ID = "122255556612270";
    private static final UUID STORE_ID = UUID.fromString("b2c30000-0000-0000-0000-000000000001");
    private WhatsAppMessagePublisher publisher;
    private WhatsAppWebhookService service;

    @BeforeEach
    void setUp() {
        WhatsAppProperties properties = new WhatsAppProperties();
        properties.setAppSecret(APP_SECRET);
        publisher = mock(WhatsAppMessagePublisher.class);
        WhatsAppStoreResolver storeResolver = new WhatsAppStoreResolver(
                PHONE_NUMBER_ID + "=" + STORE_ID
        );
        service = new WhatsAppWebhookService(
                properties,
                publisher,
                new ObjectMapper(),
                storeResolver
        );
    }

    @Test
    void publishesMessageReceivedEventWhenSignatureIsValid() throws Exception {
        String body = """
                {
                  "entry": [{
                    "changes": [{
                      "value": {
                        "metadata": {
                          "phone_number_id": "122255556612270"
                        },
                        "messages": [{
                          "id": "wamid-test",
                          "from": "905551234567",
                          "type": "text",
                          "text": {"body": "Randevu almak istiyorum"}
                        }]
                      }
                    }]
                  }]
                }
                """;

        service.process(body, signatureFor(body), "correlation-test-001");

        ArgumentCaptor<MessageReceivedEvent> captor = ArgumentCaptor.forClass(MessageReceivedEvent.class);
        verify(publisher).publish(captor.capture());
        MessageReceivedEvent event = captor.getValue();
        assertThat(event.eventType()).isEqualTo("message.received");
        assertThat(event.correlationId()).isEqualTo("correlation-test-001");
        assertThat(event.messageId()).isEqualTo("wamid-test");
        assertThat(event.customerPhone()).isEqualTo("+905551234567");
        assertThat(event.storeId()).isEqualTo(STORE_ID);
        assertThat(event.text()).isEqualTo("Randevu almak istiyorum");
    }

    private String signatureFor(String body) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(APP_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return "sha256=" + HexFormat.of().formatHex(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));
    }
}
