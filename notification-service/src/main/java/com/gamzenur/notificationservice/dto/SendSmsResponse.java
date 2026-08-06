package com.gamzenur.notificationservice.dto;

import java.time.OffsetDateTime;

public record SendSmsResponse(
        String messageId,
        String status,
        String provider,
        OffsetDateTime acceptedAt
) {
}
