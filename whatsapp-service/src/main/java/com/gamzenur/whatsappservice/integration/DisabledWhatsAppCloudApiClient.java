package com.gamzenur.whatsappservice.integration;

import com.gamzenur.whatsappservice.exception.WhatsAppExceptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "integration.whatsapp", name = "cloud-api-enabled", havingValue = "false", matchIfMissing = true)
public class DisabledWhatsAppCloudApiClient implements WhatsAppCloudApiClient {
    @Override public String sendTextMessage(String recipientPhone, String text) {
        throw new WhatsAppExceptions.NotConfigured("WhatsApp Cloud API kapalı veya ayarları eksik.");
    }
}
