package com.gamzenur.whatsappservice.messaging;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.OffsetDateTime;
import java.util.UUID;

public record MessageReceivedEvent(UUID eventId, String eventType, OffsetDateTime occurredAt,
                                   String correlationId, String messageId,
                                   @JsonProperty("from") String customerPhone, UUID storeId,
                                   String messageType, String text, JsonNode rawMessage) { }
