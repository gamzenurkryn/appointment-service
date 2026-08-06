CREATE TABLE IF NOT EXISTS stores (
    id UUID PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    location VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    google_calendar_id VARCHAR(255),
    timezone VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS appointments (
    id UUID PRIMARY KEY,
    customer_name VARCHAR(150) NOT NULL,
    customer_phone VARCHAR(20) NOT NULL,
    store_id UUID NOT NULL,
    store_name VARCHAR(150),
    service_type VARCHAR(100) NOT NULL,
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(30) NOT NULL,
    channel VARCHAR(30) NOT NULL,
    calendar_event_id VARCHAR(255),
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

INSERT INTO stores (id, name, location, phone, google_calendar_id, timezone)
VALUES
(
    'b2c30000-0000-0000-0000-000000000001',
    'Bizim Beauty Saloon - Nişantaşı',
    'Nişantaşı, Şişli, İstanbul',
    '+902120000101',
    NULL,
    'Europe/Istanbul'
),
(
    'b2c30000-0000-0000-0000-000000000002',
    'Bizim Beauty Saloon - Kadıköy',
    'Caferağa, Kadıköy, İstanbul',
    '+902120000102',
    NULL,
    'Europe/Istanbul'
),
(
    'b2c30000-0000-0000-0000-000000000003',
    'Bizim Beauty Saloon - Bakırköy',
    'Zeytinlik, Bakırköy, İstanbul',
    '+902120000103',
    NULL,
    'Europe/Istanbul'
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO stores (id, name, location, phone, google_calendar_id, timezone)
SELECT DISTINCT
    appointment.store_id,
    COALESCE(NULLIF(appointment.store_name, ''), 'Eski Mağaza'),
    'Bilinmiyor',
    '+900000000000',
    NULL,
    'Europe/Istanbul'
FROM appointments appointment
WHERE appointment.store_id IS NOT NULL
ON CONFLICT (id) DO NOTHING;

UPDATE appointments
SET end_time = start_time + INTERVAL '30 minutes'
WHERE end_time IS NULL
  AND start_time IS NOT NULL;

UPDATE appointments
SET status = 'PENDING'
WHERE status IS NULL;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'appointments'
          AND column_name = 'channel_type'
    ) THEN
        UPDATE appointments
        SET channel = COALESCE(channel_type, 'WHATSAPP')
        WHERE channel IS NULL;
    ELSE
        UPDATE appointments
        SET channel = 'WHATSAPP'
        WHERE channel IS NULL;
    END IF;
END
$$;

CREATE INDEX IF NOT EXISTS idx_appointments_store_start_time
    ON appointments (store_id, start_time);

CREATE INDEX IF NOT EXISTS idx_appointments_customer_phone
    ON appointments (customer_phone);

CREATE INDEX IF NOT EXISTS idx_appointments_status
    ON appointments (status);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_appointments_store'
    ) THEN
        ALTER TABLE appointments
            ADD CONSTRAINT fk_appointments_store
            FOREIGN KEY (store_id) REFERENCES stores (id) NOT VALID;
    END IF;
END
$$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_appointments_time_range'
    ) THEN
        ALTER TABLE appointments
            ADD CONSTRAINT chk_appointments_time_range
            CHECK (end_time > start_time) NOT VALID;
    END IF;
END
$$;

CREATE OR REPLACE FUNCTION prevent_appointment_overlap()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.status <> 'CANCELLED' AND EXISTS (
        SELECT 1
        FROM appointments existing
        WHERE existing.store_id = NEW.store_id
          AND existing.id <> NEW.id
          AND existing.status <> 'CANCELLED'
          AND existing.start_time < NEW.end_time
          AND existing.end_time > NEW.start_time
    ) THEN
        RAISE EXCEPTION 'Appointment slot overlaps with an existing appointment'
            USING ERRCODE = '23P01';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_prevent_appointment_overlap ON appointments;

CREATE TRIGGER trg_prevent_appointment_overlap
BEFORE INSERT OR UPDATE OF store_id, start_time, end_time, status
ON appointments
FOR EACH ROW
EXECUTE FUNCTION prevent_appointment_overlap();
