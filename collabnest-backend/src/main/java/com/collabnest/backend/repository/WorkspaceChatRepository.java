package com.collabnest.backend.repository;

import com.collabnest.backend.domain.entity.WorkspaceChat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WorkspaceChatRepository extends JpaRepository<WorkspaceChat, UUID> {
    Optional<WorkspaceChat> findByWorkspaceId(UUID workspaceId);
}
