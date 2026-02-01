package com.collabnest.backend.notification.dto;

import com.collabnest.backend.domain.enums.NotificationType;
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
public class NotificationDto {
    
    private UUID id;
    private UUID userId;
    private UUID workspaceId;
    private NotificationType notificationType;
    private String title;
    private String message;
    private String entityType;
    private UUID entityId;
    private UUID actorId;
    private String actorName;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
