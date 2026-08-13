package com.gamzenur.notificationservice.websocket;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.OffsetDateTime;

public record RealtimeEvent(
        String event,
        OffsetDateTime timestamp,
        String correlationId,
        JsonNode payload
) {
}
