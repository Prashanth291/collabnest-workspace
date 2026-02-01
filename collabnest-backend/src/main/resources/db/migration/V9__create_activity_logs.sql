-- Activity Log table to track all user actions
CREATE TABLE activity_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workspace_id UUID NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    activity_type VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID NOT NULL,
    entity_name VARCHAR(255),
    description TEXT,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for efficient querying
CREATE INDEX idx_activity_logs_workspace_id ON activity_logs(workspace_id);
CREATE INDEX idx_activity_logs_user_id ON activity_logs(user_id);
CREATE INDEX idx_activity_logs_created_at ON activity_logs(created_at DESC);
CREATE INDEX idx_activity_logs_entity ON activity_logs(entity_type, entity_id);
CREATE INDEX idx_activity_logs_workspace_created ON activity_logs(workspace_id, created_at DESC);

-- Comments
COMMENT ON TABLE activity_logs IS 'Tracks all user activities within workspaces';
COMMENT ON COLUMN activity_logs.activity_type IS 'Type of activity: CREATED, UPDATED, DELETED, MOVED, ASSIGNED, etc.';
COMMENT ON COLUMN activity_logs.entity_type IS 'Type of entity: TASK, DOCUMENT, COMMENT, BOARD, WORKSPACE';
COMMENT ON COLUMN activity_logs.entity_id IS 'ID of the affected entity';
COMMENT ON COLUMN activity_logs.metadata IS 'Additional context as JSON (e.g., old/new values)';
