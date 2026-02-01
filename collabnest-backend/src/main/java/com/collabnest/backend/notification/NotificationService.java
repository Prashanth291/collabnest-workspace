package com.collabnest.backend.notification;

import com.collabnest.backend.domain.enums.NotificationType;
import com.collabnest.backend.notification.dto.NotificationDto;
import com.collabnest.backend.notification.dto.NotificationStatsDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    /**
     * Create a notification for a user
     */
    NotificationDto createNotification(
            UUID userId,
            UUID workspaceId,
            NotificationType notificationType,
            String title,
            String message,
            String entityType,
            UUID entityId,
            UUID actorId
    );

    /**
     * Get all notifications for a user
     */
    Page<NotificationDto> getUserNotifications(UUID userId, Pageable pageable);

    /**
     * Get unread notifications for a user
     */
    Page<NotificationDto> getUnreadNotifications(UUID userId, Pageable pageable);

    /**
     * Get notification statistics (total count, unread count)
     */
    NotificationStatsDto getNotificationStats(UUID userId);

    /**
     * Mark a notification as read
     */
    void markAsRead(UUID notificationId, UUID userId);

    /**
     * Mark all notifications as read for a user
     */
    void markAllAsRead(UUID userId);

    /**
     * Delete a notification
     */
    void deleteNotification(UUID notificationId, UUID userId);

    /**
     * Detect and process @mentions in text
     * Returns list of mentioned user IDs
     */
    List<UUID> detectMentions(String text);

    /**
     * Create mention notifications for mentioned users
     */
    void createMentionNotifications(
            List<UUID> mentionedUserIds,
            UUID actorId,
            UUID workspaceId,
            String entityType,
            UUID entityId,
            String entityName
    );
}
