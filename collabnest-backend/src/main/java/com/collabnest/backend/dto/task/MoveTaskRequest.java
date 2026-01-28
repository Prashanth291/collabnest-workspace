package com.collabnest.backend.dto.task;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record MoveTaskRequest(
        @NotNull(message = "Target column ID is required")
        UUID targetColumnId,
        
        @NotNull(message = "Position is required")
        Integer position
) {}
