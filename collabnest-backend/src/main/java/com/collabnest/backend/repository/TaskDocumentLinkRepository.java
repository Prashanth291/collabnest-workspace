package com.collabnest.backend.repository;

import com.collabnest.backend.domain.entity.TaskDocumentLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskDocumentLinkRepository
        extends JpaRepository<TaskDocumentLink, TaskDocumentLink.TaskDocumentLinkId> {

    List<TaskDocumentLink> findByTaskId(UUID taskId);

    List<TaskDocumentLink> findByDocumentId(UUID documentId);
}
