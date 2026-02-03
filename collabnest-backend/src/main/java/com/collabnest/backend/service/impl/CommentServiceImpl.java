package com.collabnest.backend.service.impl;

import com.collabnest.backend.activity.ActivityLogService;
import com.collabnest.backend.domain.entity.Comment;
import com.collabnest.backend.domain.entity.User;
import com.collabnest.backend.domain.enums.ActivityType;
import com.collabnest.backend.notification.NotificationService;
import com.collabnest.backend.repository.CommentRepository;
import com.collabnest.backend.repository.UserRepository;
import com.collabnest.backend.service.CommentService;
import com.collabnest.backend.websocket.dto.DocumentEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ActivityLogService activityLogService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public Comment createComment(UUID entityId, String entityType, String content, UUID createdById) {
        User createdBy = userRepository.findById(createdById)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Comment comment = Comment.builder()
                .entityId(entityId)
                .entityType(entityType)
                .content(content)
                .createdBy(createdBy)
                .build();
        
        Comment savedComment = commentRepository.save(comment);
        
        // Process mentions asynchronously after save
        List<UUID> mentionedUsers = notificationService.detectMentions(content);
        if (!mentionedUsers.isEmpty() && "document".equalsIgnoreCase(entityType)) {
            notificationService.createMentionNotifications(
                    mentionedUsers,
                    createdById,
                    null,
                    entityType,
                    entityId,
                    "comment"
            );
        }
        
        return savedComment;
    }

    @Override
    public Comment getComment(UUID commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
    }

    @Override
    public List<Comment> getEntityComments(UUID entityId, String entityType) {
        return commentRepository.findByEntityIdAndEntityTypeOrderByCreatedAtDesc(entityId, entityType);
    }

    @Override
    @Transactional
    public void deleteComment(UUID commentId) {
        Comment comment = getComment(commentId);
        UUID entityId = comment.getEntityId();
        String entityType = comment.getEntityType();
        UUID userId = comment.getCreatedBy().getId();
        String userName = comment.getCreatedBy().getUsername();
        
        commentRepository.delete(comment);
        
        // Broadcast comment deletion event
        if ("DOCUMENT".equals(entityType)) {
            DocumentEvent event = DocumentEvent.builder()
                    .type(DocumentEvent.EventType.COMMENT_DELETED)
                    .documentId(entityId)
                    .workspaceId(null)
                    .userId(userId)
                    .userName(userName)
                    .payload(null)
                    .timestamp(LocalDateTime.now())
                    .build();
            
            messagingTemplate.convertAndSend("/topic/document/" + entityId, event);
        }
    }
}
