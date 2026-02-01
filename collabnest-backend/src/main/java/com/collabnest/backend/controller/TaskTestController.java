package com.collabnest.backend.controller;

import com.collabnest.backend.domain.entity.Task;
import com.collabnest.backend.domain.enums.TaskPriority;
import com.collabnest.backend.dto.task.TaskResponse;
import com.collabnest.backend.repository.BoardColumnRepository;
import com.collabnest.backend.repository.TaskRepository;
import com.collabnest.backend.repository.WorkspaceRepository;
import com.collabnest.backend.security.UserPrincipal;
import com.collabnest.backend.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Simplified Task Controller for testing - bypasses workspace/column hierarchy
 */
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskTestController {

    private final TaskService taskService;
    private final TaskRepository taskRepository;
    private final BoardColumnRepository boardColumnRepository;
    private final WorkspaceRepository workspaceRepository;

    /**
     * Simplified task creation for testing - finds first column in workspace
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody CreateTaskTestRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        // Find first column in the workspace
        var columns = boardColumnRepository.findAll().stream()
                .filter(col -> col.getBoard().getWorkspace().getId().equals(request.workspaceId()))
                .toList();

        if (columns.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        UUID columnId = columns.get(0).getId();
        UUID userId = userPrincipal.getUserId();

        Task task = taskService.createTask(
                columnId,
                request.title(),
                request.description(),
                request.priority(),
                null, // dueDate
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

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all tasks (for testing)
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TaskResponse>> getAllTasks() {
        List<Task> tasks = taskRepository.findAll();

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
     * Get specific task
     */
    @GetMapping("/{taskId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TaskResponse> getTask(@PathVariable UUID taskId) {
        Task task = taskService.getTask(taskId);

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

    // DTO for simplified task creation
    public record CreateTaskTestRequest(
            UUID workspaceId,
            String title,
            String description,
            String status, // Ignored - kept for compatibility
            TaskPriority priority
    ) {}
}
