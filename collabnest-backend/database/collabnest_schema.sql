SELECT version();


CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
SELECT uuid_generate_v4();

CREATE USER collabnest_user WITH PASSWORD 'Nani@2910';

GRANT CONNECT ON DATABASE collabnest_db TO collabnest_user;

GRANT USAGE, CREATE ON SCHEMA public TO collabnest_user;
ALTER DATABASE collabnest_db OWNER TO collabnest_user;

/* =========================================================
   ENUM TYPES
   ========================================================= */

CREATE TYPE workspace_role AS ENUM ('OWNER', 'ADMIN', 'MEMBER', 'VIEWER');
CREATE TYPE task_priority AS ENUM ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL');
CREATE TYPE chat_type AS ENUM ('WORKSPACE', 'DIRECT');
CREATE TYPE linked_entity_type AS ENUM ('TASK', 'DOCUMENT', 'NONE');


/* =========================================================
   USERS
   ========================================================= */

CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    name VARCHAR(150) NOT NULL,
    created_at TIMESTAMP DEFAULT now()
);


/* =========================================================
   WORKSPACES & MEMBERSHIP
   ========================================================= */

CREATE TABLE workspaces (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    owner_id UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMP DEFAULT now(),
    updated_at TIMESTAMP DEFAULT now()
);

CREATE TABLE workspace_members (
    id UUID PRIMARY KEY,
    workspace_id UUID NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role workspace_role NOT NULL,
    is_primary_owner BOOLEAN DEFAULT FALSE,
    joined_at TIMESTAMP DEFAULT now(),
    UNIQUE (workspace_id, user_id)
);


/* =========================================================
   BOARDS & COLUMNS
   ========================================================= */

CREATE TABLE boards (
    id UUID PRIMARY KEY,
    workspace_id UUID NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    position INT NOT NULL,
    created_at TIMESTAMP DEFAULT now()
);

CREATE TABLE columns (
    id UUID PRIMARY KEY,
    board_id UUID NOT NULL REFERENCES boards(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    position INT NOT NULL
);


/* =========================================================
   TASKS & EXECUTION ENGINE
   ========================================================= */

CREATE TABLE tasks (
    id UUID PRIMARY KEY,
    column_id UUID NOT NULL REFERENCES columns(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    priority task_priority DEFAULT 'MEDIUM',
    due_date DATE,
    position INT NOT NULL DEFAULT 0,
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMP DEFAULT now(),
    updated_at TIMESTAMP DEFAULT now()
);

CREATE TABLE task_assignees (
    task_id UUID REFERENCES tasks(id) ON DELETE CASCADE,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (task_id, user_id)
);

CREATE TABLE task_dependencies (
    task_id UUID REFERENCES tasks(id) ON DELETE CASCADE,
    depends_on_task_id UUID REFERENCES tasks(id) ON DELETE CASCADE,
    PRIMARY KEY (task_id, depends_on_task_id),
    CHECK (task_id <> depends_on_task_id)
);

CREATE TABLE task_comments (
    id UUID PRIMARY KEY,
    task_id UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    author_id UUID REFERENCES users(id),
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT now()
);


/* =========================================================
   DOCUMENTATION
   ========================================================= */

CREATE TABLE documents (
    id UUID PRIMARY KEY,
    workspace_id UUID NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMP DEFAULT now(),
    updated_at TIMESTAMP DEFAULT now()
);

CREATE TABLE document_comments (
    id UUID PRIMARY KEY,
    document_id UUID NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    author_id UUID REFERENCES users(id),
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT now()
);

CREATE TABLE task_document_links (
    task_id UUID REFERENCES tasks(id) ON DELETE CASCADE,
    document_id UUID REFERENCES documents(id) ON DELETE CASCADE,
    PRIMARY KEY (task_id, document_id)
);


/* =========================================================
   CHAT SYSTEM (SUPER-TYPE DESIGN)
   ========================================================= */

CREATE TABLE chats (
    id UUID PRIMARY KEY,
    chat_type chat_type NOT NULL,
    created_at TIMESTAMP DEFAULT now()
);

CREATE TABLE workspace_chats (
    chat_id UUID PRIMARY KEY REFERENCES chats(id) ON DELETE CASCADE,
    workspace_id UUID UNIQUE NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE
);

CREATE TABLE direct_chats (
    chat_id UUID PRIMARY KEY REFERENCES chats(id) ON DELETE CASCADE,
    user_one_id UUID NOT NULL REFERENCES users(id),
    user_two_id UUID NOT NULL REFERENCES users(id),
    CHECK (user_one_id < user_two_id),
    UNIQUE (user_one_id, user_two_id)
);

CREATE TABLE chat_messages (
    id UUID PRIMARY KEY,
    chat_id UUID NOT NULL REFERENCES chats(id) ON DELETE CASCADE,
    sender_id UUID NOT NULL REFERENCES users(id),
    content TEXT NOT NULL,
    linked_entity_type linked_entity_type DEFAULT 'NONE',
    linked_entity_id UUID,
    created_at TIMESTAMP DEFAULT now()
);


/* =========================================================
   ACTIVITY LOGS (IMMUTABLE)
   ========================================================= */

CREATE TABLE activity_logs (
    id UUID PRIMARY KEY,
    workspace_id UUID REFERENCES workspaces(id) ON DELETE CASCADE,
    actor_id UUID REFERENCES users(id),
    entity_type VARCHAR(50),
    entity_id UUID,
    action VARCHAR(255),
    created_at TIMESTAMP DEFAULT now()
);


/* =========================================================
   NOTIFICATIONS
   ========================================================= */

CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type VARCHAR(50),
    message TEXT,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT now()
);


/* =========================================================
   FILE STORAGE
   ========================================================= */

CREATE TABLE files (
    id UUID PRIMARY KEY,
    workspace_id UUID REFERENCES workspaces(id) ON DELETE CASCADE,
    uploaded_by UUID REFERENCES users(id),
    task_id UUID REFERENCES tasks(id) ON DELETE CASCADE,
    document_id UUID REFERENCES documents(id) ON DELETE CASCADE,
    file_name VARCHAR(255),
    file_url TEXT,
    size BIGINT,
    created_at TIMESTAMP DEFAULT now()
);


/* =========================================================
   INDEXES (PERFORMANCE)
   ========================================================= */

CREATE INDEX idx_workspace_members_workspace
    ON workspace_members(workspace_id);

CREATE INDEX idx_boards_workspace
    ON boards(workspace_id);

CREATE INDEX idx_tasks_column_position
    ON tasks(column_id, position);

CREATE INDEX idx_chat_messages_chat
    ON chat_messages(chat_id, created_at);

CREATE INDEX idx_activity_logs_workspace
    ON activity_logs(workspace_id, created_at);

CREATE INDEX idx_notifications_user
    ON notifications(user_id, is_read);
