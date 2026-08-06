CREATE SCHEMA IF NOT EXISTS notification_service;

CREATE TABLE notification_service.log_events (
    id UUID PRIMARY KEY,
    timestamp TIMESTAMPTZ NOT NULL,
    level VARCHAR(10) NOT NULL,
    service VARCHAR(100) NOT NULL,
    message VARCHAR(500) NOT NULL,
    correlation_id VARCHAR(100) NOT NULL,
    meta_json TEXT
);

CREATE INDEX idx_log_events_timestamp
    ON notification_service.log_events (timestamp DESC);

CREATE INDEX idx_log_events_filters
    ON notification_service.log_events (service, level, correlation_id);
