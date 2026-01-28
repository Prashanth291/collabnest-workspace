package com.collabnest.backend.dto.workspace;

import jakarta.validation.constraints.NotBlank;

public record JoinWorkspaceRequest(
        @NotBlank(message = "Invite token is required")
        String inviteToken
) {}
