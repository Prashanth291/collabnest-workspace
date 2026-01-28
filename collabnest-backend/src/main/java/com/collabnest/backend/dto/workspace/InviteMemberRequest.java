package com.collabnest.backend.dto.workspace;

import com.collabnest.backend.domain.enums.WorkspaceRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InviteMemberRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,
        
        @NotNull(message = "Role is required")
        WorkspaceRole role
) {}
