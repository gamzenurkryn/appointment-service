package com.gamzenur.appointmentservice.whatsapp;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MessageReceivedEvent(
        UUID eventId,
        String eventType,
        OffsetDateTime occurredAt,
        String correlationId,
        String messageId,
        String customerPhone,
        String messageType,
        String text,
        JsonNode rawMessage
) {
}
