CREATE TABLE insurance (
    id BIGSERIAL PRIMARY KEY,
    insurance_provider_name VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    insurance_document BYTEA NOT NULL,
    insurance_document_name VARCHAR(255) NOT NULL UNIQUE,
    property_id BIGINT NOT NULL,
    updated_by BIGINT NOT NULL,
    FOREIGN KEY (property_id) REFERENCES property(id),
    FOREIGN KEY (updated_by) REFERENCES users(id)
);
