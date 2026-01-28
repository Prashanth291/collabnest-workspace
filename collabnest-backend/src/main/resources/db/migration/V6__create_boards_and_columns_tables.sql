-- Create boards table
CREATE TABLE boards (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workspace_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    position INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_board_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE
);

-- Create index on workspace_id for faster queries
CREATE INDEX idx_boards_workspace_id ON boards(workspace_id);

-- Create board_columns table
CREATE TABLE board_columns (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    board_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    position INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT fk_column_board FOREIGN KEY (board_id) REFERENCES boards(id) ON DELETE CASCADE
);

-- Create index on board_id for faster queries
CREATE INDEX idx_board_columns_board_id ON board_columns(board_id);
