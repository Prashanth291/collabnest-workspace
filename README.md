# CollabNest

**CollabNest** is a real-time collaborative workspace and project management platform built for teams. It brings together task management, Kanban boards, real-time messaging, document collaboration, and file sharing — all within a unified, role-based workspace environment.

> This project is currently under active development.

---

## Overview

CollabNest enables teams to create shared workspaces where they can organize work visually using Kanban boards, communicate instantly through real-time chat, manage documents and files, and track all activity — with fine-grained role-based permissions ensuring the right people have the right access.

---

## Features

### Workspaces

- Create and manage multiple workspaces for different teams or projects
- Invite members via email with unique invite tokens
- Role-based hierarchy — **Owner**, **Admin**, **Member**, and **Viewer** — with granular permission control
- Activity feed to track everything happening within a workspace

### Kanban Boards

- Create multiple boards per workspace to organize work visually
- Fully customizable columns (e.g., To Do, In Progress, Review, Done)
- Drag-and-drop support for reordering boards and columns

### Task Management

- Create tasks with title, description, priority levels (**Low**, **Medium**, **High**, **Critical**), and due dates
- Assign tasks to one or more team members
- Move tasks across columns and positions
- Define task dependencies to manage workflows and blockers
- Link tasks to documents for quick reference
- Comment on tasks for contextual discussions
- Optimistic concurrency control to prevent conflicting edits

### Real-Time Chat

- **Workspace chat** — group messaging for the entire team
- **Direct messages** — private 1-on-1 conversations between members
- Link messages to specific tasks or documents for contextual discussions
- Paginated message history
- Powered by WebSocket with STOMP protocol and SockJS fallback

### Documents & Comments

- Create and collaborate on documents within workspaces
- Comment on documents for feedback and discussions
- Link documents to tasks for traceability

### File Management

- Upload and manage files within workspaces
- Associate files with specific tasks or documents
- Organized file listing per workspace

### Notifications

- Real-time notification system with read/unread tracking
- Mark individual or all notifications as read
- Unread count for quick visibility
- Filter notifications by read status

### Activity Logs

- Comprehensive activity tracking across all workspace actions
- Filter activities by user or view the full workspace feed
- Personal activity dashboard

### Authentication & Security

- JWT-based stateless authentication
- OAuth2 social login with **Google** and **GitHub**
- Role-based access control at both global and workspace levels
- Secure WebSocket connections with JWT authentication

### Admin Dashboard

- Manage all users — view, update roles, enable/disable, or remove
- Admin summary and oversight tools
- Admin registration restricted to existing administrators

### User Profiles

- View and update personal profile information
- Personal dashboard with workspace and activity overview

---

## Tech Stack

| Layer              | Technology                                    |
| ------------------ | --------------------------------------------- |
| **Backend**        | Spring Boot, Java 21                          |
| **Database**       | PostgreSQL                                    |
| **Authentication** | Spring Security, JWT, OAuth2 (Google, GitHub) |
| **Real-Time**      | WebSocket, STOMP, SockJS                      |
| **ORM**            | Spring Data JPA, Hibernate                    |
| **Migrations**     | Flyway                                        |
| **Build**          | Maven                                         |

---

## License

This project is licensed under the [MIT License](LICENSE).
