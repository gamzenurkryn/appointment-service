package com.gamzenur.notificationservice.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CallStatsPublisher {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    private final Map<String, JsonNode> calls = new ConcurrentHashMap<>();

    public CallStatsPublisher(SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

    public void updateAndPublish(JsonNode eventPayload) {
        JsonNode call = eventPayload.path("call");
        String id = call.path("id").asText();
        if (id.isBlank()) {
            return;
        }
        calls.put(id, call.deepCopy());

        long activeCalls = calls.values().stream()
                .filter(value -> "ACTIVE".equals(value.path("status").asText()))
                .count();
        long participants = calls.values().stream()
                .filter(value -> "ACTIVE".equals(value.path("status").asText()))
                .mapToLong(value -> value.path("participantCount").asLong())
                .sum();
        long matched = calls.values().stream()
                .filter(value -> "MATCHED".equals(value.path("status").asText()))
                .count();

        messagingTemplate.convertAndSend(
                RealtimeEventPublisher.EVENTS_TOPIC,
                new RealtimeEvent(
                        "stats.updated",
                        java.time.OffsetDateTime.now(),
                        eventPayload.path("correlationId").asText(),
                        objectMapper.valueToTree(new CallStats(activeCalls, participants, matched))
                )
        );
    }
}
