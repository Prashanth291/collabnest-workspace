package com.collabnest.backend.activity.impl;

import com.collabnest.backend.activity.ActivityLogService;
import com.collabnest.backend.activity.dto.ActivityLogDto;
import com.collabnest.backend.domain.entity.ActivityLog;
import com.collabnest.backend.domain.entity.User;
import com.collabnest.backend.domain.entity.Workspace;
import com.collabnest.backend.domain.enums.ActivityType;
import com.collabnest.backend.repository.ActivityLogRepository;
import com.collabnest.backend.repository.UserRepository;
import com.collabnest.backend.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;

    @Override
    @Async
    @Transactional
    public void logActivity(
            UUID workspaceId,
            UUID userId,
            ActivityType activityType,
            String entityType,
            UUID entityId,
            String entityName,
            String description
    ) {
        logActivity(workspaceId, userId, activityType, entityType, entityId, entityName, description, null);
    }

    @Override
    @Async
    @Transactional
    public void logActivity(
            UUID workspaceId,
            UUID userId,
            ActivityType activityType,
            String entityType,
            UUID entityId,
            String entityName,
            String description,
            Map<String, Object> metadata
    ) {
        try {
            Workspace workspace = workspaceRepository.findById(workspaceId)
                    .orElseThrow(() -> new RuntimeException("Workspace not found"));

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            ActivityLog activityLog = ActivityLog.builder()
                    .workspace(workspace)
                    .user(user)
                    .activityType(activityType)
                    .entityType(entityType)
                    .entityId(entityId)
                    .entityName(entityName)
                    .description(description)
                    .metadata(metadata)
                    .build();

            activityLogRepository.save(activityLog);
            log.debug("Activity logged: {} by user {} in workspace {}", activityType, userId, workspaceId);
        } catch (Exception e) {
            log.error("Failed to log activity: {}", e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityLogDto> getWorkspaceActivities(UUID workspaceId, Pageable pageable) {
        return activityLogRepository.findByWorkspaceIdOrderByCreatedAtDesc(workspaceId, pageable)
                .map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityLogDto> getUserActivitiesInWorkspace(UUID workspaceId, UUID userId, Pageable pageable) {
        return activityLogRepository.findByWorkspaceIdAndUserIdOrderByCreatedAtDesc(workspaceId, userId, pageable)
                .map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityLogDto> getEntityActivities(String entityType, UUID entityId) {
        return activityLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private ActivityLogDto toDto(ActivityLog activityLog) {
        return ActivityLogDto.builder()
                .id(activityLog.getId())
                .workspaceId(activityLog.getWorkspace().getId())
                .userId(activityLog.getUser().getId())
                .userName(activityLog.getUser().getUsername())
                .activityType(activityLog.getActivityType())
                .entityType(activityLog.getEntityType())
                .entityId(activityLog.getEntityId())
                .entityName(activityLog.getEntityName())
                .description(activityLog.getDescription())
                .metadata(activityLog.getMetadata())
                .createdAt(activityLog.getCreatedAt())
                .build();
    }
}
