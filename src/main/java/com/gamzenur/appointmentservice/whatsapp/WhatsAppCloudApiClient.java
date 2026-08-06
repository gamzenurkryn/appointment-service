package com.gamzenur.appointmentservice.whatsapp;

public interface WhatsAppCloudApiClient {

    String sendTextMessage(String recipientPhone, String text);
}
