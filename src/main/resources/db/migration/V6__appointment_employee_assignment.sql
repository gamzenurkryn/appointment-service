ALTER TABLE appointments
    ADD COLUMN employee_id UUID;

ALTER TABLE appointments
    ADD COLUMN employee_name VARCHAR(150);

ALTER TABLE appointments
    ADD CONSTRAINT fk_appointments_employee
        FOREIGN KEY (employee_id)
            REFERENCES employees (id);

CREATE INDEX idx_appointments_employee_time
    ON appointments (employee_id, start_time, end_time);

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
          AND (
              NEW.employee_id IS NULL
              OR existing.employee_id IS NULL
              OR existing.employee_id = NEW.employee_id
          )
    ) THEN
        RAISE EXCEPTION 'Employee appointment overlaps with an existing appointment'
            USING ERRCODE = '23P01';
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_prevent_appointment_overlap
    ON appointments;

CREATE TRIGGER trg_prevent_appointment_overlap
    BEFORE INSERT OR UPDATE OF
    store_id,
                         employee_id,
                         start_time,
                         end_time,
                         status
                     ON appointments
                         FOR EACH ROW
                         EXECUTE FUNCTION prevent_appointment_overlap();