package com.gamzenur.appointmentservice.dto;

public class SendWhatsAppMessageResponse {

    private String messageId;
    private String status;

    public SendWhatsAppMessageResponse(String messageId, String status) {
        this.messageId = messageId;
        this.status = status;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getStatus() {
        return status;
    }
}
