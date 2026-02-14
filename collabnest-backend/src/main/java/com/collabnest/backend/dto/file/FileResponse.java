package com.collabnest.backend.dto.file;

import java.time.Instant;
import java.util.UUID;

public record FileResponse(
        UUID id,
        UUID workspaceId,
        UUID uploadedById,
        String uploadedByUsername,
        UUID taskId,
        UUID documentId,
        String fileName,
        String fileUrl,
        Long size,
        Instant createdAt) {
}
