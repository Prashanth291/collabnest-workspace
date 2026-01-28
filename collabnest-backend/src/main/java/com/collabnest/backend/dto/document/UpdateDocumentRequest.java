package com.collabnest.backend.dto.document;

public record UpdateDocumentRequest(
        String title,
        String content
) {}
