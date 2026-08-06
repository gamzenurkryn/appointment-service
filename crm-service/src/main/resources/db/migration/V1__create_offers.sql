CREATE TABLE offers (
    id UUID PRIMARY KEY,
    appointment_id UUID NOT NULL UNIQUE,
    store_id UUID NOT NULL,
    source_service_type VARCHAR(100) NOT NULL,
    offered_service_type VARCHAR(100) NOT NULL,
    title VARCHAR(200) NOT NULL,
    discount_percent INTEGER NOT NULL,
    status VARCHAR(30) NOT NULL,
    correlation_id VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_offers_store_id ON offers(store_id);
CREATE INDEX idx_offers_status ON offers(status);
