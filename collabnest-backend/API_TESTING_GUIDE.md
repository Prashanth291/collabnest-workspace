# CollabNest API Testing Guide

**Last Updated:** January 28, 2026  
**API Version:** 1.0  
**Base URL:** `http://localhost:8080`

## 📋 Table of Contents

1. [Prerequisites](#prerequisites)
2. [Setup & Starting the Application](#setup--starting-the-application)
3. [Authentication Flow](#authentication-flow)
4. [Testing Workspace APIs](#testing-workspace-apis)
5. [Testing Board & Column APIs](#testing-board--column-apis)
6. [Testing Task APIs](#testing-task-apis)
7. [Testing Document & Comment APIs](#testing-document--comment-apis)
8. [Common Errors & Troubleshooting](#common-errors--troubleshooting)
9. [Postman Collection Setup](#postman-collection-setup)

---

## Prerequisites

### Required Tools
- ✅ **Postman** or **Thunder Client** (VS Code extension)
- ✅ **PostgreSQL** running on port 5432
- ✅ **Java 21** installed
- ✅ **Maven** (included as `mvnw`)

### Database Setup
Ensure PostgreSQL is running with:
- **Database:** `collabnest_db`
- **User:** `collabnest_user`
- **Password:** `collabnest_pass`
- **Port:** 5432

```sql
-- Run this if database doesn't exist
CREATE DATABASE collabnest_db;
CREATE USER collabnest_user WITH PASSWORD 'collabnest_pass';
GRANT ALL PRIVILEGES ON DATABASE collabnest_db TO collabnest_user;
```

---

## Setup & Starting the Application

### Step 1: Compile the Project
```bash
cd D:\Projects\CollabNest\collabnest-backend
.\mvnw clean compile
```

### Step 2: Run Database Migrations
```bash
.\mvnw flyway:migrate
```

### Step 3: Start the Application
```bash
.\mvnw spring-boot:run
```

**Expected Output:**
```
Started CollabnestBackendApplication in X.XXX seconds
Tomcat started on port 8080
```

### Step 4: Verify Server is Running
Open browser or Postman and navigate to:
```
http://localhost:8080/actuator/health
```

**Expected Response:**
```json
{
  "status": "UP"
}
```

---

## Authentication Flow

### 1️⃣ Register User 1 (Workspace Owner)

**Endpoint:** `POST /api/auth/register`  
**URL:** `http://localhost:8080/api/auth/register`

**Headers:**
```
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "username": "alice",
  "email": "alice@collabnest.com",
  "password": "password123"
}
```

**Expected Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "username": "alice",
  "email": "alice@collabnest.com",
  "role": "USER"
}
```

**📝 Save the `token` - you'll need it for authenticated requests!**

---

### 2️⃣ Register User 2 (Team Member)

**Endpoint:** `POST /api/auth/register`

**Body (JSON):**
```json
{
  "username": "bob",
  "email": "bob@collabnest.com",
  "password": "password123"
}
```

**Expected Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": "223e4567-e89b-12d3-a456-426614174001",
  "username": "bob",
  "email": "bob@collabnest.com",
  "role": "USER"
}
```

**📝 Save Bob's token separately for testing multi-user scenarios!**

---

### 3️⃣ Login (Alternative to Register)

**Endpoint:** `POST /api/auth/login`  
**URL:** `http://localhost:8080/api/auth/login`

**Body (JSON):**
```json
{
  "username": "alice",
  "password": "password123"
}
```

**Expected Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "username": "alice",
  "email": "alice@collabnest.com",
  "role": "USER"
}
```

---

## Testing Workspace APIs

### 4️⃣ Create Workspace (Alice as Owner)

**Endpoint:** `POST /api/workspaces`  
**URL:** `http://localhost:8080/api/workspaces`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <ALICE_TOKEN>
```

**Body (JSON):**
```json
{
  "name": "Product Development Team"
}
```

**Expected Response (200 OK):**
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "name": "Product Development Team",
  "ownerId": "123e4567-e89b-12d3-a456-426614174000",
  "myRole": "OWNER",
  "createdAt": "2026-01-28T12:30:00.000Z"
}
```

**📝 Save the workspace `id` - you'll use it in all subsequent requests!**

---

### 5️⃣ List My Workspaces

**Endpoint:** `GET /api/workspaces`  
**URL:** `http://localhost:8080/api/workspaces`

**Headers:**
```
Authorization: Bearer <ALICE_TOKEN>
```

**Expected Response (200 OK):**
```json
[
  {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "name": "Product Development Team",
    "ownerId": "123e4567-e89b-12d3-a456-426614174000",
    "myRole": "OWNER",
    "createdAt": "2026-01-28T12:30:00.000Z"
  }
]
```

---

### 6️⃣ Get Workspace Details

**Endpoint:** `GET /api/workspaces/{workspaceId}`  
**URL:** `http://localhost:8080/api/workspaces/a1b2c3d4-e5f6-7890-abcd-ef1234567890`

**Headers:**
```
Authorization: Bearer <ALICE_TOKEN>
```

**Expected Response (200 OK):**
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "name": "Product Development Team",
  "ownerId": "123e4567-e89b-12d3-a456-426614174000",
  "myRole": "OWNER",
  "createdAt": "2026-01-28T12:30:00.000Z"
}
```

---

### 7️⃣ Invite Member to Workspace (Alice invites Bob)

**Endpoint:** `POST /api/workspaces/{workspaceId}/invite`  
**URL:** `http://localhost:8080/api/workspaces/a1b2c3d4-e5f6-7890-abcd-ef1234567890/invite`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <ALICE_TOKEN>
```

**Body (JSON):**
```json
{
  "email": "bob@collabnest.com",
  "role": "MEMBER"
}
```

**Expected Response (200 OK):**
```json
"3f8a9c2b-7d1e-4f5a-9b3c-2e8d7a6f5c4b"
```
*(This is the invite token)*

**📝 Note:** Bob is now automatically added to the workspace. The invite token can be used for joining via link.

---

### 8️⃣ Join Workspace via Invite Token

**Endpoint:** `POST /api/workspaces/join`  
**URL:** `http://localhost:8080/api/workspaces/join`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <USER_TOKEN>
```

**Body (JSON):**
```json
{
  "inviteToken": "3f8a9c2b-7d1e-4f5a-9b3c-2e8d7a6f5c4b"
}
```

**Expected Response (200 OK):**
```json
{}
```

**Use Case:** This is for users who have an invite link but weren't directly invited by email.

---

### 9️⃣ Update Workspace Name

**Endpoint:** `PUT /api/workspaces/{workspaceId}`  
**URL:** `http://localhost:8080/api/workspaces/a1b2c3d4-e5f6-7890-abcd-ef1234567890`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <ALICE_TOKEN>
```

**Body (JSON):**
```json
{
  "name": "Product Development & Design Team"
}
```

**Expected Response (200 OK):**
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "name": "Product Development & Design Team",
  "ownerId": "123e4567-e89b-12d3-a456-426614174000",
  "myRole": "OWNER",
  "createdAt": "2026-01-28T12:30:00.000Z"
}
```

**Required Role:** ADMIN or OWNER

---

### 🔟 Delete Workspace

**Endpoint:** `DELETE /api/workspaces/{workspaceId}`  
**URL:** `http://localhost:8080/api/workspaces/a1b2c3d4-e5f6-7890-abcd-ef1234567890`

**Headers:**
```
Authorization: Bearer <ALICE_TOKEN>
```

**Expected Response (204 No Content):**
*(Empty body)*

**Required Role:** OWNER only

**⚠️ Warning:** This deletes all boards, tasks, documents, and comments in the workspace!

---

## Testing Board & Column APIs

### 1️⃣1️⃣ Create Board

**Endpoint:** `POST /api/workspaces/{workspaceId}/boards`  
**URL:** `http://localhost:8080/api/workspaces/a1b2c3d4-e5f6-7890-abcd-ef1234567890/boards`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <ALICE_TOKEN>
```

**Body (JSON):**
```json
{
  "name": "Sprint Planning Q1 2026",
  "position": 0
}
```

**Expected Response (200 OK):**
```json
{
  "id": "b1c2d3e4-f5a6-7b8c-9d0e-1f2a3b4c5d6e",
  "workspaceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "name": "Sprint Planning Q1 2026",
  "position": 0,
  "createdAt": "2026-01-28T12:35:00.000Z"
}
```

**📝 Save the board `id`!**

**Required Role:** MEMBER or higher

---

### 1️⃣2️⃣ List Workspace Boards

**Endpoint:** `GET /api/workspaces/{workspaceId}/boards`  
**URL:** `http://localhost:8080/api/workspaces/a1b2c3d4-e5f6-7890-abcd-ef1234567890/boards`

**Headers:**
```
Authorization: Bearer <ALICE_TOKEN>
```

**Expected Response (200 OK):**
```json
[
  {
    "id": "b1c2d3e4-f5a6-7b8c-9d0e-1f2a3b4c5d6e",
    "workspaceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "name": "Sprint Planning Q1 2026",
    "position": 0,
    "createdAt": "2026-01-28T12:35:00.000Z"
  }
]
```

**Required Role:** VIEWER or higher

---

### 1️⃣3️⃣ Create Board Columns

**Endpoint:** `POST /api/workspaces/{workspaceId}/boards/{boardId}/columns`  
**URL:** `http://localhost:8080/api/workspaces/a1b2c3d4-e5f6-7890-abcd-ef1234567890/boards/b1c2d3e4-f5a6-7b8c-9d0e-1f2a3b4c5d6e/columns`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <ALICE_TOKEN>
```

**Body for "To Do" Column:**
```json
{
  "name": "To Do",
  "position": 0
}
```

**Expected Response (200 OK):**
```json
{
  "id": "c1d2e3f4-a5b6-7c8d-9e0f-1a2b3c4d5e6f",
  "board": {
    "id": "b1c2d3e4-f5a6-7b8c-9d0e-1f2a3b4c5d6e",
    "name": "Sprint Planning Q1 2026"
  },
  "name": "To Do",
  "position": 0
}
```

**📝 Save column IDs! Create additional columns:**

**"In Progress" Column:**
```json
{
  "name": "In Progress",
  "position": 1
}
```

**"Done" Column:**
```json
{
  "name": "Done",
  "position": 2
}
```

**Required Role:** MEMBER or higher

---

### 1️⃣4️⃣ List Board Columns

**Endpoint:** `GET /api/workspaces/{workspaceId}/boards/{boardId}/columns`  
**URL:** `http://localhost:8080/api/workspaces/a1b2c3d4-e5f6-7890-abcd-ef1234567890/boards/b1c2d3e4-f5a6-7b8c-9d0e-1f2a3b4c5d6e/columns`

**Headers:**
```
Authorization: Bearer <ALICE_TOKEN>
```

**Expected Response (200 OK):**
```json
[
  {
    "id": "c1d2e3f4-a5b6-7c8d-9e0f-1a2b3c4d5e6f",
    "name": "To Do",
    "position": 0
  },
  {
    "id": "c2d3e4f5-b6a7-8c9d-0e1f-2a3b4c5d6e7f",
    "name": "In Progress",
    "position": 1
  },
  {
    "id": "c3d4e5f6-a7b8-9c0d-1e2f-3a4b5c6d7e8f",
    "name": "Done",
    "position": 2
  }
]
```

**Required Role:** VIEWER or higher

---

## Testing Task APIs

### 1️⃣5️⃣ Create Task in "To Do" Column

**Endpoint:** `POST /api/workspaces/{workspaceId}/columns/{columnId}/tasks`  
**URL:** `http://localhost:8080/api/workspaces/a1b2c3d4-e5f6-7890-abcd-ef1234567890/columns/c1d2e3f4-a5b6-7c8d-9e0f-1a2b3c4d5e6f/tasks`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <ALICE_TOKEN>
```

**Body (JSON):**
```json
{
  "title": "Design new landing page",
  "description": "Create wireframes and mockups for the new landing page",
  "priority": "HIGH",
  "dueDate": "2026-02-15"
}
```

**Expected Response (200 OK):**
```json
{
  "id": "t1a2b3c4-d5e6-7f8a-9b0c-1d2e3f4a5b6c",
  "columnId": "c1d2e3f4-a5b6-7c8d-9e0f-1a2b3c4d5e6f",
  "title": "Design new landing page",
  "description": "Create wireframes and mockups for the new landing page",
  "priority": "HIGH",
  "dueDate": "2026-02-15",
  "position": 0,
  "createdById": "123e4567-e89b-12d3-a456-426614174000",
  "createdAt": "2026-01-28T12:40:00.000Z",
  "updatedAt": "2026-01-28T12:40:00.000Z"
}
```

**📝 Priority Options:** `LOW`, `MEDIUM`, `HIGH`, `URGENT`

**Required Role:** MEMBER or higher

---

### 1️⃣6️⃣ List Tasks in a Column

**Endpoint:** `GET /api/workspaces/{workspaceId}/columns/{columnId}/tasks`  
**URL:** `http://localhost:8080/api/workspaces/a1b2c3d4-e5f6-7890-abcd-ef1234567890/columns/c1d2e3f4-a5b6-7c8d-9e0f-1a2b3c4d5e6f/tasks`

**Headers:**
```
Authorization: Bearer <ALICE_TOKEN>
```

**Expected Response (200 OK):**
```json
[
  {
    "id": "t1a2b3c4-d5e6-7f8a-9b0c-1d2e3f4a5b6c",
    "columnId": "c1d2e3f4-a5b6-7c8d-9e0f-1a2b3c4d5e6f",
    "title": "Design new landing page",
    "description": "Create wireframes and mockups for the new landing page",
    "priority": "HIGH",
    "dueDate": "2026-02-15",
    "position": 0,
    "createdById": "123e4567-e89b-12d3-a456-426614174000",
    "createdAt": "2026-01-28T12:40:00.000Z",
    "updatedAt": "2026-01-28T12:40:00.000Z"
  }
]
```

**Required Role:** VIEWER or higher

---

### 1️⃣7️⃣ Update Task

**Endpoint:** `PUT /api/workspaces/{workspaceId}/columns/{columnId}/tasks/{taskId}`  
**URL:** `http://localhost:8080/api/workspaces/a1b2c3d4-e5f6-7890-abcd-ef1234567890/columns/c1d2e3f4-a5b6-7c8d-9e0f-1a2b3c4d5e6f/tasks/t1a2b3c4-d5e6-7f8a-9b0c-1d2e3f4a5b6c`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <ALICE_TOKEN>
```

**Body (JSON):**
```json
{
  "title": "Design new landing page (UPDATED)",
  "description": "Create wireframes, mockups, and interactive prototype",
  "priority": "URGENT",
  "dueDate": "2026-02-10"
}
```

**Expected Response (200 OK):**
```json
{
  "id": "t1a2b3c4-d5e6-7f8a-9b0c-1d2e3f4a5b6c",
  "columnId": "c1d2e3f4-a5b6-7c8d-9e0f-1a2b3c4d5e6f",
  "title": "Design new landing page (UPDATED)",
  "description": "Create wireframes, mockups, and interactive prototype",
  "priority": "URGENT",
  "dueDate": "2026-02-10",
  "position": 0,
  "createdById": "123e4567-e89b-12d3-a456-426614174000",
  "createdAt": "2026-01-28T12:40:00.000Z",
  "updatedAt": "2026-01-28T12:45:00.000Z"
}
```

**Required Role:** MEMBER or higher

---

### 1️⃣8️⃣ Move Task to Different Column

**Endpoint:** `PUT /api/workspaces/{workspaceId}/columns/{columnId}/tasks/{taskId}/move`  
**URL:** `http://localhost:8080/api/workspaces/a1b2c3d4-e5f6-7890-abcd-ef1234567890/columns/c1d2e3f4-a5b6-7c8d-9e0f-1a2b3c4d5e6f/tasks/t1a2b3c4-d5e6-7f8a-9b0c-1d2e3f4a5b6c/move`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <ALICE_TOKEN>
```

**Body (JSON):**
```json
{
  "targetColumnId": "c2d3e4f5-b6a7-8c9d-0e1f-2a3b4c5d6e7f",
  "position": 0
}
```
*(Moves task to "In Progress" column)*

**Expected Response (200 OK):**
```json
{
  "id": "t1a2b3c4-d5e6-7f8a-9b0c-1d2e3f4a5b6c",
  "columnId": "c2d3e4f5-b6a7-8c9d-0e1f-2a3b4c5d6e7f",
  "title": "Design new landing page (UPDATED)",
  "description": "Create wireframes, mockups, and interactive prototype",
  "priority": "URGENT",
  "dueDate": "2026-02-10",
  "position": 0,
  "createdById": "123e4567-e89b-12d3-a456-426614174000",
  "createdAt": "2026-01-28T12:40:00.000Z",
  "updatedAt": "2026-01-28T12:50:00.000Z"
}
```

**Required Role:** MEMBER or higher

---

### 1️⃣9️⃣ Delete Task

**Endpoint:** `DELETE /api/workspaces/{workspaceId}/columns/{columnId}/tasks/{taskId}`  
**URL:** `http://localhost:8080/api/workspaces/a1b2c3d4-e5f6-7890-abcd-ef1234567890/columns/c1d2e3f4-a5b6-7c8d-9e0f-1a2b3c4d5e6f/tasks/t1a2b3c4-d5e6-7f8a-9b0c-1d2e3f4a5b6c`

**Headers:**
```
Authorization: Bearer <ALICE_TOKEN>
```

**Expected Response (204 No Content):**
*(Empty body)*

**Required Role:** MEMBER or higher

---

## Testing Document & Comment APIs

### 2️⃣0️⃣ Create Document

**Endpoint:** `POST /api/workspaces/{workspaceId}/documents`  
**URL:** `http://localhost:8080/api/workspaces/a1b2c3d4-e5f6-7890-abcd-ef1234567890/documents`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <ALICE_TOKEN>
```

**Body (JSON):**
```json
{
  "title": "Product Requirements Document",
  "content": "## Overview\n\nThis document outlines the requirements for the new feature...\n\n### Goals\n- Improve user experience\n- Increase conversion rate"
}
```

**Expected Response (200 OK):**
```json
{
  "id": "d1e2f3a4-b5c6-7d8e-9f0a-1b2c3d4e5f6a",
  "workspaceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "title": "Product Requirements Document",
  "content": "## Overview\n\nThis document outlines the requirements for the new feature...\n\n### Goals\n- Improve user experience\n- Increase conversion rate",
  "createdById": "123e4567-e89b-12d3-a456-426614174000",
  "createdAt": "2026-01-28T13:00:00.000Z",
  "updatedAt": "2026-01-28T13:00:00.000Z"
}
```

**📝 Save the document `id`!**

**Required Role:** MEMBER or higher

---

### 2️⃣1️⃣ List Workspace Documents

**Endpoint:** `GET /api/workspaces/{workspaceId}/documents`  
**URL:** `http://localhost:8080/api/workspaces/a1b2c3d4-e5f6-7890-abcd-ef1234567890/documents`

**Headers:**
```
Authorization: Bearer <ALICE_TOKEN>
```

**Expected Response (200 OK):**
```json
[
  {
    "id": "d1e2f3a4-b5c6-7d8e-9f0a-1b2c3d4e5f6a",
    "workspaceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "title": "Product Requirements Document",
    "content": "## Overview\n\nThis document outlines the requirements for the new feature...",
    "createdById": "123e4567-e89b-12d3-a456-426614174000",
    "createdAt": "2026-01-28T13:00:00.000Z",
    "updatedAt": "2026-01-28T13:00:00.000Z"
  }
]
```

**Required Role:** VIEWER or higher

---

### 2️⃣2️⃣ Update Document

**Endpoint:** `PUT /api/workspaces/{workspaceId}/documents/{documentId}`  
**URL:** `http://localhost:8080/api/workspaces/a1b2c3d4-e5f6-7890-abcd-ef1234567890/documents/d1e2f3a4-b5c6-7d8e-9f0a-1b2c3d4e5f6a`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <ALICE_TOKEN>
```

**Body (JSON):**
```json
{
  "title": "Product Requirements Document v2.0",
  "content": "## Overview (Updated)\n\nThis document outlines the requirements...\n\n### Goals\n- Improve UX\n- Increase conversion\n- Reduce bounce rate"
}
```

**Expected Response (200 OK):**
```json
{
  "id": "d1e2f3a4-b5c6-7d8e-9f0a-1b2c3d4e5f6a",
  "workspaceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "title": "Product Requirements Document v2.0",
  "content": "## Overview (Updated)\n\nThis document outlines the requirements...",
  "createdById": "123e4567-e89b-12d3-a456-426614174000",
  "createdAt": "2026-01-28T13:00:00.000Z",
  "updatedAt": "2026-01-28T13:10:00.000Z"
}
```

**Required Role:** MEMBER or higher

---

### 2️⃣3️⃣ Add Comment to Document

**Endpoint:** `POST /api/workspaces/{workspaceId}/documents/{documentId}/comments`  
**URL:** `http://localhost:8080/api/workspaces/a1b2c3d4-e5f6-7890-abcd-ef1234567890/documents/d1e2f3a4-b5c6-7d8e-9f0a-1b2c3d4e5f6a/comments`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <BOB_TOKEN>
```

**Body (JSON):**
```json
{
  "content": "Great document! I suggest we add a section about accessibility requirements."
}
```

**Expected Response (200 OK):**
```json
{
  "id": "co1a2b3c-4d5e-6f7a-8b9c-0d1e2f3a4b5c",
  "entityId": "d1e2f3a4-b5c6-7d8e-9f0a-1b2c3d4e5f6a",
  "entityType": "document",
  "content": "Great document! I suggest we add a section about accessibility requirements.",
  "createdById": "223e4567-e89b-12d3-a456-426614174001",
  "createdByName": "bob",
  "createdAt": "2026-01-28T13:15:00.000Z"
}
```

**Required Role:** MEMBER or higher

---

### 2️⃣4️⃣ List Document Comments

**Endpoint:** `GET /api/workspaces/{workspaceId}/documents/{documentId}/comments`  
**URL:** `http://localhost:8080/api/workspaces/a1b2c3d4-e5f6-7890-abcd-ef1234567890/documents/d1e2f3a4-b5c6-7d8e-9f0a-1b2c3d4e5f6a/comments`

**Headers:**
```
Authorization: Bearer <ALICE_TOKEN>
```

**Expected Response (200 OK):**
```json
[
  {
    "id": "co1a2b3c-4d5e-6f7a-8b9c-0d1e2f3a4b5c",
    "entityId": "d1e2f3a4-b5c6-7d8e-9f0a-1b2c3d4e5f6a",
    "entityType": "document",
    "content": "Great document! I suggest we add a section about accessibility requirements.",
    "createdById": "223e4567-e89b-12d3-a456-426614174001",
    "createdByName": "bob",
    "createdAt": "2026-01-28T13:15:00.000Z"
  }
]
```

**Required Role:** VIEWER or higher

---

### 2️⃣5️⃣ Delete Comment

**Endpoint:** `DELETE /api/workspaces/{workspaceId}/documents/{documentId}/comments/{commentId}`  
**URL:** `http://localhost:8080/api/workspaces/a1b2c3d4-e5f6-7890-abcd-ef1234567890/documents/d1e2f3a4-b5c6-7d8e-9f0a-1b2c3d4e5f6a/comments/co1a2b3c-4d5e-6f7a-8b9c-0d1e2f3a4b5c`

**Headers:**
```
Authorization: Bearer <BOB_TOKEN>
```

**Expected Response (204 No Content):**
*(Empty body)*

**Required Role:** MEMBER or higher

---

## Common Errors & Troubleshooting

### Error 401 Unauthorized
```json
{
  "timestamp": "2026-01-28T13:20:00.000Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource"
}
```

**Causes:**
- Missing `Authorization` header
- Invalid or expired token
- Token format incorrect (should be `Bearer <token>`)

**Solution:**
- Ensure token is included: `Authorization: Bearer eyJhbGci...`
- Re-login to get a fresh token
- Check token hasn't expired (3 hours default)

---

### Error 403 Forbidden
```json
{
  "timestamp": "2026-01-28T13:20:00.000Z",
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied"
}
```

**Causes:**
- User doesn't have required workspace role
- User is not a member of the workspace
- Trying to perform OWNER action as MEMBER

**Solution:**
- Check `myRole` in workspace response
- Ensure user is invited to the workspace
- Use correct token for the required permission level

---

### Error 404 Not Found
```json
{
  "timestamp": "2026-01-28T13:20:00.000Z",
  "status": 404,
  "error": "Not Found"
}
```

**Causes:**
- Invalid workspace/board/task/document ID
- Resource deleted
- Wrong endpoint URL

**Solution:**
- Verify the ID is correct
- Check if resource still exists
- Verify endpoint path

---

### Error 400 Bad Request
```json
{
  "timestamp": "2026-01-28T13:20:00.000Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed"
}
```

**Causes:**
- Missing required fields (`@NotBlank`, `@NotNull`)
- Invalid email format
- Invalid enum value (priority, role)

**Solution:**
- Check request body matches expected format
- Ensure all required fields are provided
- Validate enum values (e.g., `MEMBER`, not `member`)

---

### Error 500 Internal Server Error
```json
{
  "timestamp": "2026-01-28T13:20:00.000Z",
  "status": 500,
  "error": "Internal Server Error",
  "message": "RuntimeException: User not found"
}
```

**Causes:**
- Database connection issue
- Missing Flyway migrations
- Data constraint violation

**Solution:**
- Check application logs
- Verify database is running
- Run Flyway migrations
- Check for null values or constraint violations

---

## Postman Collection Setup

### Create a New Collection

1. **Open Postman**
2. Click **"New"** → **"Collection"**
3. Name it: `CollabNest API`

### Set Collection Variables

1. Right-click collection → **"Edit"**
2. Go to **"Variables"** tab
3. Add these variables:

| Variable | Initial Value | Current Value |
|----------|---------------|---------------|
| `baseUrl` | `http://localhost:8080` | `http://localhost:8080` |
| `aliceToken` | *(empty)* | *(paste after login)* |
| `bobToken` | *(empty)* | *(paste after login)* |
| `workspaceId` | *(empty)* | *(paste after creation)* |
| `boardId` | *(empty)* | *(paste after creation)* |
| `columnId` | *(empty)* | *(paste after creation)* |
| `taskId` | *(empty)* | *(paste after creation)* |
| `documentId` | *(empty)* | *(paste after creation)* |

### Use Variables in Requests

**URL Example:**
```
{{baseUrl}}/api/workspaces/{{workspaceId}}/boards
```

**Authorization Header:**
```
Bearer {{aliceToken}}
```

### Auto-Save Tokens with Test Scripts

Add this to the **Tests** tab of your register/login requests:

```javascript
// Save token to collection variable
const response = pm.response.json();
pm.collectionVariables.set("aliceToken", response.token);
```

For workspace creation:
```javascript
const response = pm.response.json();
pm.collectionVariables.set("workspaceId", response.id);
```

---

## Testing Workflow Summary

### 🔄 Complete Test Flow

1. ✅ **Register Alice** → Save token as `aliceToken`
2. ✅ **Register Bob** → Save token as `bobToken`
3. ✅ **Create Workspace** (Alice) → Save `workspaceId`
4. ✅ **Invite Bob** to workspace (Alice)
5. ✅ **Create Board** (Alice) → Save `boardId`
6. ✅ **Create 3 Columns** (Alice) → Save `columnId` for "To Do"
7. ✅ **Create Task** in "To Do" (Alice) → Save `taskId`
8. ✅ **Move Task** to "In Progress" (Bob)
9. ✅ **Create Document** (Alice) → Save `documentId`
10. ✅ **Add Comment** to document (Bob)
11. ✅ **Verify Permissions** - Try Bob deleting workspace (should fail 403)

### 🎯 Success Criteria

- ✅ All authenticated requests return 200/204
- ✅ Tokens work across different endpoints
- ✅ Role-based access control enforced
- ✅ Data persists between requests
- ✅ Relations maintained (workspace → board → column → task)
- ✅ Comments link to correct documents
- ✅ Unauthorized access blocked with 403

---

## 🚀 Next Steps After Testing

1. **Create Flyway Migrations** - Generate migration scripts for all tables
2. **Add Pagination** - Implement paging for list endpoints
3. **Add Search/Filter** - Enable filtering by status, priority, etc.
4. **WebSocket Integration** - Real-time updates for collaborative editing
5. **Activity Logging** - Track all user actions
6. **Frontend Development** - Build React UI

---

**Happy Testing! 🎉**

For issues, check application logs:
```bash
tail -f logs/spring.log
```
