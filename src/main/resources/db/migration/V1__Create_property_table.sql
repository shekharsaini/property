CREATE TABLE address (
    id SERIAL PRIMARY KEY,
    secondary_address VARCHAR(255),
    street VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    country VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- Timestamp for when the record was created
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP  -- Timestamp for when the record was last updated
);

CREATE TABLE property (
    id SERIAL PRIMARY KEY,
    property_name VARCHAR(255) NOT NULL,
    type VARCHAR(100),
    size DOUBLE PRECISION,
    price DOUBLE PRECISION,
    amenities VARCHAR(255),
    address_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- Timestamp for when the record was created
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- Timestamp for when the record was last updated
    created_by BIGINT NOT NULL,
    updated_by BIGINT NOT NULL,
    tenant_id BIGINT,
    agent_id BIGINT,
    status VARCHAR(255),
    property_availability VARCHAR(50) DEFAULT 'OPEN',
    CONSTRAINT fk_address FOREIGN KEY (address_id) REFERENCES address(id)
);

