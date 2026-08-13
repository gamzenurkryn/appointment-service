package com.gamzenur.notificationservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamzenur.notificationservice.domain.LogEvent;
import com.gamzenur.notificationservice.domain.LogLevel;
import com.gamzenur.notificationservice.dto.LogEventResponse;
import com.gamzenur.notificationservice.repository.LogEventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.time.Instant;
import java.time.ZoneOffset;
import java.math.BigDecimal;
import java.util.UUID;

@Service
public class LogEventService {

    private final LogEventRepository repository;
    private final ObjectMapper objectMapper;

    public LogEventService(LogEventRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void recordRabbitEvent(String routingKey, JsonNode payload) {
        LogEvent event = new LogEvent();
        event.setId(UUID.randomUUID());
        event.setTimestamp(readTimestamp(payload));
        event.setLevel(LogLevel.INFO);
        event.setService(serviceName(routingKey));
        event.setMessage("RabbitMQ olayı alındı: " + routingKey);
        event.setCorrelationId(readCorrelationId(payload));
        event.setMetaJson(safeMeta(payload).toString());
        repository.save(event);
    }

    @Transactional
    public void recordSmsAccepted(
            String messageId,
            String provider,
            String correlationId,
            OffsetDateTime acceptedAt
    ) {
        LogEvent event = new LogEvent();
        event.setId(UUID.randomUUID());
        event.setTimestamp(acceptedAt);
        event.setLevel(LogLevel.INFO);
        event.setService("notification-service");
        event.setMessage("SMS gönderim isteği kabul edildi.");
        event.setCorrelationId(correlationId);
        event.setMetaJson(objectMapper.createObjectNode()
                .put("messageId", messageId)
                .put("provider", provider)
                .toString());
        repository.save(event);
    }

    @Transactional(readOnly = true)
    public Page<LogEventResponse> getLogs(
            String service,
            LogLevel level,
            String correlationId,
            Pageable pageable
    ) {
        Specification<LogEvent> specification = Specification.allOf();
        if (StringUtils.hasText(service)) {
            specification = specification.and((root, query, builder) ->
                    builder.equal(root.get("service"), service));
        }
        if (level != null) {
            specification = specification.and((root, query, builder) ->
                    builder.equal(root.get("level"), level));
        }
        if (StringUtils.hasText(correlationId)) {
            specification = specification.and((root, query, builder) ->
                    builder.equal(root.get("correlationId"), correlationId));
        }
        return repository.findAll(specification, pageable).map(this::toResponse);
    }

    private OffsetDateTime readTimestamp(JsonNode payload) {
        JsonNode timestampNode = payload.path("occurredAt");
        if (timestampNode.isNumber()) {
            BigDecimal epochSeconds = timestampNode.decimalValue();
            long seconds = epochSeconds.longValue();
            long nanos = epochSeconds.subtract(BigDecimal.valueOf(seconds))
                    .movePointRight(9)
                    .longValue();
            return OffsetDateTime.ofInstant(Instant.ofEpochSecond(seconds, nanos), ZoneOffset.UTC);
        }

        String value = timestampNode.asText();
        return StringUtils.hasText(value) ? OffsetDateTime.parse(value) : OffsetDateTime.now();
    }

    private String readCorrelationId(JsonNode payload) {
        String value = payload.path("correlationId").asText();
        return StringUtils.hasText(value) ? value : UUID.randomUUID().toString();
    }

    private JsonNode safeMeta(JsonNode payload) {
        return objectMapper.createObjectNode()
                .put("eventId", payload.path("eventId").asText())
                .put("eventType", payload.path("eventType").asText());
    }

    private String serviceName(String routingKey) {
        if (routingKey.startsWith("appointment.")) return "appointment-service";
        if (routingKey.startsWith("call.")) return "call-service";
        return "whatsapp-service";
    }

    private LogEventResponse toResponse(LogEvent event) {
        LogEventResponse response = new LogEventResponse();
        response.setId(event.getId());
        response.setTimestamp(event.getTimestamp());
        response.setLevel(event.getLevel());
        response.setService(event.getService());
        response.setMessage(event.getMessage());
        response.setCorrelationId(event.getCorrelationId());
        if (StringUtils.hasText(event.getMetaJson())) {
            try {
                response.setMeta(objectMapper.readTree(event.getMetaJson()));
            } catch (JsonProcessingException exception) {
                response.setMeta(null);
            }
        }
        return response;
    }
}
