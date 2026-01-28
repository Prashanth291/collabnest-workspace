package com.collabnest.backend.dto.board;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateColumnRequest(
        @NotBlank(message = "Column name is required")
        String name,
        
        @NotNull(message = "Position is required")
        Integer position
) {}
