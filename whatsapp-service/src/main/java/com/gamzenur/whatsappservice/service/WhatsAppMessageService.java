package com.gamzenur.whatsappservice.service;

import com.gamzenur.whatsappservice.dto.SendWhatsAppMessageRequest;
import com.gamzenur.whatsappservice.dto.SendWhatsAppMessageResponse;
import com.gamzenur.whatsappservice.integration.WhatsAppCloudApiClient;
import org.springframework.stereotype.Service;

@Service
public class WhatsAppMessageService {
    private final WhatsAppCloudApiClient client;
    public WhatsAppMessageService(WhatsAppCloudApiClient client) { this.client = client; }
    public SendWhatsAppMessageResponse send(SendWhatsAppMessageRequest request) {
        return new SendWhatsAppMessageResponse(client.sendTextMessage(request.getTo().substring(1), request.getText()), "ACCEPTED");
    }
}
