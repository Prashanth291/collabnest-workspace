package com.collabnest.backend.domain.enums;

public enum NotificationType {
    // Mention notifications
    MENTION,
    
    // Task notifications
    TASK_ASSIGNED,
    TASK_UPDATED,
    TASK_COMMENT,
    
    // Document notifications
    DOCUMENT_SHARED,
    DOCUMENT_COMMENT,
    
    // Workspace notifications
    WORKSPACE_INVITE,
    MEMBER_JOINED,
    
    // General
    COMMENT_REPLY,
    SYSTEM
}
