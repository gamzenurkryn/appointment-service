package com.gamzenur.notificationservice.service;

import com.gamzenur.notificationservice.dto.SendSmsRequest;
import com.gamzenur.notificationservice.dto.SendSmsResponse;
import com.gamzenur.notificationservice.sms.SmsProvider;
import com.gamzenur.notificationservice.sms.SmsProviderResult;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class SmsService {

    private final SmsProvider smsProvider;
    private final LogEventService logEventService;

    public SmsService(SmsProvider smsProvider, LogEventService logEventService) {
        this.smsProvider = smsProvider;
        this.logEventService = logEventService;
    }

    public SendSmsResponse send(SendSmsRequest request, String correlationId) {
        SmsProviderResult result = smsProvider.send(request.phone(), request.message());
        OffsetDateTime acceptedAt = OffsetDateTime.now();
        logEventService.recordSmsAccepted(result.messageId(), result.provider(), correlationId, acceptedAt);
        return new SendSmsResponse(
                result.messageId(),
                result.status(),
                result.provider(),
                acceptedAt
        );
    }
}
