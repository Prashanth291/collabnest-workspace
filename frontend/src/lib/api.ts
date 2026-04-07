export const API_BASE = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

class ApiError extends Error {
  status: number;
  constructor(message: string, status: number) {
    super(message);
    this.status = status;
    this.name = "ApiError";
  }
}

function getToken(): string | null {
  if (typeof window === "undefined") return null;
  return localStorage.getItem("token");
}

async function request<T>(
  path: string,
  options: RequestInit = {}
): Promise<T> {
  const token = getToken();
  const headers: Record<string, string> = {
    ...(options.headers as Record<string, string>),
  };

  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  if (
    options.body &&
    typeof options.body === "string" &&
    !headers["Content-Type"]
  ) {
    headers["Content-Type"] = "application/json";
  }

  const res = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers,
  });

  if (!res.ok) {
    let message = `Request failed with status ${res.status}`;
    try {
      const err = await res.json();
      message = err.message || err.error || message;
    } catch {
      // ignore parse errors
    }
    throw new ApiError(message, res.status);
  }

  if (res.status === 204) return undefined as T;

  const text = await res.text();
  if (!text) return undefined as T;

  try {
    return JSON.parse(text);
  } catch {
    return text as T;
  }
}

function get<T>(path: string) {
  return request<T>(path, { method: "GET" });
}

function post<T>(path: string, body?: unknown) {
  return request<T>(path, {
    method: "POST",
    body: body ? JSON.stringify(body) : undefined,
  });
}

function postForm<T>(path: string, formData: FormData) {
  return request<T>(path, {
    method: "POST",
    body: formData,
  });
}

function put<T>(path: string, body?: unknown) {
  return request<T>(path, {
    method: "PUT",
    body: body ? JSON.stringify(body) : undefined,
  });
}

function del<T>(path: string) {
  return request<T>(path, { method: "DELETE" });
}

async function download(path: string): Promise<Blob> {
  const token = getToken();
  const headers: Record<string, string> = {};

  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  const res = await fetch(`${API_BASE}${path}`, {
    method: "GET",
    headers,
  });

  if (!res.ok) {
    let message = `Request failed with status ${res.status}`;
    try {
      const err = await res.json();
      message = err.message || err.error || message;
    } catch {
      // ignore parse errors
    }
    throw new ApiError(message, res.status);
  }

  return res.blob();
}

// ─── Auth ────────────────────────────────────────────────────
import type {
  RegisterRequest,
  LoginRequest,
  User,
  Workspace,
  WorkspaceMember,
  Board,
  BoardColumn,
  Task,
  Document,
  Comment,
  ChatResponse,
  ChatMessage,
  FileResponse,
  NotificationDto,
  NotificationStats,
  ActivityLogDto,
  PageResponse,
  CreateWorkspaceRequest,
  InviteMemberRequest,
  CreateBoardRequest,
  CreateColumnRequest,
  CreateTaskRequest,
  MoveTaskRequest,
  AssignTaskRequest,
  CreateDocumentRequest,
  UpdateDocumentRequest,
  SendMessageRequest,
  CreateCommentRequest,
  ChangeMemberRoleRequest,
} from "./types";

export const api = {
  // Auth
  auth: {
    register: (data: RegisterRequest) =>
      post<{ accessToken: string }>("/api/auth/register", data),
    login: (data: LoginRequest) =>
      post<{ accessToken: string }>("/api/auth/login", data),
    getGoogleUrl: () => `${API_BASE}/oauth2/authorization/google`,
    getGithubUrl: () => `${API_BASE}/oauth2/authorization/github`,
  },

  // User
  user: {
    getProfile: () => get<User>("/api/user/profile"),
    updateProfile: (data: { name?: string; username?: string; email?: string; password?: string }) =>
      put<User>("/api/user/profile", data),
  },

  // Workspaces
  workspaces: {
    list: () => get<Workspace[]>("/api/workspaces"),
    get: (id: string) => get<Workspace>(`/api/workspaces/${id}`),
    create: (data: CreateWorkspaceRequest) =>
      post<Workspace>("/api/workspaces", data),
    update: (id: string, data: CreateWorkspaceRequest) =>
      put<Workspace>(`/api/workspaces/${id}`, data),
    delete: (id: string) => del<void>(`/api/workspaces/${id}`),
    invite: (id: string, data: InviteMemberRequest) =>
      post<string>(`/api/workspaces/${id}/invite`, data),
    join: (inviteToken: string) =>
      post<void>("/api/workspaces/join", { inviteToken }),
    getMembers: (id: string) =>
      get<WorkspaceMember[]>(`/api/workspaces/${id}/members`),
    changeMemberRole: (
      workspaceId: string,
      userId: string,
      data: ChangeMemberRoleRequest
    ) =>
      put<WorkspaceMember>(
        `/api/workspaces/${workspaceId}/members/${userId}/role`,
        data
      ),
    removeMember: (workspaceId: string, userId: string) =>
      del<void>(`/api/workspaces/${workspaceId}/members/${userId}`),
  },

  // Boards
  boards: {
    list: (workspaceId: string) =>
      get<Board[]>(`/api/workspaces/${workspaceId}/boards`),
    get: (workspaceId: string, boardId: string) =>
      get<Board>(`/api/workspaces/${workspaceId}/boards/${boardId}`),
    create: (workspaceId: string, data: CreateBoardRequest) =>
      post<Board>(`/api/workspaces/${workspaceId}/boards`, data),
    update: (workspaceId: string, boardId: string, data: CreateBoardRequest) =>
      put<Board>(`/api/workspaces/${workspaceId}/boards/${boardId}`, data),
    delete: (workspaceId: string, boardId: string) =>
      del<void>(`/api/workspaces/${workspaceId}/boards/${boardId}`),
  },

  // Columns
  columns: {
    list: (workspaceId: string, boardId: string) =>
      get<BoardColumn[]>(
        `/api/workspaces/${workspaceId}/boards/${boardId}/columns`
      ),
    create: (
      workspaceId: string,
      boardId: string,
      data: CreateColumnRequest
    ) =>
      post<BoardColumn>(
        `/api/workspaces/${workspaceId}/boards/${boardId}/columns`,
        data
      ),
    update: (
      workspaceId: string,
      boardId: string,
      columnId: string,
      data: CreateColumnRequest
    ) =>
      put<BoardColumn>(
        `/api/workspaces/${workspaceId}/boards/${boardId}/columns/${columnId}`,
        data
      ),
    delete: (workspaceId: string, boardId: string, columnId: string) =>
      del<void>(
        `/api/workspaces/${workspaceId}/boards/${boardId}/columns/${columnId}`
      ),
  },

  // Tasks
  tasks: {
    list: (workspaceId: string, columnId: string) =>
      get<Task[]>(
        `/api/workspaces/${workspaceId}/columns/${columnId}/tasks`
      ),
    get: (workspaceId: string, columnId: string, taskId: string) =>
      get<Task>(
        `/api/workspaces/${workspaceId}/columns/${columnId}/tasks/${taskId}`
      ),
    create: (workspaceId: string, columnId: string, data: CreateTaskRequest) =>
      post<Task>(
        `/api/workspaces/${workspaceId}/columns/${columnId}/tasks`,
        data
      ),
    update: (
      workspaceId: string,
      columnId: string,
      taskId: string,
      data: CreateTaskRequest
    ) =>
      put<Task>(
        `/api/workspaces/${workspaceId}/columns/${columnId}/tasks/${taskId}`,
        data
      ),
    move: (
      workspaceId: string,
      columnId: string,
      taskId: string,
      data: MoveTaskRequest
    ) =>
      put<Task>(
        `/api/workspaces/${workspaceId}/columns/${columnId}/tasks/${taskId}/move`,
        data
      ),
    assign: (
      workspaceId: string,
      columnId: string,
      taskId: string,
      data: AssignTaskRequest
    ) =>
      put<Task>(
        `/api/workspaces/${workspaceId}/columns/${columnId}/tasks/${taskId}/assign`,
        data
      ),
    delete: (workspaceId: string, columnId: string, taskId: string) =>
      del<void>(
        `/api/workspaces/${workspaceId}/columns/${columnId}/tasks/${taskId}`
      ),
  },

  // Documents
  documents: {
    list: (workspaceId: string) =>
      get<Document[]>(`/api/workspaces/${workspaceId}/documents`),
    get: (workspaceId: string, documentId: string) =>
      get<Document>(`/api/workspaces/${workspaceId}/documents/${documentId}`),
    create: (workspaceId: string, data: CreateDocumentRequest) =>
      post<Document>(`/api/workspaces/${workspaceId}/documents`, data),
    update: (
      workspaceId: string,
      documentId: string,
      data: UpdateDocumentRequest
    ) =>
      put<Document>(
        `/api/workspaces/${workspaceId}/documents/${documentId}`,
        data
      ),
    delete: (workspaceId: string, documentId: string) =>
      del<void>(`/api/workspaces/${workspaceId}/documents/${documentId}`),
    addComment: (
      workspaceId: string,
      documentId: string,
      data: CreateCommentRequest
    ) =>
      post<Comment>(
        `/api/workspaces/${workspaceId}/documents/${documentId}/comments`,
        data
      ),
    getComments: (workspaceId: string, documentId: string) =>
      get<Comment[]>(
        `/api/workspaces/${workspaceId}/documents/${documentId}/comments`
      ),
    deleteComment: (
      workspaceId: string,
      documentId: string,
      commentId: string
    ) =>
      del<void>(
        `/api/workspaces/${workspaceId}/documents/${documentId}/comments/${commentId}`
      ),
  },

  // Chat
  chat: {
    getWorkspaceChat: (workspaceId: string) =>
      post<ChatResponse>(`/api/chat/workspace/${workspaceId}`),
    getDirectChat: (otherUserId: string) =>
      post<ChatResponse>(`/api/chat/direct/${otherUserId}`),
    listDirectChats: () => get<ChatResponse[]>("/api/chat/direct"),
    sendMessage: (chatId: string, data: SendMessageRequest) =>
      post<ChatMessage>(`/api/chat/${chatId}/messages`, data),
    getMessages: (chatId: string, page = 0, size = 50) =>
      get<PageResponse<ChatMessage>>(
        `/api/chat/${chatId}/messages?page=${page}&size=${size}`
      ),
  },

  // Files
  files: {
    list: (workspaceId: string) =>
      get<FileResponse[]>(`/api/workspaces/${workspaceId}/files`),
    get: (workspaceId: string, fileId: string) =>
      get<FileResponse>(`/api/workspaces/${workspaceId}/files/${fileId}`),
    upload: (
      workspaceId: string,
      params: { fileName: string; fileUrl: string; size: number; taskId?: string; documentId?: string }
    ) => {
      const query = new URLSearchParams({
        fileName: params.fileName,
        fileUrl: params.fileUrl,
        size: params.size.toString(),
      });
      if (params.taskId) query.set("taskId", params.taskId);
      if (params.documentId) query.set("documentId", params.documentId);
      return post<FileResponse>(
        `/api/workspaces/${workspaceId}/files?${query.toString()}`
      );
    },
    uploadBinary: (
      workspaceId: string,
      file: File,
      params?: { taskId?: string; documentId?: string }
    ) => {
      const formData = new FormData();
      formData.append("file", file);
      if (params?.taskId) formData.append("taskId", params.taskId);
      if (params?.documentId) formData.append("documentId", params.documentId);
      return postForm<FileResponse>(
        `/api/workspaces/${workspaceId}/files/upload`,
        formData
      );
    },
    delete: (workspaceId: string, fileId: string) =>
      del<void>(`/api/workspaces/${workspaceId}/files/${fileId}`),
    download: async (workspaceId: string, fileId: string, fileName?: string) => {
      const blob = await download(
        `/api/workspaces/${workspaceId}/files/${fileId}/download`
      );
      const objectUrl = window.URL.createObjectURL(blob);
      const anchor = document.createElement("a");
      anchor.href = objectUrl;
      anchor.download = fileName || fileId;
      document.body.appendChild(anchor);
      anchor.click();
      anchor.remove();
      window.URL.revokeObjectURL(objectUrl);
    },
  },

  // Notifications
  notifications: {
    list: (page = 0, size = 20, unreadOnly = false) =>
      get<PageResponse<NotificationDto>>(
        `/api/notifications?page=${page}&size=${size}&unreadOnly=${unreadOnly}`
      ),
    getStats: () => get<NotificationStats>("/api/notifications/stats"),
    markRead: (id: string) =>
      put<void>(`/api/notifications/${id}/read`),
    markAllRead: () => put<void>("/api/notifications/read-all"),
    delete: (id: string) => del<void>(`/api/notifications/${id}`),
  },

  // Activity
  activity: {
    workspace: (workspaceId: string, page = 0, size = 20) =>
      get<PageResponse<ActivityLogDto>>(
        `/api/activity/workspace/${workspaceId}?page=${page}&size=${size}`
      ),
    userInWorkspace: (
      workspaceId: string,
      userId: string,
      page = 0,
      size = 20
    ) =>
      get<PageResponse<ActivityLogDto>>(
        `/api/activity/workspace/${workspaceId}/user/${userId}?page=${page}&size=${size}`
      ),
    me: (workspaceId?: string, page = 0, size = 20) => {
      const query = new URLSearchParams({
        page: page.toString(),
        size: size.toString(),
      });
      if (workspaceId) query.set("workspaceId", workspaceId);
      return get<PageResponse<ActivityLogDto>>(
        `/api/activity/me?${query.toString()}`
      );
    },
  },
};

export { ApiError };
export default api;
