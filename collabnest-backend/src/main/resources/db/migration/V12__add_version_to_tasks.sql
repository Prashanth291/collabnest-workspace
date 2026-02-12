-- Add version column for optimistic locking to tasks table
ALTER TABLE tasks
    ADD COLUMN version BIGINT DEFAULT 0;
