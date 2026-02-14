-- Add updated_at to workspaces, joined_at to workspace_members
ALTER TABLE workspaces ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

ALTER TABLE workspace_members ADD COLUMN IF NOT EXISTS joined_at TIMESTAMP DEFAULT now();
