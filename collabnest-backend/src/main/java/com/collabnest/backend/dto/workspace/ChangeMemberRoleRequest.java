package com.collabnest.backend.dto.workspace;

import com.collabnest.backend.domain.enums.WorkspaceRole;
import jakarta.validation.constraints.NotNull;

public record ChangeMemberRoleRequest(
        @NotNull(message = "Role is required") WorkspaceRole role) {
}
