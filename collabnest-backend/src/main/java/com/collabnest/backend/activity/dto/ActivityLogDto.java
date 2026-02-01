package com.collabnest.backend.activity.dto;

import com.collabnest.backend.domain.enums.ActivityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogDto {
    
    private UUID id;
    private UUID workspaceId;
    private UUID userId;
    private String userName;
    private ActivityType activityType;
    private String entityType;
    private UUID entityId;
    private String entityName;
    private String description;
    private Map<String, Object> metadata;
    private LocalDateTime createdAt;
}
