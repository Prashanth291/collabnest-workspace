package com.collabnest.backend.service.impl;

import com.collabnest.backend.domain.entity.Document;
import com.collabnest.backend.domain.entity.User;
import com.collabnest.backend.domain.entity.Workspace;
import com.collabnest.backend.repository.DocumentRepository;
import com.collabnest.backend.repository.UserRepository;
import com.collabnest.backend.repository.WorkspaceRepository;
import com.collabnest.backend.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Document createDocument(UUID workspaceId, String title, String content, UUID createdById) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new RuntimeException("Workspace not found"));
        
        User createdBy = userRepository.findById(createdById)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Document document = Document.builder()
                .workspace(workspace)
                .title(title)
                .content(content)
                .createdBy(createdBy)
                .build();
        
        return documentRepository.save(document);
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
    @Transactional
    public Document updateDocument(UUID documentId, String title, String content) {
        Document document = getDocument(documentId);
        
        if (title != null) {
            document.setTitle(title);
        }
        if (content != null) {
            document.setContent(content);
        }
        
        return documentRepository.save(document);
    }

    @Override
    @Transactional
    public void deleteDocument(UUID documentId) {
        Document document = getDocument(documentId);
        documentRepository.delete(document);
    }
}
