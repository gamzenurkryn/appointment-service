package com.gamzenur.notificationservice.sms;

public interface SmsProvider {

    SmsProviderResult send(String phone, String message);
}
