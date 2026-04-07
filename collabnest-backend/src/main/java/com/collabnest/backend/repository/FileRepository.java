package com.collabnest.backend.repository;

import com.collabnest.backend.domain.entity.FileEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FileRepository extends JpaRepository<FileEntity, UUID> {

    @EntityGraph(attributePaths = { "workspace", "uploadedBy", "task", "document" })
    @Override
    java.util.Optional<FileEntity> findById(UUID id);

    @EntityGraph(attributePaths = { "workspace", "uploadedBy", "task", "document" })
    List<FileEntity> findByWorkspaceId(UUID workspaceId);

    @EntityGraph(attributePaths = { "workspace", "uploadedBy", "task", "document" })
    List<FileEntity> findByTaskId(UUID taskId);

    @EntityGraph(attributePaths = { "workspace", "uploadedBy", "task", "document" })
    List<FileEntity> findByDocumentId(UUID documentId);
}
