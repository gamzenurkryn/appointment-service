package com.gamzenur.appointmentservice.whatsapp;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "integration.rabbitmq.enabled",
        havingValue = "false",
        matchIfMissing = true
)
public class DisabledWhatsAppMessagePublisher implements WhatsAppMessagePublisher {

    @Override
    public void publish(MessageReceivedEvent event) {
    }
}
