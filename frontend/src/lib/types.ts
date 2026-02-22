// ─── Enums ───────────────────────────────────────────────────
export type AuthProvider = "LOCAL" | "GOOGLE" | "GITHUB" | "LINKEDIN";
export type UserRole = "USER" | "ADMIN";
export type WorkspaceRole = "OWNER" | "ADMIN" | "MEMBER" | "VIEWER";
export type TaskPriority = "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";
export type ChatType = "WORKSPACE" | "DIRECT";
export type LinkedEntityType = "TASK" | "DOCUMENT" | "NONE";
export type NotificationType =
  | "MENTION"
  | "TASK_ASSIGNED"
  | "TASK_UPDATED"
  | "TASK_COMMENT"
  | "DOCUMENT_SHARED"
  | "DOCUMENT_COMMENT"
  | "WORKSPACE_INVITE"
  | "MEMBER_JOINED"
  | "COMMENT_REPLY"
  | "SYSTEM";
export type ActivityType =
  | "WORKSPACE_CREATED"
  | "WORKSPACE_UPDATED"
  | "WORKSPACE_DELETED"
  | "MEMBER_ADDED"
  | "MEMBER_REMOVED"
  | "BOARD_CREATED"
  | "BOARD_UPDATED"
  | "BOARD_DELETED"
  | "COLUMN_CREATED"
  | "COLUMN_UPDATED"
  | "COLUMN_DELETED"
  | "TASK_CREATED"
  | "TASK_UPDATED"
  | "TASK_MOVED"
  | "TASK_ASSIGNED"
  | "TASK_DELETED"
  | "DOCUMENT_CREATED"
  | "DOCUMENT_UPDATED"
  | "DOCUMENT_DELETED"
  | "COMMENT_ADDED"
  | "COMMENT_UPDATED"
  | "COMMENT_DELETED";

// ─── Response Types ──────────────────────────────────────────
export interface User {
  id: string;
  email: string;
  username: string;
  name: string;
  authProvider: AuthProvider;
  role: UserRole;
  enabled: boolean;
  createdAt: string;
}

export interface Workspace {
  id: string;
  name: string;
  description?: string;
  ownerId: string;
  myRole: WorkspaceRole;
  inviteToken?: string;
  createdAt: string;
  updatedAt: string;
}

export interface WorkspaceMember {
  id: string;
  userId: string;
  username: string;
  name: string;
  email: string;
  role: WorkspaceRole;
  isPrimaryOwner: boolean;
  joinedAt: string;
}

export interface Board {
  id: string;
  workspaceId: string;
  name: string;
  position: number;
  createdAt: string;
}

export interface BoardColumn {
  id: string;
  boardId: string;
  name: string;
  position: number;
}

export interface Task {
  id: string;
  columnId: string;
  title: string;
  description: string;
  priority: TaskPriority;
  dueDate: string | null;
  position: number;
  assigneeId: string | null;
  assigneeName: string | null;
  createdById: string;
  createdAt: string;
  updatedAt: string;
}

export interface Document {
  id: string;
  workspaceId: string;
  title: string;
  content: string;
  createdById: string;
  createdAt: string;
  updatedAt: string;
}

export interface Comment {
  id: string;
  entityId: string;
  entityType: string;
  content: string;
  createdById: string;
  createdByName: string;
  createdAt: string;
}

export interface ChatResponse {
  id: string;
  chatType: ChatType;
  workspaceId?: string;
  otherUserId?: string;
  otherUsername?: string;
  createdAt: string;
}

export interface ChatMessage {
  id: string;
  chatId: string;
  senderId: string;
  senderUsername: string;
  content: string;
  linkedEntityType: LinkedEntityType;
  linkedEntityId?: string;
  createdAt: string;
}

export interface FileResponse {
  id: string;
  workspaceId: string;
  uploadedById: string;
  uploadedByUsername: string;
  taskId?: string;
  documentId?: string;
  fileName: string;
  fileUrl: string;
  size: number;
  createdAt: string;
}

export interface NotificationDto {
  id: string;
  userId: string;
  workspaceId?: string;
  notificationType: NotificationType;
  title: string;
  message: string;
  entityType?: string;
  entityId?: string;
  actorId?: string;
  actorName?: string;
  isRead: boolean;
  createdAt: string;
  readAt?: string;
}

export interface NotificationStats {
  totalCount: number;
  unreadCount: number;
}

export interface ActivityLogDto {
  id: string;
  workspaceId: string;
  userId: string;
  userName: string;
  activityType: ActivityType;
  entityType: string;
  entityId: string;
  entityName: string;
  description: string;
  metadata: Record<string, unknown>;
  createdAt: string;
}

export interface PageResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

// ─── Request Types ───────────────────────────────────────────
export interface RegisterRequest {
  email: string;
  username: string;
  name: string;
  password: string;
}

export interface LoginRequest {
  identifier: string;
  password: string;
}

export interface CreateWorkspaceRequest {
  name: string;
  description?: string;
}

export interface InviteMemberRequest {
  email: string;
  role: WorkspaceRole;
}

export interface CreateBoardRequest {
  name: string;
  position: number;
}

export interface CreateColumnRequest {
  name: string;
  position: number;
}

export interface CreateTaskRequest {
  title: string;
  description?: string;
  priority?: TaskPriority;
  dueDate?: string;
}

export interface MoveTaskRequest {
  targetColumnId: string;
  position: number;
}

export interface AssignTaskRequest {
  userId: string | null;
}

export interface CreateDocumentRequest {
  title: string;
  content: string;
}

export interface UpdateDocumentRequest {
  title: string;
  content: string;
}

export interface SendMessageRequest {
  content: string;
  linkedEntityType?: LinkedEntityType;
  linkedEntityId?: string;
}

export interface CreateCommentRequest {
  content: string;
}

export interface ChangeMemberRoleRequest {
  role: WorkspaceRole;
}
