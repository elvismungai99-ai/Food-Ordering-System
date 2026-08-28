-- V12: Add audit_logs table for tracking admin actions, refunds, payment changes, account disabling, and role changes
CREATE TABLE IF NOT EXISTS audit_logs (
    id UUID PRIMARY KEY,
    action VARCHAR(100) NOT NULL,
    actor_id UUID,
    actor_email VARCHAR(255),
    actor_role VARCHAR(50),
    target_id VARCHAR(255),
    target_type VARCHAR(100),
    details TEXT,
    ip_address VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_audit_logs_action ON audit_logs(action);
CREATE INDEX IF NOT EXISTS idx_audit_logs_actor_id ON audit_logs(actor_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_created_at ON audit_logs(created_at DESC);

