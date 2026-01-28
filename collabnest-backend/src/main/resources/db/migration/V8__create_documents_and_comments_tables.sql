-- Create documents table
CREATE TABLE documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workspace_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    created_by_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_document_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE,
    CONSTRAINT fk_document_created_by FOREIGN KEY (created_by_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for efficient lookups
CREATE INDEX idx_documents_workspace_id ON documents(workspace_id);
CREATE INDEX idx_documents_created_by_id ON documents(created_by_id);

-- Create comments table (polymorphic - can comment on documents or tasks)
CREATE TABLE comments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_id UUID NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    created_by_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comment_created_by FOREIGN KEY (created_by_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for efficient polymorphic lookups
CREATE INDEX idx_comments_entity_id ON comments(entity_id);
CREATE INDEX idx_comments_entity_type ON comments(entity_type);
CREATE INDEX idx_comments_entity_id_type ON comments(entity_id, entity_type);
CREATE INDEX idx_comments_created_by_id ON comments(created_by_id);
