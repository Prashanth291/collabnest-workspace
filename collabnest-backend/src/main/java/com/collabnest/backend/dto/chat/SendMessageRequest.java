package com.collabnest.backend.dto.chat;

import com.collabnest.backend.domain.enums.LinkedEntityType;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record SendMessageRequest(
        @NotBlank(message = "Content is required") String content,

        LinkedEntityType linkedEntityType,

        UUID linkedEntityId) {
}
