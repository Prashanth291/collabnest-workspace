package com.collabnest.backend.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentEvent {
    
    public enum EventType {
        DOCUMENT_CREATED,
        DOCUMENT_UPDATED,
        DOCUMENT_DELETED,
        COMMENT_ADDED,
        COMMENT_UPDATED,
        COMMENT_DELETED
    }

    private EventType type;
    private UUID documentId;
    private UUID workspaceId;
    private UUID userId;
    private String userName;
    private Object payload;
    private LocalDateTime timestamp;
}
