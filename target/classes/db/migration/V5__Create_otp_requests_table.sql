CREATE TABLE IF NOT EXISTS otp_requests (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    otp_code VARCHAR(10) NOT NULL,
    otp_type VARCHAR(10) NOT NULL CHECK (otp_type IN ('email', 'phone')),
    is_used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    resend_attempts INT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_otp_requests_user_id ON otp_requests(user_id);
