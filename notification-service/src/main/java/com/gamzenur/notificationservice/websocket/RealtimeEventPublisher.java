package com.gamzenur.notificationservice.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.UUID;

@Component
public class RealtimeEventPublisher {

    public static final String EVENTS_TOPIC = "/topic/events";

    private final SimpMessagingTemplate messagingTemplate;

    public RealtimeEventPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publish(String eventType, JsonNode payload) {
        String correlationId = payload.path("correlationId").asText();
        if (!StringUtils.hasText(correlationId)) {
            correlationId = UUID.randomUUID().toString();
        }

        RealtimeEvent event = new RealtimeEvent(
                eventType,
                OffsetDateTime.now(),
                correlationId,
                payload
        );
        messagingTemplate.convertAndSend(EVENTS_TOPIC, event);
    }
}
