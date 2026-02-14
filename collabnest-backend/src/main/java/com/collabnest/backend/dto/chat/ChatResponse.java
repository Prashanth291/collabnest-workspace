package com.collabnest.backend.dto.chat;

import com.collabnest.backend.domain.enums.ChatType;

import java.time.Instant;
import java.util.UUID;

public record ChatResponse(
        UUID id,
        ChatType chatType,
        UUID workspaceId,
        UUID otherUserId,
        String otherUsername,
        Instant createdAt) {
}
