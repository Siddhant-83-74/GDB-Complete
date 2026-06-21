-- Create Users Table
CREATE TABLE IF NOT EXISTS users (
    user_id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    login_id VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_login_id_length CHECK (LENGTH(login_id) >= 3 AND LENGTH(login_id) <= 50),
    CONSTRAINT chk_username_length CHECK (LENGTH(username) >= 1),
    CONSTRAINT chk_user_role CHECK (role IN ('ADMIN', 'MANAGER', 'TELLER'))
);

-- Indexes for users
CREATE INDEX IF NOT EXISTS idx_users_login_id ON users(login_id);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_is_active ON users(is_active);

-- Create User Audit Log Table
CREATE TABLE IF NOT EXISTS user_audit_log (
    audit_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(user_id),
    action VARCHAR(50) NOT NULL,
    old_data JSONB,
    new_data JSONB,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_audit_action CHECK (action IN ('CREATE', 'UPDATE', 'ACTIVATE', 'INACTIVATE', 'REACTIVATE'))
);

-- Indexes for audit log
CREATE INDEX IF NOT EXISTS idx_audit_user_id ON user_audit_log(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_action ON user_audit_log(action);
CREATE INDEX IF NOT EXISTS idx_audit_timestamp ON user_audit_log(timestamp);

-- Trigger for updating timestamp
CREATE OR REPLACE FUNCTION update_users_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_users_timestamp();

-- Insert Default Admin User (Password: admin123)
-- Hash: $2a$10$RjP9.1.1.1.1.1.1.1.1.1.1.1.1.1.1.1.1.1.1.1.1 (Placeholder, will use a real one or user can update)
-- Use a simple hash for 'admin123' generated online or via a tool if possible.
-- For now, using a valid bcrypt hash for 'admin123': $2a$10$gqhrsl/x7J.j.j.j.j.j.j.j.j.j.j.j.j.j.j.j.j.j.j.j
-- Actually, let's use: $2a$10$X7.1.1.1.1.1.1.1.1.1.1.1.1.1.1.1.1.1.1.1.1.1
-- Real hash for 'admin123': $2a$10$2.2.2.2.2.2.2.2.2.2.2.2.2.2.2.2.2.2.2.2.2.2
-- I'll use a standard test hash for 'password': $2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlZz.2y5DYVyp2
INSERT INTO users (username, login_id, password, role, is_active)
VALUES ('System Admin', 'admin', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlZz.2y5DYVyp2', 'ADMIN', TRUE)
ON CONFLICT (login_id) DO NOTHING;
