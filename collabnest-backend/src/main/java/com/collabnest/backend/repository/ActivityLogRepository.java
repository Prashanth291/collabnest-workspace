package com.collabnest.backend.repository;

import com.collabnest.backend.domain.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID> {
    List<ActivityLog> findByWorkspaceIdOrderByCreatedAtDesc(UUID workspaceId);
    List<ActivityLog> findByActorIdOrderByCreatedAtDesc(UUID actorId);
}
