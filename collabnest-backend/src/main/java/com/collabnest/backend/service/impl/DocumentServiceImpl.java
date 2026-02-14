package com.collabnest.backend.service.impl;

import com.collabnest.backend.activity.ActivityLogService;
import com.collabnest.backend.domain.entity.Document;
import com.collabnest.backend.domain.entity.User;
import com.collabnest.backend.domain.entity.Workspace;
import com.collabnest.backend.domain.enums.ActivityType;
import com.collabnest.backend.exception.ResourceNotFoundException;
import com.collabnest.backend.repository.DocumentRepository;
import com.collabnest.backend.repository.UserRepository;
import com.collabnest.backend.repository.WorkspaceRepository;
import com.collabnest.backend.service.DocumentService;
import com.collabnest.backend.websocket.dto.DocumentEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ActivityLogService activityLogService;

    @Override
    @Transactional
    public Document createDocument(UUID workspaceId, String title, String content, UUID createdById) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));

        User createdBy = userRepository.findById(createdById)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Document document = Document.builder()
                .workspace(workspace)
                .title(title)
                .content(content)
                .createdBy(createdBy)
                .build();

        Document savedDocument = documentRepository.save(document);

        // Broadcast document creation event
        DocumentEvent event = DocumentEvent.builder()
                .type(DocumentEvent.EventType.DOCUMENT_CREATED)
                .documentId(savedDocument.getId())
                .workspaceId(workspaceId)
                .userId(createdById)
                .userName(createdBy.getUsername())
                .payload(savedDocument)
                .timestamp(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend("/topic/workspace/" + workspaceId, event);

        // Log activity
        activityLogService.logActivity(
                workspaceId,
                createdById,
                ActivityType.DOCUMENT_CREATED,
                "DOCUMENT",
                savedDocument.getId(),
                savedDocument.getTitle(),
                String.format("Created document: %s", savedDocument.getTitle()));

        return savedDocument;
    }

    @Override
    public Document getDocument(UUID documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
    }

    @Override
    public List<Document> getWorkspaceDocuments(UUID workspaceId) {
        return documentRepository.findByWorkspaceId(workspaceId);
    }

    @Override
    @Transactional
    public Document updateDocument(UUID documentId, String title, String content) {
        Document document = getDocument(documentId);

        if (title != null) {
            document.setTitle(title);
        }
        if (content != null) {
            document.setContent(content);
        }

        Document updatedDocument = documentRepository.save(document);

        // Broadcast document update event
        DocumentEvent event = DocumentEvent.builder()
                .type(DocumentEvent.EventType.DOCUMENT_UPDATED)
                .documentId(documentId)
                .workspaceId(updatedDocument.getWorkspace().getId())
                .userId(updatedDocument.getCreatedBy().getId())
                .userName(updatedDocument.getCreatedBy().getUsername())
                .payload(updatedDocument)
                .timestamp(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend("/topic/document/" + documentId, event);

        // Log activity
        activityLogService.logActivity(
                updatedDocument.getWorkspace().getId(),
                updatedDocument.getCreatedBy().getId(),
                ActivityType.DOCUMENT_UPDATED,
                "DOCUMENT",
                updatedDocument.getId(),
                updatedDocument.getTitle(),
                String.format("Updated document: %s", updatedDocument.getTitle()));

        return updatedDocument;
    }

    @Override
    @Transactional
    public void deleteDocument(UUID documentId) {
        Document document = getDocument(documentId);
        UUID workspaceId = document.getWorkspace().getId();
        UUID userId = document.getCreatedBy().getId();
        String userName = document.getCreatedBy().getUsername();

        documentRepository.delete(document);

        // Broadcast document deletion event
        DocumentEvent event = DocumentEvent.builder()
                .type(DocumentEvent.EventType.DOCUMENT_DELETED)
                .documentId(documentId)
                .workspaceId(workspaceId)
                .userId(userId)
                .userName(userName)
                .payload(null)
                .timestamp(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend("/topic/workspace/" + workspaceId, event);

        // Log activity
        activityLogService.logActivity(
                workspaceId,
                userId,
                ActivityType.DOCUMENT_DELETED,
                "DOCUMENT",
                documentId,
                document.getTitle(),
                String.format("Deleted document: %s", document.getTitle()));
    }
}
