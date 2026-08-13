package com.gamzenur.whatsappservice.integration;

public interface WhatsAppCloudApiClient {
    String sendTextMessage(String recipientPhone, String text);
}
