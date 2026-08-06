package com.gamzenur.appointmentservice.whatsapp;

public interface WhatsAppMessagePublisher {

    void publish(MessageReceivedEvent event);
}
