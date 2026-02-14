package com.collabnest.backend.dto.task;

import com.collabnest.backend.domain.entity.Task;
import com.collabnest.backend.domain.enums.TaskPriority;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TaskResponse(
                UUID id,
                UUID columnId,
                String title,
                String description,
                TaskPriority priority,
                LocalDate dueDate,
                Integer position,
                UUID assigneeId,
                String assigneeName,
                UUID createdById,
                Instant createdAt,
                Instant updatedAt) {
        public static TaskResponse fromEntity(Task task) {
                return new TaskResponse(
                                task.getId(),
                                task.getColumn().getId(),
                                task.getTitle(),
                                task.getDescription(),
                                task.getPriority(),
                                task.getDueDate(),
                                task.getPosition(),
                                task.getAssignee() != null ? task.getAssignee().getId() : null,
                                task.getAssignee() != null ? task.getAssignee().getUsername() : null,
                                task.getCreatedBy().getId(),
                                task.getCreatedAt(),
                                task.getUpdatedAt());
        }
}
