CREATE TABLE employees (
                           id UUID PRIMARY KEY,
                           store_id UUID NOT NULL,
                           name VARCHAR(150) NOT NULL,
                           active BOOLEAN NOT NULL DEFAULT TRUE,

                           CONSTRAINT fk_employees_store
                               FOREIGN KEY (store_id)
                                   REFERENCES stores (id)
);

CREATE INDEX idx_employees_store_id
    ON employees (store_id);

CREATE TABLE employee_services (
                                   employee_id UUID NOT NULL,
                                   service_type VARCHAR(100) NOT NULL,

                                   CONSTRAINT fk_employee_services_employee
                                       FOREIGN KEY (employee_id)
                                           REFERENCES employees (id)
                                           ON DELETE CASCADE,

                                   CONSTRAINT pk_employee_services
                                       PRIMARY KEY (employee_id, service_type)
);

CREATE INDEX idx_employee_services_service_type
    ON employee_services (service_type);