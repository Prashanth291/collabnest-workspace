package com.collabnest.backend.notification.impl;

import com.collabnest.backend.domain.entity.Notification;
import com.collabnest.backend.domain.entity.User;
import com.collabnest.backend.domain.entity.Workspace;
import com.collabnest.backend.domain.enums.NotificationType;
import com.collabnest.backend.notification.NotificationService;
import com.collabnest.backend.notification.dto.NotificationDto;
import com.collabnest.backend.notification.dto.NotificationStatsDto;
import com.collabnest.backend.repository.NotificationRepository;
import com.collabnest.backend.repository.UserRepository;
import com.collabnest.backend.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // Pattern to match @username mentions
    private static final Pattern MENTION_PATTERN = Pattern.compile("@([a-zA-Z0-9._-]+)");

    @Override
    @Transactional
    public NotificationDto createNotification(
            UUID userId,
            UUID workspaceId,
            NotificationType notificationType,
            String title,
            String message,
            String entityType,
            UUID entityId,
            UUID actorId
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Workspace workspace = workspaceId != null ? 
                workspaceRepository.findById(workspaceId).orElse(null) : null;

        User actor = actorId != null ? 
                userRepository.findById(actorId).orElse(null) : null;

        Notification notification = Notification.builder()
                .user(user)
                .workspace(workspace)
                .notificationType(notificationType)
                .title(title)
                .message(message)
                .entityType(entityType)
                .entityId(entityId)
                .actor(actor)
                .actorName(actor != null ? actor.getUsername() : null)
                .build();

        Notification saved = notificationRepository.save(notification);

        // Broadcast notification via WebSocket
        NotificationDto dto = toDto(saved);
        messagingTemplate.convertAndSend("/topic/user/" + userId + "/notifications", dto);

        log.debug("Created notification for user {}: {}", userId, title);
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDto> getUserNotifications(UUID userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDto> getUnreadNotifications(UUID userId, Pageable pageable) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId, pageable)
                .map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationStatsDto getNotificationStats(UUID userId) {
        long totalCount = notificationRepository.countByUserId(userId);
        long unreadCount = notificationRepository.countByUserIdAndIsReadFalse(userId);

        return NotificationStatsDto.builder()
                .totalCount(totalCount)
                .unreadCount(unreadCount)
                .build();
    }

    @Override
    @Transactional
    public void markAsRead(UUID notificationId, UUID userId) {
        notificationRepository.markAsRead(notificationId, userId, LocalDateTime.now());
        log.debug("Marked notification {} as read for user {}", notificationId, userId);
    }

    @Override
    @Transactional
    public void markAllAsRead(UUID userId) {
        int updated = notificationRepository.markAllAsRead(userId, LocalDateTime.now());
        log.debug("Marked {} notifications as read for user {}", updated, userId);
    }

    @Override
    @Transactional
    public void deleteNotification(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized to delete this notification");
        }

        notificationRepository.delete(notification);
        log.debug("Deleted notification {} for user {}", notificationId, userId);
    }

    @Override
    public List<UUID> detectMentions(String text) {
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }

        List<UUID> mentionedUserIds = new ArrayList<>();
        Matcher matcher = MENTION_PATTERN.matcher(text);

        while (matcher.find()) {
            String username = matcher.group(1);
            userRepository.findByUsername(username).ifPresent(user -> {
                mentionedUserIds.add(user.getId());
                log.debug("Detected mention: @{} ({})", username, user.getId());
            });
        }

        return mentionedUserIds;
    }

    @Override
    @Transactional
    public void createMentionNotifications(
            List<UUID> mentionedUserIds,
            UUID actorId,
            UUID workspaceId,
            String entityType,
            UUID entityId,
            String entityName
    ) {
        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new RuntimeException("Actor not found"));

        for (UUID userId : mentionedUserIds) {
            // Don't notify the actor about their own mention
            if (userId.equals(actorId)) {
                continue;
            }

            String title = String.format("%s mentioned you", actor.getUsername());
            String message = String.format("You were mentioned in %s: %s", entityType.toLowerCase(), entityName);

            createNotification(
                    userId,
                    workspaceId,
                    NotificationType.MENTION,
                    title,
                    message,
                    entityType,
                    entityId,
                    actorId
            );
        }
    }

    private NotificationDto toDto(Notification notification) {
        return NotificationDto.builder()
                .id(notification.getId())
                .userId(notification.getUser().getId())
                .workspaceId(notification.getWorkspace() != null ? notification.getWorkspace().getId() : null)
                .notificationType(notification.getNotificationType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .entityType(notification.getEntityType())
                .entityId(notification.getEntityId())
                .actorId(notification.getActor() != null ? notification.getActor().getId() : null)
                .actorName(notification.getActorName())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .build();
    }
}
