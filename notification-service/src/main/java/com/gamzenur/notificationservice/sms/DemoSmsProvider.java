package com.gamzenur.notificationservice.sms;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DemoSmsProvider implements SmsProvider {

    @Override
    public SmsProviderResult send(String phone, String message) {
        return new SmsProviderResult(
                "demo-sms-" + UUID.randomUUID(),
                "ACCEPTED",
                "DEMO"
        );
    }
}
