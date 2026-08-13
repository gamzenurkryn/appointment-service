package com.gamzenur.whatsappservice.service;

import com.gamzenur.whatsappservice.dto.SendWhatsAppMessageRequest;
import com.gamzenur.whatsappservice.dto.SendWhatsAppMessageResponse;
import com.gamzenur.whatsappservice.integration.WhatsAppCloudApiClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WhatsAppMessageServiceTest {

    @Test
    void removesPlusPrefixAndReturnsAcceptedResponse() {
        RecordingClient client = new RecordingClient();
        WhatsAppMessageService service = new WhatsAppMessageService(client);
        SendWhatsAppMessageRequest request = new SendWhatsAppMessageRequest();
        request.setTo("+905551234567");
        request.setText("Test mesajı");

        SendWhatsAppMessageResponse response = service.send(request);

        assertThat(client.phone).isEqualTo("905551234567");
        assertThat(client.text).isEqualTo("Test mesajı");
        assertThat(response.messageId()).isEqualTo("wamid-test");
        assertThat(response.status()).isEqualTo("ACCEPTED");
    }

    private static class RecordingClient implements WhatsAppCloudApiClient {
        private String phone;
        private String text;
        @Override public String sendTextMessage(String recipientPhone, String messageText) {
            phone = recipientPhone;
            text = messageText;
            return "wamid-test";
        }
    }
}
