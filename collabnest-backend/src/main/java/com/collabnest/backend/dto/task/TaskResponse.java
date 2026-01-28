package com.collabnest.backend.dto.task;

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
        UUID createdById,
        Instant createdAt,
        Instant updatedAt
) {}
