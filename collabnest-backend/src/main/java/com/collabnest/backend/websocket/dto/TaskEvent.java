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
public class TaskEvent {
    
    public enum EventType {
        TASK_CREATED,
        TASK_UPDATED,
        TASK_MOVED,
        TASK_DELETED,
        TASK_ASSIGNED
    }

    private EventType type;
    private UUID taskId;
    private UUID boardId;
    private UUID workspaceId;
    private UUID userId;
    private String userName;
    private Object payload;
    private LocalDateTime timestamp;
}
