package com.collabnest.backend.dto.comment;

import jakarta.validation.constraints.NotBlank;

public record CreateCommentRequest(
        @NotBlank(message = "Comment content is required")
        String content
) {}
