-- Create workspaces table
CREATE TABLE workspaces (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    owner_id UUID NOT NULL,
    invite_token VARCHAR(255) UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_workspace_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create index on owner_id for faster queries
CREATE INDEX idx_workspaces_owner_id ON workspaces(owner_id);

-- Create index on invite_token for invite link lookups
CREATE INDEX idx_workspaces_invite_token ON workspaces(invite_token);
