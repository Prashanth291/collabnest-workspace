package com.collabnest.backend.dto.board;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateBoardRequest(
        @NotBlank(message = "Board name is required")
        String name,
        
        @NotNull(message = "Position is required")
        Integer position
) {}
