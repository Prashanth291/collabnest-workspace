package com.collabnest.backend.service.impl;

import com.collabnest.backend.domain.entity.Document;
import com.collabnest.backend.repository.DocumentRepository;
import com.collabnest.backend.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;

    @Override
    public Document createDocument(UUID workspaceId, String title, String content, UUID createdById) {
        throw new UnsupportedOperationException("Implemented in Step 6");
    }

    @Override
    public Document getDocument(UUID documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
    }

    @Override
    public List<Document> getWorkspaceDocuments(UUID workspaceId) {
        return documentRepository.findByWorkspaceId(workspaceId);
    }

    @Override
    public Document updateDocument(UUID documentId, String title, String content) {
        throw new UnsupportedOperationException("Implemented in Step 6");
    }

    @Override
    public void deleteDocument(UUID documentId) {
        throw new UnsupportedOperationException("Implemented in Step 6");
    }
}
