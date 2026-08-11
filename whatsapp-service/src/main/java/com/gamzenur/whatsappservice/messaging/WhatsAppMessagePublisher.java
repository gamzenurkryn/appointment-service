package com.gamzenur.whatsappservice.messaging;

public interface WhatsAppMessagePublisher {
    void publish(MessageReceivedEvent event);
}
