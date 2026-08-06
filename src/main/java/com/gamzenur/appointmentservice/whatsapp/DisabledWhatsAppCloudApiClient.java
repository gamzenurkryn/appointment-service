package com.gamzenur.appointmentservice.whatsapp;

import com.gamzenur.appointmentservice.exception.WhatsAppNotConfiguredException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "integration.whatsapp", name = "cloud-api-enabled", havingValue = "false", matchIfMissing = true)
public class DisabledWhatsAppCloudApiClient implements WhatsAppCloudApiClient {

    @Override
    public String sendTextMessage(String recipientPhone, String text) {
        throw new WhatsAppNotConfiguredException(
                "WhatsApp Cloud API kapalı. WHATSAPP_CLOUD_API_ENABLED, WHATSAPP_PHONE_NUMBER_ID ve WHATSAPP_ACCESS_TOKEN ayarlanmalıdır."
        );
    }
}
