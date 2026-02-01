package com.collabnest.backend.repository;

import com.collabnest.backend.domain.entity.ActivityLog;
import com.collabnest.backend.domain.enums.ActivityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID> {

    /**
     * Find all activities in a workspace, ordered by creation time descending
     */
    Page<ActivityLog> findByWorkspaceIdOrderByCreatedAtDesc(UUID workspaceId, Pageable pageable);

    /**
     * Find activities by a specific user in a workspace
     */
    Page<ActivityLog> findByWorkspaceIdAndUserIdOrderByCreatedAtDesc(
            UUID workspaceId, 
            UUID userId, 
            Pageable pageable
    );

    /**
     * Find activities for a specific entity
     */
    List<ActivityLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
            String entityType, 
            UUID entityId
    );

    /**
     * Find activities by type within a workspace
     */
    Page<ActivityLog> findByWorkspaceIdAndActivityTypeOrderByCreatedAtDesc(
            UUID workspaceId, 
            ActivityType activityType, 
            Pageable pageable
    );

    /**
     * Find recent activities within a time range
     */
    @Query("SELECT a FROM ActivityLog a WHERE a.workspace.id = :workspaceId " +
           "AND a.createdAt >= :startDate ORDER BY a.createdAt DESC")
    List<ActivityLog> findRecentActivities(
            @Param("workspaceId") UUID workspaceId, 
            @Param("startDate") LocalDateTime startDate
    );

    /**
     * Count activities by workspace
     */
    long countByWorkspaceId(UUID workspaceId);

    /**
     * Delete old activities (for cleanup)
     */
    void deleteByCreatedAtBefore(LocalDateTime cutoffDate);
}
