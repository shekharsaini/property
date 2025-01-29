CREATE TABLE document (
    id SERIAL PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL UNIQUE,
    file_path VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    uploaded_by BIGINT NOT NULL,
    property_id BIGINT NOT NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    file_content BYTEA NOT NULL,
    CONSTRAINT fk_uploaded_by FOREIGN KEY (uploaded_by) REFERENCES users(id),
    CONSTRAINT fk_property_id FOREIGN KEY (property_id) REFERENCES property(id)
);