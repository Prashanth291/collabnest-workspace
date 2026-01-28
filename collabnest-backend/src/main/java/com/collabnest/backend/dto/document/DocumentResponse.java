package com.collabnest.backend.dto.document;

import java.time.Instant;
import java.util.UUID;

public record DocumentResponse(
        UUID id,
        UUID workspaceId,
        String title,
        String content,
        UUID createdById,
        Instant createdAt,
        Instant updatedAt
) {}
