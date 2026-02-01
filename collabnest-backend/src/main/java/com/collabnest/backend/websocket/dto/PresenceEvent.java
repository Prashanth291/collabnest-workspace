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
public class PresenceEvent {
    
    public enum EventType {
        USER_ONLINE,
        USER_OFFLINE,
        USER_JOINED_WORKSPACE,
        USER_LEFT_WORKSPACE,
        USER_JOINED_BOARD,
        USER_LEFT_BOARD,
        USER_JOINED_DOCUMENT,
        USER_LEFT_DOCUMENT
    }

    private EventType type;
    private UUID userId;
    private String userName;
    private UUID workspaceId;
    private UUID boardId;
    private UUID documentId;
    private LocalDateTime timestamp;
}
