package com.collabnest.backend.dto.task;

import com.collabnest.backend.domain.enums.TaskPriority;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record CreateTaskRequest(
        @NotBlank(message = "Task title is required")
        String title,
        
        String description,
        TaskPriority priority,
        LocalDate dueDate
) {}
