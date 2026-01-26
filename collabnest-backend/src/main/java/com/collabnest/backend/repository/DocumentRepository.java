package com.collabnest.backend.repository;

import com.collabnest.backend.domain.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {
    List<Document> findByWorkspaceId(UUID workspaceId);
    List<Document> findByCreatedById(UUID userId);
}
