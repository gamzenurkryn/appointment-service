package com.gamzenur.appointmentservice.whatsapp;

import com.gamzenur.appointmentservice.dto.SendWhatsAppMessageRequest;
import com.gamzenur.appointmentservice.dto.SendWhatsAppMessageResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WhatsAppMessageServiceTest {

    @Test
    void shouldSendTextMessageAndReturnMetaMessageId() {
        RecordingClient client = new RecordingClient();
        WhatsAppMessageService service = new WhatsAppMessageService(client);
        SendWhatsAppMessageRequest request = new SendWhatsAppMessageRequest();
        request.setTo("+905321234567");
        request.setText("Randevunuz oluşturuldu.");

        SendWhatsAppMessageResponse response = service.sendTextMessage(request);

        assertThat(client.recipientPhone).isEqualTo("905321234567");
        assertThat(client.text).isEqualTo("Randevunuz oluşturuldu.");
        assertThat(response.getMessageId()).isEqualTo("wamid.test-001");
        assertThat(response.getStatus()).isEqualTo("ACCEPTED");
    }

    private static class RecordingClient implements WhatsAppCloudApiClient {
        private String recipientPhone;
        private String text;

        @Override
        public String sendTextMessage(String recipientPhone, String text) {
            this.recipientPhone = recipientPhone;
            this.text = text;
            return "wamid.test-001";
        }
    }
}
