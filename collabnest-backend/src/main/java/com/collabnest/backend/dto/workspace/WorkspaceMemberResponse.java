package com.collabnest.backend.dto.workspace;

import com.collabnest.backend.domain.enums.WorkspaceRole;

import java.time.Instant;
import java.util.UUID;

public record WorkspaceMemberResponse(
        UUID id,
        UUID userId,
        String username,
        String name,
        String email,
        WorkspaceRole role,
        Boolean isPrimaryOwner,
        Instant joinedAt) {
}
