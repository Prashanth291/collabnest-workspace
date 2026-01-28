package com.collabnest.backend.dto.workspace;

import jakarta.validation.constraints.NotBlank;

public record CreateWorkspaceRequest(
        @NotBlank(message = "Workspace name is required")
        String name
) {}
