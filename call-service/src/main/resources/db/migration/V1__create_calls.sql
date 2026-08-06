CREATE TABLE calls (
    id UUID PRIMARY KEY,
    appointment_id UUID NOT NULL,
    room_name VARCHAR(100) NOT NULL UNIQUE,
    customer_phone VARCHAR(20) NOT NULL,
    store_id UUID NOT NULL,
    status VARCHAR(30) NOT NULL,
    result VARCHAR(30),
    participant_count INTEGER NOT NULL DEFAULT 0 CHECK (participant_count >= 0),
    transcript_url VARCHAR(1000),
    started_at TIMESTAMPTZ,
    ended_at TIMESTAMPTZ
);

CREATE INDEX idx_calls_status ON calls(status);
CREATE INDEX idx_calls_store_id ON calls(store_id);
CREATE INDEX idx_calls_appointment_id ON calls(appointment_id);
