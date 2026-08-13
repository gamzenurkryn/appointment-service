package com.gamzenur.notificationservice.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.gamzenur.notificationservice.domain.LogLevel;

import java.time.OffsetDateTime;
import java.util.UUID;

public class LogEventResponse {

    private UUID id;
    private OffsetDateTime timestamp;
    private LogLevel level;
    private String service;
    private String message;
    private String correlationId;
    private JsonNode meta;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public OffsetDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(OffsetDateTime timestamp) { this.timestamp = timestamp; }
    public LogLevel getLevel() { return level; }
    public void setLevel(LogLevel level) { this.level = level; }
    public String getService() { return service; }
    public void setService(String service) { this.service = service; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public JsonNode getMeta() { return meta; }
    public void setMeta(JsonNode meta) { this.meta = meta; }
}
