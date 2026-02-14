package com.collabnest.backend.repository;

import com.collabnest.backend.domain.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FileRepository extends JpaRepository<FileEntity, UUID> {

    List<FileEntity> findByWorkspaceId(UUID workspaceId);

    List<FileEntity> findByTaskId(UUID taskId);

    List<FileEntity> findByDocumentId(UUID documentId);
}
