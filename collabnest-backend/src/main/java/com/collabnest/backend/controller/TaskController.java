package com.collabnest.backend.controller;

import com.collabnest.backend.domain.entity.BoardColumn;
import com.collabnest.backend.domain.entity.Task;
import com.collabnest.backend.dto.task.CreateTaskRequest;
import com.collabnest.backend.dto.task.MoveTaskRequest;
import com.collabnest.backend.dto.task.TaskResponse;
import com.collabnest.backend.repository.BoardColumnRepository;
import com.collabnest.backend.security.UserPrincipal;
import com.collabnest.backend.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Task controller with workspace-aware authorization.
 */
@RestController
@RequestMapping("/api/workspaces/{workspaceId}/columns/{columnId}/tasks")
public class TaskController {

    private final TaskService taskService;
    private final BoardColumnRepository boardColumnRepository;

    public TaskController(TaskService taskService, BoardColumnRepository boardColumnRepository) {
        this.taskService = taskService;
        this.boardColumnRepository = boardColumnRepository;
    }

    /**
     * Create a new task in a column - requires MEMBER role.
     */
    @PostMapping
    @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'MEMBER')")
    public ResponseEntity<TaskResponse> createTask(
            @PathVariable UUID workspaceId,
            @PathVariable UUID columnId,
            @Valid @RequestBody CreateTaskRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        // Verify column belongs to this workspace
        BoardColumn column = boardColumnRepository.findById(columnId)
                .orElseThrow(() -> new RuntimeException("Column not found"));
        
        if (!column.getBoard().getWorkspace().getId().equals(workspaceId)) {
            return ResponseEntity.badRequest().build();
        }
        
        UUID userId = userPrincipal.getUserId();
        
        Task task = taskService.createTask(
                columnId,
                request.title(),
                request.description(),
                request.priority(),
                request.dueDate(),
                userId
        );
        
        TaskResponse response = new TaskResponse(
                task.getId(),
                task.getColumn().getId(),
                task.getTitle(),
                task.getDescription(),
                task.getPriority(),
                task.getDueDate(),
                task.getPosition(),
                task.getCreatedBy().getId(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get all tasks in a column - requires VIEWER role.
     */
    @GetMapping
    @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'VIEWER')")
    public ResponseEntity<List<TaskResponse>> getColumnTasks(
            @PathVariable UUID workspaceId,
            @PathVariable UUID columnId) {
        
        List<Task> tasks = taskService.getColumnTasks(columnId);
        
        List<TaskResponse> responses = tasks.stream()
                .map(task -> new TaskResponse(
                        task.getId(),
                        task.getColumn().getId(),
                        task.getTitle(),
                        task.getDescription(),
                        task.getPriority(),
                        task.getDueDate(),
                        task.getPosition(),
                        task.getCreatedBy().getId(),
                        task.getCreatedAt(),
                        task.getUpdatedAt()
                ))
                .toList();
        
        return ResponseEntity.ok(responses);
    }

    /**
     * Get a specific task - requires VIEWER role.
     */
    @GetMapping("/{taskId}")
    @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'VIEWER')")
    public ResponseEntity<TaskResponse> getTask(
            @PathVariable UUID workspaceId,
            @PathVariable UUID columnId,
            @PathVariable UUID taskId) {
        
        Task task = taskService.getTask(taskId);
        
        // Verify the task belongs to this workspace and column
        if (!task.getColumn().getId().equals(columnId)) {
            return ResponseEntity.notFound().build();
        }
        
        TaskResponse response = new TaskResponse(
                task.getId(),
                task.getColumn().getId(),
                task.getTitle(),
                task.getDescription(),
                task.getPriority(),
                task.getDueDate(),
                task.getPosition(),
                task.getCreatedBy().getId(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Update a task - requires MEMBER role.
     */
    @PutMapping("/{taskId}")
    @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'MEMBER')")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable UUID workspaceId,
            @PathVariable UUID columnId,
            @PathVariable UUID taskId,
            @Valid @RequestBody CreateTaskRequest request) {
        
        Task task = taskService.updateTask(
                taskId,
                request.title(),
                request.description(),
                request.priority(),
                request.dueDate()
        );
        
        TaskResponse response = new TaskResponse(
                task.getId(),
                task.getColumn().getId(),
                task.getTitle(),
                task.getDescription(),
                task.getPriority(),
                task.getDueDate(),
                task.getPosition(),
                task.getCreatedBy().getId(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Move a task to a different column/position - requires MEMBER role.
     */
    @PutMapping("/{taskId}/move")
    @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'MEMBER')")
    public ResponseEntity<TaskResponse> moveTask(
            @PathVariable UUID workspaceId,
            @PathVariable UUID columnId,
            @PathVariable UUID taskId,
            @Valid @RequestBody MoveTaskRequest request) {
        
        Task task = taskService.moveTask(taskId, request.targetColumnId(), request.position());
        
        TaskResponse response = new TaskResponse(
                task.getId(),
                task.getColumn().getId(),
                task.getTitle(),
                task.getDescription(),
                task.getPriority(),
                task.getDueDate(),
                task.getPosition(),
                task.getCreatedBy().getId(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a task - requires MEMBER role.
     */
    @DeleteMapping("/{taskId}")
    @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'MEMBER')")
    public ResponseEntity<Void> deleteTask(
            @PathVariable UUID workspaceId,
            @PathVariable UUID columnId,
            @PathVariable UUID taskId) {
        
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }
}
