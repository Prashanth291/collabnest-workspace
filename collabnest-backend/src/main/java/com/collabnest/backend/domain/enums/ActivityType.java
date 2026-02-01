package com.collabnest.backend.domain.enums;

public enum ActivityType {
    // Workspace activities
    WORKSPACE_CREATED,
    WORKSPACE_UPDATED,
    WORKSPACE_DELETED,
    MEMBER_ADDED,
    MEMBER_REMOVED,
    
    // Board activities
    BOARD_CREATED,
    BOARD_UPDATED,
    BOARD_DELETED,
    COLUMN_CREATED,
    COLUMN_UPDATED,
    COLUMN_DELETED,
    
    // Task activities
    TASK_CREATED,
    TASK_UPDATED,
    TASK_MOVED,
    TASK_ASSIGNED,
    TASK_DELETED,
    
    // Document activities
    DOCUMENT_CREATED,
    DOCUMENT_UPDATED,
    DOCUMENT_DELETED,
    
    // Comment activities
    COMMENT_ADDED,
    COMMENT_UPDATED,
    COMMENT_DELETED
}
