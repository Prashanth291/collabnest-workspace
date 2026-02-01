package com.collabnest.backend.activity;

import com.collabnest.backend.activity.dto.ActivityLogDto;
import com.collabnest.backend.domain.enums.ActivityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ActivityLogService {

    /**
     * Log an activity asynchronously
     */
    void logActivity(
            UUID workspaceId,
            UUID userId,
            ActivityType activityType,
            String entityType,
            UUID entityId,
            String entityName,
            String description
    );

    /**
     * Log an activity with metadata
     */
    void logActivity(
            UUID workspaceId,
            UUID userId,
            ActivityType activityType,
            String entityType,
            UUID entityId,
            String entityName,
            String description,
            Map<String, Object> metadata
    );

    /**
     * Get activity feed for a workspace
     */
    Page<ActivityLogDto> getWorkspaceActivities(UUID workspaceId, Pageable pageable);

    /**
     * Get activities for a specific user in a workspace
     */
    Page<ActivityLogDto> getUserActivitiesInWorkspace(UUID workspaceId, UUID userId, Pageable pageable);

    /**
     * Get activities for a specific entity (e.g., all activities on a task)
     */
    List<ActivityLogDto> getEntityActivities(String entityType, UUID entityId);
}
