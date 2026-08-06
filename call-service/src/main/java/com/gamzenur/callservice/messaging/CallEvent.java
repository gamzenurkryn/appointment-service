package com.gamzenur.callservice.messaging;

import com.gamzenur.callservice.dto.CallResponse;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CallEvent(
        UUID eventId,
        String eventType,
        OffsetDateTime occurredAt,
        String correlationId,
        CallResponse call
) {
}
