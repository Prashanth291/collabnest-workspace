package com.collabnest.backend.service;

import com.collabnest.backend.domain.entity.Document;

import java.util.List;
import java.util.UUID;

public interface DocumentService {

    Document createDocument(UUID workspaceId, String title, String content, UUID createdById);

    Document getDocument(UUID documentId);

    List<Document> getWorkspaceDocuments(UUID workspaceId);

    Document updateDocument(UUID documentId, String title, String content);

    void deleteDocument(UUID documentId);
}
