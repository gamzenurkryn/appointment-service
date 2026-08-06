package com.gamzenur.notificationservice.service;

import com.gamzenur.notificationservice.dto.SendSmsRequest;
import com.gamzenur.notificationservice.dto.SendSmsResponse;
import com.gamzenur.notificationservice.sms.SmsProvider;
import com.gamzenur.notificationservice.sms.SmsProviderResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SmsServiceTest {

    @Test
    void sendsSmsThroughProviderAndRecordsAuditLog() {
        SmsProvider provider = mock(SmsProvider.class);
        LogEventService logEventService = mock(LogEventService.class);
        when(provider.send("+905551234567", "Randevunuz onaylandı."))
                .thenReturn(new SmsProviderResult("sms-123", "ACCEPTED", "DEMO"));
        SmsService service = new SmsService(provider, logEventService);

        SendSmsResponse response = service.send(
                new SendSmsRequest("+905551234567", "Randevunuz onaylandı."),
                "correlation-123"
        );

        assertThat(response.messageId()).isEqualTo("sms-123");
        assertThat(response.status()).isEqualTo("ACCEPTED");
        assertThat(response.provider()).isEqualTo("DEMO");
        verify(logEventService).recordSmsAccepted(
                eq("sms-123"),
                eq("DEMO"),
                eq("correlation-123"),
                any()
        );
    }
}
