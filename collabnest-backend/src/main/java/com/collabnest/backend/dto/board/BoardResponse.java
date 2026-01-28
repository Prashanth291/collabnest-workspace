package com.collabnest.backend.dto.board;

import java.time.Instant;
import java.util.UUID;

public record BoardResponse(
        UUID id,
        UUID workspaceId,
        String name,
        Integer position,
        Instant createdAt
) {}
