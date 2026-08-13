package com.gamzenur.notificationservice.sms;

public record SmsProviderResult(
        String messageId,
        String status,
        String provider
) {
}
