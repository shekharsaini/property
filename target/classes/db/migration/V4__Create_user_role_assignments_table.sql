CREATE TABLE IF NOT EXISTS user_role_assignments (
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    role_id INT REFERENCES user_roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);
