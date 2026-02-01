package com.collabnest.backend.repository;

import com.collabnest.backend.domain.entity.Notification;
import com.collabnest.backend.domain.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    /**
     * Find all notifications for a user, ordered by creation time descending
     */
    Page<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * Find unread notifications for a user
     */
    Page<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * Find notifications by type for a user
     */
    List<Notification> findByUserIdAndNotificationTypeOrderByCreatedAtDesc(
            UUID userId, 
            NotificationType notificationType
    );

    /**
     * Count all notifications for a user
     */
    long countByUserId(UUID userId);

    /**
     * Count unread notifications for a user
     */
    long countByUserIdAndIsReadFalse(UUID userId);

    /**
     * Mark notification as read
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt " +
           "WHERE n.id = :notificationId AND n.user.id = :userId")
    int markAsRead(
            @Param("notificationId") UUID notificationId, 
            @Param("userId") UUID userId, 
            @Param("readAt") LocalDateTime readAt
    );

    /**
     * Mark all notifications as read for a user
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt " +
           "WHERE n.user.id = :userId AND n.isRead = false")
    int markAllAsRead(@Param("userId") UUID userId, @Param("readAt") LocalDateTime readAt);

    /**
     * Delete old read notifications (for cleanup)
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.isRead = true AND n.readAt < :cutoffDate")
    void deleteOldReadNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find notifications for a specific entity
     */
    List<Notification> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
            String entityType, 
            UUID entityId
    );
}
