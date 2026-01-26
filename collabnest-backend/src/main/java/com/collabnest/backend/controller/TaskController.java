package com.collabnest.backend.controller;

import com.collabnest.backend.domain.entity.BoardColumn;
import com.collabnest.backend.domain.entity.Task;
import com.collabnest.backend.domain.enums.TaskPriority;
import com.collabnest.backend.repository.BoardColumnRepository;
import com.collabnest.backend.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Task controller with workspace-aware authorization.
 * 
 * Authorization strategy:
 * - Tasks belong to board columns, which belong to boards, which belong to workspaces
 * - Access is controlled by workspace membership
 * - We extract the workspaceId from the column/board hierarchy for permission checks
 */
@RestController
@RequestMapping("/api/workspaces/{workspaceId}/tasks")
public class TaskController {

    private final TaskService taskService;
    private final BoardColumnRepository boardColumnRepository;

    public TaskController(TaskService taskService, BoardColumnRepository boardColumnRepository) {
        this.taskService = taskService;
        this.boardColumnRepository = boardColumnRepository;
    }

    /**
     * Create a new task in a board column.
     * Requires at least MEMBER role in the workspace.
     */
    @PostMapping
    @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'MEMBER')")
    public ResponseEntity<Task> createTask(
            @PathVariable UUID workspaceId,
            @RequestBody CreateTaskRequest request) {
        
        // Verify the column belongs to a board in this workspace
        BoardColumn column = boardColumnRepository.findById(request.getColumnId())
                .orElseThrow(() -> new RuntimeException("Column not found"));
        
        if (!column.getBoard().getWorkspace().getId().equals(workspaceId)) {
            return ResponseEntity.badRequest().build();
        }
        
        Task task = taskService.createTask(
                request.getColumnId(),
                request.getTitle(),
                request.getDescription(),
                request.getPriority(),
                request.getDueDate(),
                request.getAssigneeId()
        );
        return ResponseEntity.ok(task);
    }

    /**
     * Get all tasks in the workspace.
     * Requires at least VIEWER role.
     */
    @GetMapping
    @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'VIEWER')")
    public ResponseEntity<List<Task>> getTasks(@PathVariable UUID workspaceId) {
        List<Task> tasks = taskService.getTasksByWorkspace(workspaceId);
        return ResponseEntity.ok(tasks);
    }

    /**
     * Get a specific task.
     * Requires at least VIEWER role.
     */
    @GetMapping("/{taskId}")
    @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'VIEWER')")
    public ResponseEntity<Task> getTask(
            @PathVariable UUID workspaceId,
            @PathVariable UUID taskId) {
        
        Task task = taskService.getTask(taskId);
        
        // Verify the task belongs to this workspace
        UUID taskWorkspaceId = task.getColumn().getBoard().getWorkspace().getId();
        if (!taskWorkspaceId.equals(workspaceId)) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(task);
    }

    /**
     * Update a task.
     * Requires at least MEMBER role.
     */
    @PutMapping("/{taskId}")
    @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'MEMBER')")
    public ResponseEntity<Task> updateTask(
            @PathVariable UUID workspaceId,
            @PathVariable UUID taskId,
            @RequestBody UpdateTaskRequest request) {
        
        Task task = taskService.getTask(taskId);
        
        // Verify the task belongs to this workspace
        UUID taskWorkspaceId = task.getColumn().getBoard().getWorkspace().getId();
        if (!taskWorkspaceId.equals(workspaceId)) {
            return ResponseEntity.notFound().build();
        }
        
        Task updatedTask = taskService.updateTask(
                taskId,
                request.getTitle(),
                request.getDescription(),
                request.getPriority(),
                request.getDueDate()
        );
        return ResponseEntity.ok(updatedTask);
    }

    /**
     * Delete a task.
     * Requires at least MEMBER role (users can delete tasks they can edit).
     */
    @DeleteMapping("/{taskId}")
    @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'MEMBER')")
    public ResponseEntity<Void> deleteTask(
            @PathVariable UUID workspaceId,
            @PathVariable UUID taskId) {
        
        Task task = taskService.getTask(taskId);
        
        // Verify the task belongs to this workspace
        UUID taskWorkspaceId = task.getColumn().getBoard().getWorkspace().getId();
        if (!taskWorkspaceId.equals(workspaceId)) {
            return ResponseEntity.notFound().build();
        }
        
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Assign a task to a user.
     * Requires at least MEMBER role.
     */
    @PutMapping("/{taskId}/assign/{userId}")
    @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'MEMBER')")
    public ResponseEntity<Task> assignTask(
            @PathVariable UUID workspaceId,
            @PathVariable UUID taskId,
            @PathVariable UUID userId) {
        
        Task task = taskService.getTask(taskId);
        
        // Verify the task belongs to this workspace
        UUID taskWorkspaceId = task.getColumn().getBoard().getWorkspace().getId();
        if (!taskWorkspaceId.equals(workspaceId)) {
            return ResponseEntity.notFound().build();
        }
        
        Task updatedTask = taskService.assignTask(taskId, userId);
        return ResponseEntity.ok(updatedTask);
    }

    // DTOs
    public static class CreateTaskRequest {
        private UUID columnId;
        private String title;
        private String description;
        private TaskPriority priority;
        private LocalDate dueDate;
        private UUID assigneeId;

        public UUID getColumnId() {
            return columnId;
        }

        public void setColumnId(UUID columnId) {
            this.columnId = columnId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public TaskPriority getPriority() {
            return priority;
        }

        public void setPriority(TaskPriority priority) {
            this.priority = priority;
        }

        public LocalDate getDueDate() {
            return dueDate;
        }

        public void setDueDate(LocalDate dueDate) {
            this.dueDate = dueDate;
        }

        public UUID getAssigneeId() {
            return assigneeId;
        }

        public void setAssigneeId(UUID assigneeId) {
            this.assigneeId = assigneeId;
        }
    }

    public static class UpdateTaskRequest {
        private String title;
        private String description;
        private TaskPriority priority;
        private LocalDate dueDate;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public TaskPriority getPriority() {
            return priority;
        }

        public void setPriority(TaskPriority priority) {
            this.priority = priority;
        }

        public LocalDate getDueDate() {
            return dueDate;
        }

        public void setDueDate(LocalDate dueDate) {
            this.dueDate = dueDate;
        }
    }
}
