package com.gamzenur.appointmentservice.whatsapp;

import com.gamzenur.appointmentservice.dto.SendWhatsAppMessageRequest;
import com.gamzenur.appointmentservice.dto.SendWhatsAppMessageResponse;
import org.springframework.stereotype.Service;

@Service
public class WhatsAppMessageService {

    private final WhatsAppCloudApiClient cloudApiClient;

    public WhatsAppMessageService(WhatsAppCloudApiClient cloudApiClient) {
        this.cloudApiClient = cloudApiClient;
    }

    public SendWhatsAppMessageResponse sendTextMessage(SendWhatsAppMessageRequest request) {
        String recipientPhone = request.getTo().substring(1);
        String messageId = cloudApiClient.sendTextMessage(recipientPhone, request.getText());
        return new SendWhatsAppMessageResponse(messageId, "ACCEPTED");
    }
}
