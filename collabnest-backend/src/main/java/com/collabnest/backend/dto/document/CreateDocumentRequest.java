package com.collabnest.backend.dto.document;

import jakarta.validation.constraints.NotBlank;

public record CreateDocumentRequest(
        @NotBlank(message = "Document title is required")
        String title,
        
        String content
) {}
