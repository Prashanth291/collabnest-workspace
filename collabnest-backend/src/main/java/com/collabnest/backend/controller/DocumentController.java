package com.collabnest.backend.controller;

import com.collabnest.backend.domain.entity.Comment;
import com.collabnest.backend.domain.entity.Document;
import com.collabnest.backend.dto.comment.CommentResponse;
import com.collabnest.backend.dto.comment.CreateCommentRequest;
import com.collabnest.backend.dto.document.CreateDocumentRequest;
import com.collabnest.backend.dto.document.DocumentResponse;
import com.collabnest.backend.dto.document.UpdateDocumentRequest;
import com.collabnest.backend.security.UserPrincipal;
import com.collabnest.backend.service.CommentService;
import com.collabnest.backend.service.DocumentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Document controller with workspace-aware authorization.
 */
@RestController
@RequestMapping("/api/workspaces/{workspaceId}/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final CommentService commentService;

    public DocumentController(DocumentService documentService, CommentService commentService) {
        this.documentService = documentService;
        this.commentService = commentService;
    }

    /**
     * Create a new document - requires MEMBER role.
     */
    @PostMapping
    @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'MEMBER')")
    public ResponseEntity<DocumentResponse> createDocument(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody CreateDocumentRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        UUID userId = userPrincipal.getUserId();
        
        Document document = documentService.createDocument(
                workspaceId,
                request.title(),
                request.content(),
                userId
        );
        
        DocumentResponse response = new DocumentResponse(
                document.getId(),
                document.getWorkspace().getId(),
                document.getTitle(),
                document.getContent(),
                document.getCreatedBy().getId(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get all documents in workspace - requires VIEWER role.
     */
    @GetMapping
    @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'VIEWER')")
    public ResponseEntity<List<DocumentResponse>> getDocuments(@PathVariable UUID workspaceId) {
        List<Document> documents = documentService.getWorkspaceDocuments(workspaceId);
        
        List<DocumentResponse> responses = documents.stream()
                .map(doc -> new DocumentResponse(
                        doc.getId(),
                        doc.getWorkspace().getId(),
                        doc.getTitle(),
                        doc.getContent(),
                        doc.getCreatedBy().getId(),
                        doc.getCreatedAt(),
                        doc.getUpdatedAt()
                ))
                .toList();
        
        return ResponseEntity.ok(responses);
    }

    /**
     * Get a specific document - requires VIEWER role.
     */
    @GetMapping("/{documentId}")
    @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'VIEWER')")
    public ResponseEntity<DocumentResponse> getDocument(
            @PathVariable UUID workspaceId,
            @PathVariable UUID documentId) {
        
        Document document = documentService.getDocument(documentId);
        
        // Verify document belongs to this workspace
        if (!document.getWorkspace().getId().equals(workspaceId)) {
            return ResponseEntity.notFound().build();
        }
        
        DocumentResponse response = new DocumentResponse(
                document.getId(),
                document.getWorkspace().getId(),
                document.getTitle(),
                document.getContent(),
                document.getCreatedBy().getId(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Update a document - requires MEMBER role.
     */
    @PutMapping("/{documentId}")
    @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'MEMBER')")
    public ResponseEntity<DocumentResponse> updateDocument(
            @PathVariable UUID workspaceId,
            @PathVariable UUID documentId,
            @Valid @RequestBody UpdateDocumentRequest request) {
        
        Document document = documentService.updateDocument(
                documentId,
                request.title(),
                request.content()
        );
        
        DocumentResponse response = new DocumentResponse(
                document.getId(),
                document.getWorkspace().getId(),
                document.getTitle(),
                document.getContent(),
                document.getCreatedBy().getId(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a document - requires ADMIN role.
     */
    @DeleteMapping("/{documentId}")
    @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'ADMIN')")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable UUID workspaceId,
            @PathVariable UUID documentId) {
        
        documentService.deleteDocument(documentId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Add a comment to a document - requires MEMBER role.
     */
    @PostMapping("/{documentId}/comments")
    @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'MEMBER')")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable UUID workspaceId,
            @PathVariable UUID documentId,
            @Valid @RequestBody CreateCommentRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        UUID userId = userPrincipal.getUserId();
        
        Comment comment = commentService.createComment(
                documentId,
                "document",
                request.content(),
                userId
        );
        
        CommentResponse response = new CommentResponse(
                comment.getId(),
                comment.getEntityId(),
                comment.getEntityType(),
                comment.getContent(),
                comment.getCreatedBy().getId(),
                comment.getCreatedBy().getUsername(),
                comment.getCreatedAt()
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get all comments on a document - requires VIEWER role.
     */
    @GetMapping("/{documentId}/comments")
    @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'VIEWER')")
    public ResponseEntity<List<CommentResponse>> getComments(
            @PathVariable UUID workspaceId,
            @PathVariable UUID documentId) {
        
        List<Comment> comments = commentService.getEntityComments(documentId, "document");
        
        List<CommentResponse> responses = comments.stream()
                .map(comment -> new CommentResponse(
                        comment.getId(),
                        comment.getEntityId(),
                        comment.getEntityType(),
                        comment.getContent(),
                        comment.getCreatedBy().getId(),
                        comment.getCreatedBy().getUsername(),
                        comment.getCreatedAt()
                ))
                .toList();
        
        return ResponseEntity.ok(responses);
    }

    /**
     * Delete a comment - requires MEMBER role (or comment creator).
     */
    @DeleteMapping("/{documentId}/comments/{commentId}")
    @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'MEMBER')")
    public ResponseEntity<Void> deleteComment(
            @PathVariable UUID workspaceId,
            @PathVariable UUID documentId,
            @PathVariable UUID commentId) {
        
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}
