CREATE TABLE vehicle_info (
    id BIGSERIAL PRIMARY KEY,
    make VARCHAR(255),
    model VARCHAR(255),
    color VARCHAR(255),
    licence_number VARCHAR(255),
    model_year DATE
);

ALTER TABLE users
ADD CONSTRAINT fk_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicle_info (id);