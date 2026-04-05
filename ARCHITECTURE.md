# CollabNest — Complete Architecture & Request Flow Documentation

> **What is CollabNest?**
> A real-time collaborative workspace platform where teams can create workspaces, manage tasks on Kanban boards, collaborate on documents, chat in real-time, share files, and track activity — all secured with JWT + OAuth2 authentication and role-based workspace permissions.

---

## Table of Contents

1. [High-Level Architecture](#1-high-level-architecture)
2. [Tech Stack](#2-tech-stack)
3. [Project Structure](#3-project-structure)
4. [Database Schema & Entity Relationships](#4-database-schema--entity-relationships)
5. [Authentication & Security Deep Dive](#5-authentication--security-deep-dive)
6. [Request Lifecycle — How Every HTTP Request Flows](#6-request-lifecycle--how-every-http-request-flows)
7. [Feature-by-Feature Walkthroughs](#7-feature-by-feature-walkthroughs)
   - 7.1 [User Registration & Login](#71-user-registration--login)
   - 7.2 [OAuth2 Login (Google/GitHub)](#72-oauth2-login-googlegithub)
   - 7.3 [Workspaces & Membership](#73-workspaces--membership)
   - 7.4 [Boards, Columns & Tasks (Kanban)](#74-boards-columns--tasks-kanban)
   - 7.5 [Documents & Comments](#75-documents--comments)
   - 7.6 [Chat System](#76-chat-system)
   - 7.7 [File Management](#77-file-management)
   - 7.8 [Notifications](#78-notifications)
   - 7.9 [Activity Logs](#79-activity-logs)
   - 7.10 [Admin Panel](#710-admin-panel)
8. [WebSocket (Real-Time) Architecture](#8-websocket-real-time-architecture)
9. [Frontend Architecture](#9-frontend-architecture)
10. [How the Frontend Talks to the Backend](#10-how-the-frontend-talks-to-the-backend)
11. [Error Handling](#11-error-handling)
12. [Database Migrations (Flyway)](#12-database-migrations-flyway)
13. [Configuration & Environment](#13-configuration--environment)
14. [Complete API Reference](#14-complete-api-reference)

---

## 1. High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         BROWSER                                  │
│  Next.js 16 (React 19) — port 3000                              │
│  ┌─────────────┐  ┌──────────────┐  ┌────────────────────────┐  │
│  │ Zustand      │  │ React Query  │  │ STOMP/SockJS WebSocket │  │
│  │ (Auth State) │  │ (API Cache)  │  │ (Real-time events)     │  │
│  └──────┬───────┘  └──────┬───────┘  └──────────┬─────────────┘  │
│         │                 │                      │                │
│         └────────┬────────┘                      │                │
│                  ▼                                ▼                │
│         api.ts (fetch wrapper)          ws://localhost:8080/ws    │
│         ────────┬──────────             ─────────┬───────────    │
└─────────────────┼────────────────────────────────┼────────────────┘
                  │ HTTP (REST)                     │ WebSocket
                  ▼                                 ▼
┌─────────────────────────────────────────────────────────────────┐
│               SPRING BOOT 4 BACKEND — port 8080                  │
│                                                                   │
│  ┌───────────────────────────────────────────────────────────┐   │
│  │                   SECURITY FILTER CHAIN                    │   │
│  │  ┌──────────────┐  ┌────────────────┐  ┌──────────────┐  │   │
│  │  │ CORS Filter  │→ │ JWT Auth Filter │→ │ OAuth2 Login │  │   │
│  │  └──────────────┘  └────────────────┘  └──────────────┘  │   │
│  └───────────────────────────┬───────────────────────────────┘   │
│                              ▼                                    │
│  ┌───────────────────────────────────────────────────────────┐   │
│  │              @PreAuthorize PERMISSION CHECK                 │   │
│  │  isAuthenticated() / hasRole('ADMIN') /                    │   │
│  │  hasPermission(#workspaceId, 'Workspace', 'MEMBER')        │   │
│  └───────────────────────────┬───────────────────────────────┘   │
│                              ▼                                    │
│  ┌──────────┐  ┌──────────────┐  ┌──────────────────────────┐   │
│  │Controller │→ │   Service    │→ │   Repository (JPA)       │   │
│  │ (REST)   │  │  (Business)  │  │   ↕ PostgreSQL            │   │
│  └──────────┘  └──────┬───────┘  └──────────────────────────┘   │
│                       │                                           │
│                       ├→ SimpMessagingTemplate (WebSocket push)   │
│                       ├→ ActivityLogService (async audit trail)   │
│                       └→ NotificationService (async notifs)       │
│                                                                   │
│  ┌───────────────────────────────────────────────────────────┐   │
│  │ WebSocket: STOMP + SockJS                                  │   │
│  │ Topics: /topic/board/{id}  /topic/workspace/{id}           │   │
│  │         /topic/chat/{id}   /topic/presence                 │   │
│  │         /topic/user/{id}/notifications                     │   │
│  └───────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                     POSTGRESQL DATABASE                          │
│  Tables: users, workspaces, workspace_members, boards,           │
│          board_columns, tasks, documents, comments, chats,       │
│          workspace_chats, direct_chats, chat_messages,           │
│          activity_logs, notifications, files,                     │
│          task_dependencies, task_document_links                   │
│  Migrations: Flyway V1 → V17                                    │
└─────────────────────────────────────────────────────────────────┘
```

---

## 2. Tech Stack

| Layer      | Technology                         | Purpose                                      |
|------------|------------------------------------|----------------------------------------------|
| Frontend   | Next.js 16, React 19, TypeScript   | Server-rendered + client SPA                 |
| Styling    | Tailwind CSS 4, Lucide Icons       | Utility-first CSS + icon library             |
| State      | Zustand                            | Lightweight global state (auth, workspace)   |
| Data       | TanStack React Query               | Server state cache + refetch                 |
| DnD        | @hello-pangea/dnd                  | Drag-and-drop for Kanban boards              |
| Animation  | Framer Motion                      | Page transitions & micro-animations          |
| WebSocket  | @stomp/stompjs + sockjs-client     | Real-time bidirectional communication        |
| Backend    | Spring Boot 4.0.2, Java 21        | REST API + WebSocket server                  |
| Security   | Spring Security 6 + JWT + OAuth2   | Auth, authorization, token management        |
| ORM        | Spring Data JPA + Hibernate        | Object-relational mapping                    |
| Database   | PostgreSQL                         | Primary relational database                  |
| Migration  | Flyway                             | Version-controlled schema migrations         |
| Build      | Maven (backend), npm (frontend)    | Dependency management + build pipeline       |

---

## 3. Project Structure

### Backend (`collabnest-backend/src/main/java/com/collabnest/backend/`)

```
backend/
├── CollabnestBackendApplication.java    # Entry point — loads .env, starts Spring
│
├── auth/                                # Authentication module
│   ├── AuthController.java              # POST /api/auth/register, /login, /register-admin
│   ├── AuthService.java                 # Registration logic, login logic, password check
│   ├── dto/                             # Auth DTOs
│   │   ├── AuthResponse.java           # { accessToken: "..." }
│   │   ├── LoginRequest.java           # { identifier, password }
│   │   └── RegisterRequest.java        # { email, username, name, password }
│   ├── jwt/
│   │   ├── JwtAuthFilter.java          # Intercepts every request, validates Bearer token
│   │   └── JwtService.java             # Generate/validate/parse JWT tokens
│   └── oauth2/
│       ├── CustomOAuth2UserService.java   # Handles user auto-registration from Google/GitHub
│       ├── OAuth2AuthenticationSuccessHandler.java  # On success → generate JWT → redirect to frontend
│       ├── OAuth2AuthenticationFailureHandler.java  # On failure → redirect to /login?error=true
│       ├── OAuth2UserInfo.java            # Abstract: getId(), getName(), getEmail()
│       ├── OAuth2UserInfoFactory.java     # Routes to GoogleOAuth2UserInfo or GithubOAuth2UserInfo
│       ├── GoogleOAuth2UserInfo.java      # Extracts sub, name, email, picture from Google
│       └── GithubOAuth2UserInfo.java      # Extracts id, name/login, email, avatar_url from GitHub
│
├── config/                              # Spring configuration
│   ├── SecurityConfig.java              # Filter chain, CORS, OAuth2, method security
│   ├── PasswordConfig.java              # BCryptPasswordEncoder bean
│   ├── AsyncConfig.java                 # Thread pool for @Async (activity logs, notifications)
│   ├── WebSocketConfig.java             # STOMP endpoints, message broker
│   └── websocket/
│       └── WebSocketAuthInterceptor.java # Authenticates WebSocket CONNECT via JWT
│
├── security/                            # Authorization layer
│   ├── UserPrincipal.java               # Implements UserDetails + OAuth2User
│   ├── CustomUserDetailsService.java    # Loads user by email/username from DB
│   ├── WorkspacePermissionService.java  # Role hierarchy: OWNER(4) > ADMIN(3) > MEMBER(2) > VIEWER(1)
│   └── WorkspacePermissionEvaluator.java # Custom PermissionEvaluator for @PreAuthorize
│
├── controller/                          # REST API endpoints
│   ├── WorkspaceController.java         # /api/workspaces/** — CRUD, invite, join, members
│   ├── BoardController.java             # /api/workspaces/{id}/boards/** — boards + columns
│   ├── TaskController.java              # /api/workspaces/{id}/columns/{id}/tasks/** — CRUD, move, assign
│   ├── DocumentController.java          # /api/workspaces/{id}/documents/** — CRUD + comments
│   ├── ChatController.java              # /api/chat/** — workspace/direct chats + messages
│   ├── FileController.java              # /api/workspaces/{id}/files/** — file metadata CRUD
│   ├── UserController.java              # /api/user/profile — get/update profile
│   ├── AdminController.java             # /api/admin/** — user management (ADMIN only)
│   ├── HelloController.java             # GET /hello — health check
│   └── HomeController.java              # GET / — welcome message
│
├── service/                             # Business logic interfaces
│   ├── WorkspaceService.java
│   ├── BoardService.java
│   ├── ColumnService.java
│   ├── TaskService.java
│   ├── DocumentService.java
│   ├── CommentService.java
│   ├── ChatService.java
│   ├── FileStorageService.java
│   ├── UserService.java                 # (concrete class, not interface)
│   └── impl/                            # Implementations
│       ├── WorkspaceServiceImpl.java
│       ├── BoardServiceImpl.java
│       ├── ColumnServiceImpl.java
│       ├── TaskServiceImpl.java
│       ├── DocumentServiceImpl.java
│       ├── CommentServiceImpl.java
│       ├── ChatServiceImpl.java
│       └── FileStorageServiceImpl.java
│
├── domain/                              # JPA entities + enums
│   ├── base/
│   │   └── BaseEntity.java             # createdAt/updatedAt with @PreUpdate
│   ├── enums/
│   │   ├── UserRole.java               # USER, ADMIN
│   │   ├── WorkspaceRole.java          # OWNER, ADMIN, MEMBER, VIEWER
│   │   ├── AuthProvider.java           # LOCAL, GOOGLE, GITHUB, LINKEDIN
│   │   ├── TaskPriority.java           # LOW, MEDIUM, HIGH, CRITICAL
│   │   ├── ChatType.java              # WORKSPACE, DIRECT
│   │   ├── LinkedEntityType.java      # TASK, DOCUMENT, NONE
│   │   ├── NotificationType.java      # MENTION, TASK_ASSIGNED, etc.
│   │   └── ActivityType.java          # WORKSPACE_CREATED, TASK_MOVED, etc.
│   └── entity/
│       ├── User.java
│       ├── Workspace.java
│       ├── WorkspaceMember.java
│       ├── Board.java
│       ├── BoardColumn.java
│       ├── Task.java
│       ├── Document.java
│       ├── Comment.java
│       ├── Chat.java                  # Base chat (uses JOINED inheritance)
│       ├── WorkspaceChat.java         # 1:1 with Chat, links to Workspace
│       ├── DirectChat.java            # 1:1 with Chat, links userOne + userTwo
│       ├── ChatMessage.java
│       ├── ActivityLog.java
│       ├── Notification.java
│       ├── FileEntity.java
│       ├── TaskDependency.java
│       └── TaskDocumentLink.java
│
├── repository/                          # Spring Data JPA repositories
│   ├── UserRepository.java
│   ├── WorkspaceRepository.java
│   ├── WorkspaceMemberRepository.java
│   ├── BoardRepository.java
│   ├── BoardColumnRepository.java
│   ├── TaskRepository.java
│   ├── DocumentRepository.java
│   ├── CommentRepository.java
│   ├── ChatRepository.java
│   ├── WorkspaceChatRepository.java
│   ├── DirectChatRepository.java
│   ├── ChatMessageRepository.java
│   ├── ActivityLogRepository.java
│   ├── NotificationRepository.java
│   ├── FileRepository.java
│   ├── TaskDependencyRepository.java
│   └── TaskDocumentLinkRepository.java
│
├── notification/                        # Notification feature module
│   ├── NotificationService.java        # Interface
│   ├── NotificationController.java     # REST endpoints
│   ├── impl/
│   │   └── NotificationServiceImpl.java # @Mention detection, WebSocket push
│   └── dto/
│       ├── NotificationDto.java
│       └── NotificationStatsDto.java
│
├── activity/                            # Activity logging module
│   ├── ActivityLogService.java         # Interface
│   ├── ActivityController.java         # REST endpoints
│   ├── impl/
│   │   └── ActivityLogServiceImpl.java # @Async logging to DB
│   └── dto/
│       └── ActivityLogDto.java
│
├── websocket/                           # WebSocket events
│   ├── listener/
│   │   └── WebSocketEventListener.java # Publishes presence (ONLINE/OFFLINE) events
│   └── dto/
│       ├── TaskEvent.java             # TASK_CREATED/UPDATED/MOVED/DELETED/ASSIGNED
│       ├── DocumentEvent.java         # DOCUMENT_CREATED/UPDATED/DELETED, COMMENT_*
│       └── PresenceEvent.java         # USER_ONLINE/OFFLINE/JOINED_*/LEFT_*
│
├── exception/                           # Custom exceptions + global handler
│   ├── ResourceNotFoundException.java  # → 404
│   ├── DuplicateResourceException.java # → 409
│   ├── UnauthorizedException.java      # → 401
│   ├── AccountDisabledException.java   # → 403
│   ├── InvalidOperationException.java  # → 400
│   ├── OAuth2AuthenticationProcessingException.java # → login redirect
│   └── GlobalExceptionHandler.java     # @RestControllerAdvice — catches all
│
└── dto/                                 # Data Transfer Objects
    ├── board/    { BoardResponse, CreateBoardRequest, ColumnResponse, CreateColumnRequest }
    ├── task/     { TaskResponse, CreateTaskRequest, MoveTaskRequest, AssignTaskRequest }
    ├── document/ { DocumentResponse, CreateDocumentRequest, UpdateDocumentRequest }
    ├── comment/  { CommentResponse, CreateCommentRequest }
    ├── workspace/ { WorkspaceResponse, CreateWorkspaceRequest, InviteMemberRequest,
    │               JoinWorkspaceRequest, WorkspaceMemberResponse, ChangeMemberRoleRequest }
    ├── user/     { UserResponse, UpdateProfileRequest }
    ├── chat/     { ChatResponse, ChatMessageResponse, SendMessageRequest }
    └── file/     { FileResponse }
```

### Frontend (`frontend/src/`)

```
src/
├── app/
│   ├── layout.tsx              # Root layout — Inter font, Providers wrapper
│   ├── page.tsx                # Landing page (/)
│   ├── globals.css             # Tailwind + custom CSS
│   ├── login/page.tsx          # Login form + OAuth buttons
│   ├── register/page.tsx       # Registration form + OAuth buttons
│   └── (app)/                  # Authenticated route group
│       ├── layout.tsx          # Wraps children in AppLayout (sidebar + header)
│       ├── dashboard/page.tsx  # User dashboard — list workspaces
│       ├── notifications/page.tsx
│       ├── profile/page.tsx
│       └── workspace/[workspaceId]/
│           ├── boards/page.tsx      # List boards in workspace
│           ├── board/[boardId]/page.tsx  # Kanban board view
│           ├── documents/page.tsx   # Document list
│           ├── documents/[documentId]/page.tsx  # Document editor + comments
│           ├── chat/page.tsx        # Workspace + direct chat
│           ├── files/page.tsx       # File manager
│           ├── members/page.tsx     # Member management
│           ├── activity/page.tsx    # Activity feed
│           └── settings/page.tsx    # Workspace settings
│
├── components/
│   ├── app-layout.tsx          # Sidebar + header + workspace selector
│   ├── providers.tsx           # QueryClientProvider + AuthInitializer
│   └── ui/index.tsx            # Reusable UI components (Button, Input, Avatar, etc.)
│
└── lib/
    ├── api.ts                  # HTTP client — all API calls organized by resource
    ├── types.ts                # TypeScript interfaces matching backend DTOs
    ├── store.ts                # Zustand stores: useAuthStore, useWorkspaceStore
    └── utils.ts                # cn() utility for class merging
```

---

## 4. Database Schema & Entity Relationships

```
┌──────────────┐        ┌──────────────────┐        ┌──────────────┐
│    users     │        │   workspaces     │        │    boards    │
├──────────────┤        ├──────────────────┤        ├──────────────┤
│ id (PK,UUID) │◄──────┐│ id (PK,UUID)     │◄──────┐│ id (PK,UUID) │
│ username     │       ││ name             │       ││ workspace_id │──► workspaces
│ email        │       ││ owner_id ────────┘       ││ name         │
│ password_hash│       ││ invite_token     │       ││ position     │
│ name         │       ││ created_at       │       ││ created_at   │
│ auth_provider│       ││ updated_at       │       │└──────────────┘
│ provider_id  │       │└──────────────────┘       │       │
│ github_id    │       │                           │       ▼
│ google_id    │       │                           │┌──────────────┐
│ role         │       │                           ││ board_columns│
│ enabled      │       │                           │├──────────────┤
│ created_at   │       │                           ││ id (PK,UUID) │
└──────────────┘       │                           ││ board_id ────┘
       │               │                           ││ name
       │               │                           ││ position
       ▼               ▼                           │└──────────────┘
┌──────────────────────┐                           │       │
│  workspace_members   │                           │       ▼
├──────────────────────┤                           │┌──────────────┐
│ id (PK,UUID)         │                           ││    tasks     │
│ workspace_id ────────┘                           │├──────────────┤
│ user_id ─────────────► users                     ││ id (PK,UUID) │
│ role (OWNER/ADMIN/MEMBER/VIEWER)                 ││ column_id ───┘
│ is_primary_owner     │                           ││ title
│ joined_at            │                           ││ description
└──────────────────────┘                           ││ priority
                                                   ││ due_date
┌──────────────┐                                   ││ position
│  documents   │                                   ││ assignee_id ──► users
├──────────────┤                                   ││ created_by_id─► users
│ id (PK,UUID) │                                   ││ version (optimistic lock)
│ workspace_id │──► workspaces                     ││ created_at
│ title        │                                   ││ updated_at
│ content      │                                   │└──────────────┘
│ created_by_id│──► users                          │
│ created_at   │                                   │
│ updated_at   │                                   │
└──────────────┘                                   │

┌──────────────┐   ┌──────────────┐   ┌──────────────────┐
│   comments   │   │    chats     │   │  chat_messages   │
├──────────────┤   ├──────────────┤   ├──────────────────┤
│ id (PK,UUID) │   │ id (PK,UUID) │◄──│ chat_id          │
│ entity_id    │   │ chat_type    │   │ id (PK,UUID)     │
│ entity_type  │   │ created_at   │   │ sender_id ──► users
│ content      │   └──────┬───────┘   │ content          │
│ created_by_id│          │           │ linked_entity_type│
│ created_at   │          ├───────┐   │ linked_entity_id │
└──────────────┘          ▼       ▼   │ created_at       │
               ┌──────────────┐ ┌──────────────┐         │
               │workspace_chats│ │ direct_chats │         │
               ├──────────────┤ ├──────────────┤         │
               │chat_id (PK)  │ │chat_id (PK)  │         │
               │workspace_id  │ │user_one_id   │         │
               └──────────────┘ │user_two_id   │         │
                                └──────────────┘         │
                                                         │
┌──────────────┐   ┌──────────────┐   ┌──────────────────┘
│notifications │   │activity_logs │   │
├──────────────┤   ├──────────────┤   │  ┌──────────────┐
│ id (PK,UUID) │   │ id (PK,UUID) │   │  │    files     │
│ user_id      │   │ workspace_id │   │  ├──────────────┤
│ workspace_id │   │ user_id      │   │  │ id (PK,UUID) │
│ notif_type   │   │ activity_type│   │  │ workspace_id │
│ title        │   │ entity_type  │   │  │ uploaded_by  │
│ message      │   │ entity_id    │   │  │ task_id      │
│ entity_type  │   │ entity_name  │   │  │ document_id  │
│ entity_id    │   │ description  │   │  │ file_name    │
│ actor_id     │   │ metadata     │   │  │ file_url     │
│ is_read      │   │ created_at   │   │  │ size         │
│ created_at   │   └──────────────┘   │  │ created_at   │
│ read_at      │                      │  └──────────────┘
└──────────────┘                      │
                                      │
                    ┌─────────────────┼──────────────────┐
                    │task_dependencies│ task_document_links│
                    ├─────────────────┤──────────────────┤
                    │task_id (PK)     │ task_id (PK)     │
                    │depends_on(PK)   │ document_id (PK) │
                    └─────────────────┴──────────────────┘
```

### Key Relationships

| Relationship | Type | Description |
|---|---|---|
| User ↔ Workspace | Many-to-Many | Through `workspace_members` with role |
| Workspace → Board | One-to-Many | Each workspace has multiple boards |
| Board → BoardColumn | One-to-Many | Columns represent Kanban stages |
| BoardColumn → Task | One-to-Many | Tasks live inside columns |
| Task → User (assignee) | Many-to-One | Optional: one assignee per task |
| Task → User (createdBy) | Many-to-One | Who created the task |
| Workspace → Document | One-to-Many | Documents belong to workspace |
| Comment → any entity | Polymorphic | `entity_type` + `entity_id` (task or document) |
| Chat (base) → WorkspaceChat | 1:1 (JOINED inheritance) | One chat per workspace |
| Chat (base) → DirectChat | 1:1 (JOINED inheritance) | DM between two users |
| Chat → ChatMessage | One-to-Many | Messages in a chat |
| Task ↔ Task | Many-to-Many | Through `task_dependencies` |
| Task ↔ Document | Many-to-Many | Through `task_document_links` |
| File → Workspace/Task/Document | Many-to-One | Optional links to task or document |

---

## 5. Authentication & Security Deep Dive

### 5.1 Two Auth Paths

CollabNest supports **two authentication methods**:

#### Path A: Email/Password (Local Auth)
```
User fills form → POST /api/auth/register or /login
→ AuthController → AuthService
→ BCrypt password hash check
→ JwtService.generateToken(email, {userId, role})
→ Returns { accessToken: "eyJhbG..." }
→ Frontend stores token in localStorage
```

#### Path B: OAuth2 (Google/GitHub)
```
User clicks "Sign in with Google" → browser navigates to:
  http://localhost:8080/oauth2/authorization/google

→ Spring Security redirects to Google's consent screen
→ User grants permission → Google redirects back with auth code
→ Spring exchanges code for access token
→ CustomOAuth2UserService.loadUser() is called:
    1. Fetches user profile from Google/GitHub
    2. Checks: does user with this provider ID exist? → update name
    3. No? Does user with this email exist? → link provider to existing account
    4. No? → Create brand new user (auto-generate username from email)
→ OAuth2AuthenticationSuccessHandler:
    - Generates JWT with userId + role claims
    - Redirects to: http://localhost:3000/dashboard?token=eyJhbG...
→ Frontend's AuthInitializer in providers.tsx:
    - Detects ?token= in URL
    - Calls useAuthStore.login(token)
    - Removes token from URL bar
    - Fetches user profile
```

### 5.2 JWT Token Structure

```
Header:  { alg: "HS256" }
Payload: {
  sub: "user@example.com",      // email (used to load UserDetails)
  userId: "550e8400-...",        // UUID string
  role: "USER",                  // or "ADMIN"
  iat: 1709337600,               // issued at
  exp: 1709348400                // expires (3 hours later)
}
Signature: HMAC-SHA256(base64(header).base64(payload), secret)
```

### 5.3 How Every Request is Authenticated (JwtAuthFilter)

On **every** HTTP request, the `JwtAuthFilter` (a `OncePerRequestFilter`) runs:

```
1. Check: Is there an "Authorization: Bearer xxx" header?
   NO  → skip, let Spring Security handle (will be anonymous)
   YES → extract token from "Bearer xxx"

2. Is token valid? (not expired, signature matches)
   NO  → skip (will be treated as unauthenticated)
   YES → extract subject (email) from token

3. Is SecurityContext already populated?
   YES → skip (already authenticated, e.g., OAuth2)
   NO  → load UserPrincipal via CustomUserDetailsService.loadUserByUsername(email)
         ↳ queries DB: findByEmailOrUsername(email, email)
         ↳ wraps User entity in UserPrincipal (implements UserDetails)

4. Set authentication:
   UsernamePasswordAuthenticationToken(userPrincipal, null, authorities)
   → SecurityContextHolder.getContext().setAuthentication(auth)

5. Continue filter chain → request reaches controller
```

### 5.4 Workspace Permission System

The app has **two levels of authorization**:

#### Level 1: Global Roles (UserRole)
- `USER` — standard user (can create workspaces, join workspaces)
- `ADMIN` — platform admin (can manage all users via `/api/admin/**`)

Checked with: `@PreAuthorize("hasRole('ADMIN')")`

#### Level 2: Workspace Roles (WorkspaceRole)
Each user has a **role per workspace** via the `workspace_members` table:

| Role | Level | Can Do |
|------|-------|--------|
| `VIEWER` | 1 | Read boards, tasks, documents, members, activity |
| `MEMBER` | 2 | Everything VIEWER can + create/edit/delete tasks, docs, comments, upload files |
| `ADMIN` | 3 | Everything MEMBER can + invite/remove members, change roles, delete boards/columns |
| `OWNER` | 4 | Everything ADMIN can + delete workspace, is marked as `isPrimaryOwner` |

Checked with: `@PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'MEMBER')")`

This flows through:
```
@PreAuthorize annotation
  → MethodSecurityExpressionHandler
    → WorkspacePermissionEvaluator.hasPermission(auth, workspaceId, "MEMBER")
      → extracts userId from UserPrincipal
      → calls WorkspacePermissionService.hasMinimumRole(userId, workspaceId, MEMBER)
        → queries workspace_members table for user's role
        → compares role levels: OWNER(4) >= MEMBER(2) → true ✓
```

---

## 6. Request Lifecycle — How Every HTTP Request Flows

Here's the complete journey of a typical request, e.g., **creating a task**:

```
FRONTEND                           BACKEND
────────                           ───────
1. User clicks "Add Task" button

2. React component calls:
   api.tasks.create(workspaceId, columnId, {
     title: "Fix bug",
     priority: "HIGH"
   })

3. api.ts → request() function:
   - Gets token from localStorage
   - Adds "Authorization: Bearer eyJ..."
   - Adds "Content-Type: application/json"
   - Sends: POST /api/workspaces/{wid}/columns/{cid}/tasks
     Body: {"title":"Fix bug","priority":"HIGH"}

                                   4. Request hits Spring Boot at port 8080

                                   5. CORS Filter:
                                      - Checks Origin header
                                      - Currently allows * (all origins)
                                      - Adds CORS response headers

                                   6. JwtAuthFilter.doFilterInternal():
                                      - Extracts "Bearer eyJ..." from header
                                      - jwtService.isTokenValid(token) → true
                                      - jwtService.extractSubject(token) → "user@email.com"
                                      - CustomUserDetailsService.loadUserByUsername("user@email.com")
                                        → queries: SELECT * FROM users WHERE email='user@email.com'
                                        → wraps in UserPrincipal
                                      - Sets SecurityContext authentication

                                   7. Spring Security AuthorizationFilter:
                                      - Path /api/workspaces/... → requires authenticated ✓

                                   8. Method Security Interceptor:
                                      - @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'MEMBER')")
                                      - WorkspacePermissionEvaluator:
                                        → queries: SELECT * FROM workspace_members
                                                   WHERE workspace_id=? AND user_id=?
                                        → user has MEMBER role (level 2) >= MEMBER (level 2) → ✓

                                   9. TaskController.createTask() executes:
                                      - Validates @Valid @RequestBody (title not blank)
                                      - Extracts userId from @AuthenticationPrincipal UserPrincipal
                                      - Calls taskService.createTask(...)

                                   10. TaskServiceImpl.createTask():
                                       a. boardColumnRepository.findById(columnId) → BoardColumn entity
                                       b. userRepository.findById(userId) → User entity
                                       c. taskRepository.findMaxPositionByColumnId(columnId) → e.g., 3
                                       d. Builds Task entity (position = 4)
                                       e. taskRepository.save(task) → Hibernate INSERT into tasks table
                                       f. Builds TaskEvent (type=TASK_CREATED)
                                       g. messagingTemplate.convertAndSend("/topic/board/{boardId}", event)
                                          → pushes to ALL WebSocket subscribers of that board
                                       h. activityLogService.logActivity(...) → @Async
                                          → runs on separate thread pool
                                          → INSERT into activity_logs table

                                   11. Controller returns ResponseEntity.ok(TaskResponse.fromEntity(task))
                                       → Jackson serializes to JSON

                                   12. Response:
                                       HTTP 200 OK
                                       Content-Type: application/json
                                       {
                                         "id": "550e8400-...",
                                         "columnId": "...",
                                         "title": "Fix bug",
                                         "priority": "HIGH",
                                         "position": 4,
                                         "assigneeId": null,
                                         "createdById": "...",
                                         "createdAt": "2026-03-02T...",
                                         "updatedAt": "2026-03-02T..."
                                       }

13. api.ts → request() function:
    - res.ok → true
    - JSON.parse(text) → TaskResponse object
    - Returns to component

14. React Query cache is updated
    → component re-renders with new task

15. SIMULTANEOUSLY via WebSocket:
    - All other users viewing the same board
      receive the TaskEvent on /topic/board/{boardId}
    - Their React components update in real-time
```

---

## 7. Feature-by-Feature Walkthroughs

### 7.1 User Registration & Login

#### Registration Flow
```
POST /api/auth/register
Body: { email, username, name, password }

AuthService.register():
1. Check: username taken? → 409 Conflict
2. Check: email exists?
   YES + has password → 409 "Email already registered"
   YES + no password (OAuth-only account) → LINK: set password, update name/username
   NO → Create new User entity (provider=LOCAL, role=USER, enabled=true)
3. BCrypt encode password → save to password_hash
4. userRepository.save(user)
5. Generate JWT with claims: { userId, role }
6. Return { accessToken: "eyJ..." }
```

#### Login Flow
```
POST /api/auth/login
Body: { identifier, password }
(identifier can be email OR username)

AuthService.login():
1. findByEmailOrUsername(identifier, identifier)
   NOT FOUND → 404 "User not found"
2. user.hasPassword()?
   NO → 401 "This account uses GOOGLE sign-in. Please log in with GOOGLE"
3. BCrypt matches(password, user.passwordHash)?
   NO → 401 "Invalid credentials"
4. user.getEnabled()?
   NO → 403 "Account has been disabled"
5. Generate JWT → return { accessToken }
```

### 7.2 OAuth2 Login (Google/GitHub)

```
1. Frontend: <a href="http://localhost:8080/oauth2/authorization/google">

2. Spring Security OAuth2 Client:
   → Redirects to https://accounts.google.com/o/oauth2/auth?client_id=...&redirect_uri=...

3. User consents → Google redirects to backend callback URL with auth code

4. Spring exchanges code for access token with Google

5. CustomOAuth2UserService.loadUser(oAuth2UserRequest):
   a. Calls Google API to get user profile
   b. OAuth2UserInfoFactory → GoogleOAuth2UserInfo (extracts sub, name, email)
   c. Try find by googleId → existing user? Update name, return
   d. Try find by email → existing account? Link Google provider to it
   e. Not found? Register new user:
      - email from Google
      - name from Google
      - username = email prefix (e.g., "john" from "john@gmail.com")
      - If username taken, append counter: john, john1, john2, ...
      - authProvider = GOOGLE, googleId = sub
   f. Return UserPrincipal.create(user, attributes)

6. OAuth2AuthenticationSuccessHandler.onAuthenticationSuccess():
   → Generate JWT → redirect to http://localhost:3000/dashboard?token=eyJ...

7. Frontend providers.tsx AuthInitializer:
   → Detects ?token= in URL
   → Calls useAuthStore.login(token)
   → Removes token from URL
   → Fetches user profile from /api/user/profile
```

**GitHub special case:** GitHub may not return email in the main profile. The system makes an additional API call to `https://api.github.com/user/emails` to fetch the primary verified email.

### 7.3 Workspaces & Membership

#### Create Workspace
```
POST /api/workspaces
Auth: any authenticated user
Body: { name: "My Project" }

WorkspaceServiceImpl.createWorkspace():
1. Find user entity
2. Create Workspace: { name, ownerId, inviteToken: UUID.randomUUID() }
3. Create WorkspaceMember: { user, workspace, role=OWNER, isPrimaryOwner=true }
4. Return workspace
```

#### Invite Member
```
POST /api/workspaces/{id}/invite
Auth: workspace ADMIN+
Body: { email: "bob@example.com", role: "MEMBER" }

WorkspaceServiceImpl.inviteMember():
1. Find workspace
2. Find user by email → 404 if not found
3. Already a member? → 409 Conflict
4. addMember(workspaceId, userId, role)
5. Return workspace's inviteToken (sharable link token)
```

#### Join via Invite Token
```
POST /api/workspaces/join
Auth: any authenticated user
Body: { inviteToken: "550e8400-..." }

WorkspaceServiceImpl.joinWorkspace():
1. Find workspace by invite token → 404 if invalid
2. Already a member? → 409 Conflict
3. Add as MEMBER role
```

#### Role Hierarchy for Workspace Operations
| Operation | Min Role |
|---|---|
| View boards, tasks, docs, members, activity | VIEWER |
| Create/edit/delete tasks, docs, comments, files | MEMBER |
| Invite/remove members, change roles, delete boards | ADMIN |
| Delete workspace | OWNER |

### 7.4 Boards, Columns & Tasks (Kanban)

The Kanban system has three levels: **Board → Columns → Tasks**

#### Data Model
```
Workspace
  └── Board (e.g., "Sprint 1") — has a position for ordering
        ├── Column "To Do" (position: 0)
        │     ├── Task "Fix login bug" (position: 0, priority: HIGH)
        │     └── Task "Add dark mode" (position: 1, priority: LOW)
        ├── Column "In Progress" (position: 1)
        │     └── Task "Build dashboard" (position: 0, priority: MEDIUM)
        └── Column "Done" (position: 2)
```

#### Create Task
```
POST /api/workspaces/{wid}/columns/{cid}/tasks
Auth: workspace MEMBER+
Body: { title, description?, priority?, dueDate? }

TaskServiceImpl.createTask():
1. Find column → verify it belongs to the workspace
2. Find user (createdBy)
3. Auto-calculate position: MAX(position) + 1 in this column
4. Save task
5. 📡 Push TaskEvent(TASK_CREATED) to /topic/board/{boardId} → real-time
6. 📝 Log activity (async): "Created task: Fix login bug"
```

#### Move Task (Drag & Drop)
```
PUT /api/workspaces/{wid}/columns/{cid}/tasks/{tid}/move
Body: { targetColumnId, position }

TaskServiceImpl.moveTask():
1. Moving to different column?
   YES →
     a. In old column: shift DOWN all tasks after moved task's position
     b. In new column: shift UP all tasks at/after target position
     c. Set task's column = new column
   NO → same column reorder:
     a. If moving down: shift tasks between old→new position UP by 1
     b. If moving up: shift tasks between new→old position DOWN by 1
2. Set task.position = target position
3. Save
4. 📡 Push TaskEvent(TASK_MOVED) → real-time board update
```

#### Assign Task
```
PUT /api/workspaces/{wid}/columns/{cid}/tasks/{tid}/assign
Body: { userId: "..." }

TaskServiceImpl.assignTask():
1. Verify assignee is a workspace member → 401 if not
2. task.setAssignee(user)
3. Save
4. 📡 Push TaskEvent(TASK_ASSIGNED) → real-time
```

**Optimistic Locking:** Tasks have a `@Version` column. If two users edit the same task simultaneously, the second save will throw `OptimisticLockException`.

### 7.5 Documents & Comments

#### Create Document
```
POST /api/workspaces/{wid}/documents
Auth: workspace MEMBER+
Body: { title: "Design Spec", content: "## Overview\n..." }

DocumentServiceImpl.createDocument():
1. Find workspace + user
2. Save document
3. 📡 Push DocumentEvent(DOCUMENT_CREATED) to /topic/workspace/{wid}
4. 📝 Log activity: "Created document: Design Spec"
```

#### Add Comment
```
POST /api/workspaces/{wid}/documents/{did}/comments
Auth: workspace MEMBER+
Body: { content: "Nice work @john!" }

CommentServiceImpl.createComment():
1. Save comment (entityId=documentId, entityType="document")
2. notificationService.detectMentions("Nice work @john!")
   → regex @([a-zA-Z0-9._-]+) → finds "john"
   → userRepository.findByUsername("john") → found! → [userId]
3. notificationService.createMentionNotifications(...) @Async
   → Creates Notification: "username mentioned you in document: comment"
   → 📡 Pushes to /topic/user/{mentionedUserId}/notifications
```

### 7.6 Chat System

The chat system uses **table inheritance** (JOINED strategy):

```
Chat (base table: id, chat_type, created_at)
  ├── WorkspaceChat (chat_id FK, workspace_id) — one per workspace
  └── DirectChat (chat_id FK, user_one_id, user_two_id) — DM between two users
```

#### Get/Create Workspace Chat
```
POST /api/chat/workspace/{workspaceId}
Auth: workspace MEMBER+

ChatServiceImpl.getOrCreateWorkspaceChat():
- Find existing WorkspaceChat for this workspace → return it
- Not found? Create Chat(WORKSPACE) + WorkspaceChat → return it
```

#### Get/Create Direct Chat
```
POST /api/chat/direct/{otherUserId}
Auth: any authenticated user

ChatServiceImpl.getOrCreateDirectChat():
- Normalize user IDs (smaller UUID first) to prevent duplicates
- Find existing DirectChat → return it
- Not found? Create Chat(DIRECT) + DirectChat → return it
```

#### Send Message
```
POST /api/chat/{chatId}/messages
Auth: any authenticated user
Body: { content: "Hello!", linkedEntityType?: "TASK", linkedEntityId?: "..." }

ChatServiceImpl.sendMessage():
1. Find chat + sender
2. Save ChatMessage(content, linkedEntityType, linkedEntityId)
3. 📡 Push message to /topic/chat/{chatId} → real-time delivery
```

Messages can optionally link to a task or document (`linkedEntityType` + `linkedEntityId`).

#### Get Messages (Paginated)
```
GET /api/chat/{chatId}/messages?page=0&size=50
→ Returns Page<ChatMessageResponse> ordered by createdAt DESC
→ Uses @EntityGraph to eagerly load sender + chat (avoids N+1)
```

### 7.7 File Management

Files are stored as **metadata records** (the actual file lives externally, e.g., a cloud URL).

```
POST /api/workspaces/{wid}/files?fileName=report.pdf&fileUrl=https://...&size=1024
Auth: workspace MEMBER+
Optional: taskId, documentId (to link file to task or document)

FileStorageServiceImpl.storeFileMetadata():
1. Find workspace, user
2. Optionally find task/document to link
3. Save FileEntity { workspace, uploadedBy, fileName, fileUrl, size, task?, document? }
```

### 7.8 Notifications

Notifications are created by various services and delivered via WebSocket:

| Trigger | NotificationType | Created By |
|---|---|---|
| `@username` in comment | `MENTION` | CommentServiceImpl |
| Task assigned | `TASK_ASSIGNED` | (could be added to TaskServiceImpl) |
| Member invited | `WORKSPACE_INVITE` | (could be added to WorkspaceServiceImpl) |

```
NotificationServiceImpl.createNotification():
1. Build Notification entity
2. Save to DB
3. 📡 Push to /topic/user/{userId}/notifications → real-time bell icon update

NotificationController endpoints:
GET  /api/notifications?page=0&size=20&unreadOnly=false  → paginated list
GET  /api/notifications/stats → { totalCount, unreadCount }
PUT  /api/notifications/{id}/read → mark single as read
PUT  /api/notifications/read-all → mark all as read
DEL  /api/notifications/{id} → delete
```

The frontend polls notification stats every 30 seconds via React Query's `refetchInterval: 30_000`.

### 7.9 Activity Logs

Every significant action is logged **asynchronously** (via `@Async` on a separate thread pool):

```
ActivityLogServiceImpl.logActivity() — @Async @Transactional:
1. Find workspace + user
2. Build ActivityLog { workspace, user, activityType, entityType, entityId, entityName, description, metadata }
3. Save to DB
4. If save fails → log error (never throws to caller)
```

Services that log activities:
- `TaskServiceImpl` → TASK_CREATED, TASK_UPDATED, TASK_DELETED
- `DocumentServiceImpl` → DOCUMENT_CREATED, DOCUMENT_UPDATED, DOCUMENT_DELETED
- `BoardServiceImpl` → BOARD_DELETED
- `ColumnServiceImpl` → COLUMN_DELETED

```
ActivityController endpoints:
GET /api/activity/workspace/{wid}?page=0&size=20 → workspace activity feed
GET /api/activity/workspace/{wid}/user/{uid}      → user's activities in workspace
GET /api/activity/me?workspaceId=...              → current user's activities
```

### 7.10 Admin Panel

Platform admins (`UserRole.ADMIN`) have special endpoints:

```
AdminController — @PreAuthorize("hasRole('ADMIN')") on class level:

GET  /api/admin/dashboard     → admin stats
GET  /api/admin/users         → all users list
GET  /api/admin/users/{id}    → single user
PUT  /api/admin/users/{id}/role?role=ADMIN  → change user's global role
PUT  /api/admin/users/{id}/enable?enabled=false → disable/enable account
DEL  /api/admin/users/{id}    → delete user
```

---

## 8. WebSocket (Real-Time) Architecture

### Connection Setup
```
Frontend (STOMP over SockJS):
  const client = new Client({
    brokerURL: 'ws://localhost:8080/ws',
    connectHeaders: { Authorization: 'Bearer eyJ...' }
  });
```

### Server-Side Auth
```
WebSocketAuthInterceptor.preSend():
- On STOMP CONNECT:
  1. Extract Authorization header
  2. Validate JWT
  3. Load UserDetails
  4. Set SecurityContext + accessor.setUser(authentication)
```

### Topic Subscriptions

| Topic | Published By | Use Case |
|---|---|---|
| `/topic/board/{boardId}` | TaskServiceImpl | Task CRUD + move + assign events |
| `/topic/workspace/{wid}` | DocumentServiceImpl | Document create/delete events |
| `/topic/document/{docId}` | DocumentServiceImpl, CommentServiceImpl | Document update, comment delete |
| `/topic/chat/{chatId}` | ChatServiceImpl | New chat messages |
| `/topic/presence` | WebSocketEventListener | User online/offline |
| `/topic/user/{userId}/notifications` | NotificationServiceImpl | Personal notifications |

### Event Flow Example (Task Move)
```
User A drags task from "To Do" to "In Progress" in the browser
  → PUT /api/.../tasks/{id}/move { targetColumnId, position }
  → TaskServiceImpl.moveTask() processes and saves
  → messagingTemplate.convertAndSend("/topic/board/{boardId}", TaskEvent)
  → All users subscribed to that board topic receive the event
  → Their boards update instantly without page refresh
```

### Presence Events
```
WebSocketEventListener:
  @EventListener SessionConnectedEvent → broadcast USER_ONLINE to /topic/presence
  @EventListener SessionDisconnectEvent → broadcast USER_OFFLINE to /topic/presence
```

---

## 9. Frontend Architecture

### State Management

**Zustand stores** (`store.ts`):

```
useAuthStore:
  ├── user: User | null
  ├── token: string | null
  ├── isAuthenticated: boolean
  ├── isHydrated: boolean        ← prevents flash of login page on refresh
  ├── isLoading: boolean
  ├── login(token) → save to localStorage → fetch profile
  ├── logout() → clear localStorage → reset state
  └── fetchUser() → GET /api/user/profile

useWorkspaceStore:
  ├── workspaces: Workspace[]
  ├── currentWorkspace: Workspace | null
  ├── fetchWorkspaces() → GET /api/workspaces
  └── setCurrentWorkspace(ws)
```

**React Query** for server state:
- All API data (boards, tasks, documents, members, etc.) is fetched via React Query
- Automatic caching with 30-second stale time
- One retry on failure
- No refetch on window focus

### Routing (Next.js App Router)

```
/                        → Landing page (public)
/login                   → Login form + OAuth buttons
/register                → Registration form + OAuth buttons
/dashboard               → User's workspaces list (protected)
/notifications           → Notification center (protected)
/profile                 → User profile settings (protected)
/workspace/{id}/boards   → Board list (protected, workspace context)
/workspace/{id}/board/{boardId} → Kanban board (protected)
/workspace/{id}/documents      → Document list (protected)
/workspace/{id}/documents/{docId} → Document editor (protected)
/workspace/{id}/chat     → Chat interface (protected)
/workspace/{id}/files    → File manager (protected)
/workspace/{id}/members  → Member management (protected)
/workspace/{id}/activity → Activity feed (protected)
/workspace/{id}/settings → Workspace settings (protected)
```

All routes under `(app)/` are wrapped in `AppLayout` (sidebar navigation + header).

### Hydration Flow (What happens on page load)

```
1. Browser loads page → SSR renders shell (no auth state on server)
2. providers.tsx AuthInitializer runs (client-side):
   a. Check localStorage for existing token
   b. Check URL for ?token= (OAuth2 redirect)
   c. If token found → fetchUser() → set user + isAuthenticated
   d. Set isHydrated = true
3. AppLayout checks:
   - If !isHydrated → render nothing (prevents flash)
   - If !isAuthenticated → redirect to /login
   - If authenticated → render sidebar + content
4. AppLayout loads workspaces on mount
5. URL parsing: if path is /workspace/{id}/..., auto-select that workspace
```

---

## 10. How the Frontend Talks to the Backend

### API Client (`api.ts`)

The `request<T>()` function is the single gateway for all HTTP calls:

```typescript
async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  // 1. Get JWT token from localStorage
  const token = getToken();
  
  // 2. Build headers
  if (token) headers["Authorization"] = `Bearer ${token}`;
  if (body is string) headers["Content-Type"] = "application/json";
  
  // 3. Make fetch call to http://localhost:8080 + path
  const res = await fetch(`${API_BASE}${path}`, { ...options, headers });
  
  // 4. Handle errors
  if (!res.ok) {
    const err = await res.json();  // parse error body
    throw new ApiError(err.message, res.status);
  }
  
  // 5. Handle empty responses (204 No Content)
  if (res.status === 204) return undefined;
  
  // 6. Parse JSON response
  return JSON.parse(await res.text());
}
```

The `api` object organizes all endpoints:
```typescript
api.auth.login({...})                    → POST /api/auth/login
api.workspaces.list()                    → GET  /api/workspaces
api.boards.create(wid, {...})            → POST /api/workspaces/{wid}/boards
api.tasks.move(wid, cid, tid, {...})     → PUT  /api/workspaces/{wid}/columns/{cid}/tasks/{tid}/move
api.chat.sendMessage(chatId, {...})      → POST /api/chat/{chatId}/messages
api.notifications.getStats()             → GET  /api/notifications/stats
```

---

## 11. Error Handling

### Backend: GlobalExceptionHandler

Every exception is caught and returned as a consistent JSON response:

```json
{
  "timestamp": "2026-03-02T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Workspace not found"
}
```

| Exception | HTTP Status | When |
|---|---|---|
| `ResourceNotFoundException` | 404 | Entity not found in DB |
| `DuplicateResourceException` | 409 | Email/username already taken, already a member |
| `UnauthorizedException` | 401 | Bad credentials, OAuth-only account, not a workspace member |
| `AccountDisabledException` | 403 | Disabled account tries to login |
| `InvalidOperationException` | 400 | Remove primary owner, change owner's role |
| `AccessDeniedException` | 403 | @PreAuthorize check failed |
| `MethodArgumentNotValidException` | 400 | @Valid constraint violations (e.g., blank title) |
| `Exception` (catch-all) | 500 | Unexpected errors |

### Frontend: ApiError

```typescript
class ApiError extends Error {
  status: number;  // HTTP status code
}

// Usage in components:
try {
  await api.auth.login({...});
} catch (err) {
  if (err instanceof ApiError) {
    setError(err.message);  // shows server's error message
  }
}
```

---

## 12. Database Migrations (Flyway)

Flyway manages schema evolution via versioned SQL files in `src/main/resources/db/migration/`:

| Version | File | What It Does |
|---|---|---|
| V1 | `create_users.sql` | Creates `users` table |
| V2 | `add_auth_provider_and_role_to_users.sql` | Adds `auth_provider`, `role` columns |
| V3 | `add_username_to_users.sql` | Adds `username` column |
| V4 | `create_workspaces_table.sql` | Creates `workspaces` |
| V5 | `create_workspace_members_table.sql` | Creates `workspace_members` |
| V6 | `create_boards_and_columns_tables.sql` | Creates `boards`, `board_columns` |
| V7 | `create_tasks_table.sql` | Creates `tasks` with FK to columns |
| V8 | `create_documents_and_comments_tables.sql` | Creates `documents`, `comments` |
| V9 | `create_activity_logs.sql` | Creates `activity_logs` |
| V10 | `create_notifications.sql` | Creates `notifications` |
| V11 | `add_provider_id_to_users.sql` | Adds `provider_id` to users |
| V12 | `add_version_to_tasks.sql` | Adds optimistic locking `version` column |
| V13 | `add_timestamps_to_workspace_and_members.sql` | Adds created_at/updated_at |
| V14 | `create_chat_system.sql` | Creates `chats`, `workspace_chats`, `direct_chats`, `chat_messages` |
| V15 | `create_files_table.sql` | Creates `files` |
| V16 | `create_task_dependencies_and_document_links.sql` | Creates junction tables |
| V17 | `add_multi_provider_support.sql` | Adds `github_id`, `google_id` columns |

JPA is set to `ddl-auto=none` — Flyway is the **only** source of schema changes. Hibernate just maps to existing tables.

---

## 13. Configuration & Environment

### `application.properties` breakdown:

```properties
# App identity
spring.application.name=collabnest-backend
server.port=8080

# PostgreSQL connection
spring.datasource.url=jdbc:postgresql://localhost:5432/collabnest_db
spring.datasource.username=collabnest_user
spring.datasource.password=Nani@2910            # ⚠️ Should be env var

# JPA: Hibernate manages ORM, Flyway manages schema
spring.jpa.hibernate.ddl-auto=none               # Flyway handles DDL
spring.jpa.show-sql=true                          # Log SQL (dev only)
spring.jpa.open-in-view=false                     # Performance: no lazy-load in view
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.jdbc.time_zone=UTC

# Flyway
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true

# JWT
jwt.secret=ZmFrZS1qd3Qtc2VjcmV...      # Base64-encoded HMAC key
jwt.expiration-ms=10800000                # 3 hours

# OAuth2 (from environment variables)
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}
spring.security.oauth2.client.registration.github.client-id=${GITHUB_CLIENT_ID}
spring.security.oauth2.client.registration.github.client-secret=${GITHUB_CLIENT_SECRET}
spring.security.oauth2.client.registration.github.scope=user:email

# OAuth2 redirect after success
app.oauth2.authorized-redirect-uri=http://localhost:3000/dashboard

# Actuator endpoints
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=always

# DevTools (hot reload in dev)
spring.devtools.restart.enabled=true
spring.devtools.livereload.enabled=true
```

### `.env` loading
The `CollabnestBackendApplication.main()` uses `dotenv-java` to load a `.env` file (if present) and set values as system properties. This allows OAuth2 secrets to be kept outside source code.

---

## 14. Complete API Reference

### Auth
| Method | Path | Auth | Body | Response |
|---|---|---|---|---|
| POST | `/api/auth/register` | None | `{email, username, name, password}` | `{accessToken}` |
| POST | `/api/auth/login` | None | `{identifier, password}` | `{accessToken}` |
| POST | `/api/auth/register-admin` | ADMIN | `{email, username, name, password}` | `{accessToken}` |

### User
| Method | Path | Auth | Body | Response |
|---|---|---|---|---|
| GET | `/api/user/profile` | Authenticated | — | `UserResponse` |
| PUT | `/api/user/profile` | Authenticated | `{name?, email?, password?}` | `UserResponse` |

### Workspaces
| Method | Path | Auth | Body | Response |
|---|---|---|---|---|
| POST | `/api/workspaces` | Authenticated | `{name}` | `WorkspaceResponse` |
| GET | `/api/workspaces` | Authenticated | — | `WorkspaceResponse[]` |
| GET | `/api/workspaces/{id}` | WS VIEWER+ | — | `WorkspaceResponse` |
| PUT | `/api/workspaces/{id}` | WS ADMIN+ | `{name}` | `WorkspaceResponse` |
| DELETE | `/api/workspaces/{id}` | WS OWNER | — | 204 |
| POST | `/api/workspaces/{id}/invite` | WS ADMIN+ | `{email, role}` | `inviteToken` |
| POST | `/api/workspaces/join` | Authenticated | `{inviteToken}` | 200 |
| GET | `/api/workspaces/{id}/members` | WS VIEWER+ | — | `WorkspaceMemberResponse[]` |
| PUT | `/api/workspaces/{wid}/members/{uid}/role` | WS ADMIN+ | `{role}` | `WorkspaceMemberResponse` |
| DELETE | `/api/workspaces/{wid}/members/{uid}` | WS ADMIN+ | — | 204 |

### Boards & Columns
| Method | Path | Auth | Body | Response |
|---|---|---|---|---|
| POST | `/api/workspaces/{wid}/boards` | WS MEMBER+ | `{name, position}` | `BoardResponse` |
| GET | `/api/workspaces/{wid}/boards` | WS VIEWER+ | — | `BoardResponse[]` |
| GET | `/api/workspaces/{wid}/boards/{bid}` | WS VIEWER+ | — | `BoardResponse` |
| PUT | `/api/workspaces/{wid}/boards/{bid}` | WS MEMBER+ | `{name, position}` | `BoardResponse` |
| DELETE | `/api/workspaces/{wid}/boards/{bid}` | WS ADMIN+ | — | 204 |
| POST | `/api/workspaces/{wid}/boards/{bid}/columns` | WS MEMBER+ | `{name, position}` | `ColumnResponse` |
| GET | `/api/workspaces/{wid}/boards/{bid}/columns` | WS VIEWER+ | — | `ColumnResponse[]` |
| PUT | `/api/workspaces/{wid}/boards/{bid}/columns/{cid}` | WS MEMBER+ | `{name, position}` | `ColumnResponse` |
| DELETE | `/api/workspaces/{wid}/boards/{bid}/columns/{cid}` | WS ADMIN+ | — | 204 |

### Tasks
| Method | Path | Auth | Body | Response |
|---|---|---|---|---|
| POST | `/api/workspaces/{wid}/columns/{cid}/tasks` | WS MEMBER+ | `{title, description?, priority?, dueDate?}` | `TaskResponse` |
| GET | `/api/workspaces/{wid}/columns/{cid}/tasks` | WS VIEWER+ | — | `TaskResponse[]` |
| GET | `/api/workspaces/{wid}/columns/{cid}/tasks/{tid}` | WS VIEWER+ | — | `TaskResponse` |
| PUT | `/api/workspaces/{wid}/columns/{cid}/tasks/{tid}` | WS MEMBER+ | `{title, description?, priority?, dueDate?}` | `TaskResponse` |
| PUT | `/api/workspaces/{wid}/columns/{cid}/tasks/{tid}/move` | WS MEMBER+ | `{targetColumnId, position}` | `TaskResponse` |
| PUT | `/api/workspaces/{wid}/columns/{cid}/tasks/{tid}/assign` | WS MEMBER+ | `{userId}` | `TaskResponse` |
| DELETE | `/api/workspaces/{wid}/columns/{cid}/tasks/{tid}` | WS MEMBER+ | — | 204 |

### Documents & Comments
| Method | Path | Auth | Body | Response |
|---|---|---|---|---|
| POST | `/api/workspaces/{wid}/documents` | WS MEMBER+ | `{title, content}` | `DocumentResponse` |
| GET | `/api/workspaces/{wid}/documents` | WS VIEWER+ | — | `DocumentResponse[]` |
| GET | `/api/workspaces/{wid}/documents/{did}` | WS VIEWER+ | — | `DocumentResponse` |
| PUT | `/api/workspaces/{wid}/documents/{did}` | WS MEMBER+ | `{title, content}` | `DocumentResponse` |
| DELETE | `/api/workspaces/{wid}/documents/{did}` | WS ADMIN+ | — | 204 |
| POST | `/api/workspaces/{wid}/documents/{did}/comments` | WS MEMBER+ | `{content}` | `CommentResponse` |
| GET | `/api/workspaces/{wid}/documents/{did}/comments` | WS VIEWER+ | — | `CommentResponse[]` |
| DELETE | `/api/workspaces/{wid}/documents/{did}/comments/{cid}` | WS MEMBER+ | — | 204 |

### Chat
| Method | Path | Auth | Body | Response |
|---|---|---|---|---|
| POST | `/api/chat/workspace/{wid}` | WS MEMBER+ | — | `ChatResponse` |
| POST | `/api/chat/direct/{otherUserId}` | Authenticated | — | `ChatResponse` |
| GET | `/api/chat/direct` | Authenticated | — | `ChatResponse[]` |
| POST | `/api/chat/{chatId}/messages` | Authenticated | `{content, linkedEntityType?, linkedEntityId?}` | `ChatMessageResponse` |
| GET | `/api/chat/{chatId}/messages?page=0&size=50` | Authenticated | — | `Page<ChatMessageResponse>` |

### Files
| Method | Path | Auth | Body | Response |
|---|---|---|---|---|
| POST | `/api/workspaces/{wid}/files?fileName=...&fileUrl=...&size=...` | WS MEMBER+ | — (query params) | `FileResponse` |
| GET | `/api/workspaces/{wid}/files` | WS VIEWER+ | — | `FileResponse[]` |
| GET | `/api/workspaces/{wid}/files/{fid}` | WS VIEWER+ | — | `FileResponse` |
| DELETE | `/api/workspaces/{wid}/files/{fid}` | WS MEMBER+ | — | 204 |

### Notifications
| Method | Path | Auth | Body | Response |
|---|---|---|---|---|
| GET | `/api/notifications?page=0&size=20&unreadOnly=false` | USER | — | `Page<NotificationDto>` |
| GET | `/api/notifications/stats` | USER | — | `{totalCount, unreadCount}` |
| PUT | `/api/notifications/{id}/read` | USER | — | 200 |
| PUT | `/api/notifications/read-all` | USER | — | 200 |
| DELETE | `/api/notifications/{id}` | USER | — | 204 |

### Activity
| Method | Path | Auth | Body | Response |
|---|---|---|---|---|
| GET | `/api/activity/workspace/{wid}?page=0&size=20` | WS VIEWER+ | — | `Page<ActivityLogDto>` |
| GET | `/api/activity/workspace/{wid}/user/{uid}?page=0&size=20` | WS VIEWER+ | — | `Page<ActivityLogDto>` |
| GET | `/api/activity/me?workspaceId=...&page=0&size=20` | Authenticated | — | `Page<ActivityLogDto>` |

### Admin
| Method | Path | Auth | Body | Response |
|---|---|---|---|---|
| GET | `/api/admin/dashboard` | ADMIN | — | `{message, userId, ...}` |
| GET | `/api/admin/users` | ADMIN | — | `UserResponse[]` |
| GET | `/api/admin/users/{id}` | ADMIN | — | `UserResponse` |
| PUT | `/api/admin/users/{id}/role?role=ADMIN` | ADMIN | — | `UserResponse` |
| PUT | `/api/admin/users/{id}/enable?enabled=false` | ADMIN | — | `UserResponse` |
| DELETE | `/api/admin/users/{id}` | ADMIN | — | `{message}` |

---

*This document was auto-generated from a complete source code analysis of the CollabNest project.*
