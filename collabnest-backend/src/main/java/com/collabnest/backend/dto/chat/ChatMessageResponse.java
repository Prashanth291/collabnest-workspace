package com.collabnest.backend.dto.chat;

import com.collabnest.backend.domain.enums.LinkedEntityType;

import java.time.Instant;
import java.util.UUID;

public record ChatMessageResponse(
        UUID id,
        UUID chatId,
        UUID senderId,
        String senderUsername,
        String content,
        LinkedEntityType linkedEntityType,
        UUID linkedEntityId,
        Instant createdAt) {
}
