package com.collabnest.backend.dto.comment;

import java.time.Instant;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        UUID entityId,  // documentId or taskId
        String entityType,  // "document" or "task"
        String content,
        UUID createdById,
        String createdByName,
        Instant createdAt
) {}
