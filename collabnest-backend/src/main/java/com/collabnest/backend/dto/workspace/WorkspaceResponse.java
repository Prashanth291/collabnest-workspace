package com.collabnest.backend.dto.workspace;

import com.collabnest.backend.domain.enums.WorkspaceRole;

import java.time.Instant;
import java.util.UUID;

public record WorkspaceResponse(
        UUID id,
        String name,
        UUID ownerId,
        WorkspaceRole myRole,
        Instant createdAt
) {}
