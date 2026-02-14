package com.collabnest.backend.controller;

import com.collabnest.backend.domain.entity.FileEntity;
import com.collabnest.backend.dto.file.FileResponse;
import com.collabnest.backend.security.UserPrincipal;
import com.collabnest.backend.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/files")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;

    /**
     * Register file metadata (file upload URL is generated externally or stored as
     * path).
     */
    @PostMapping
    @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'MEMBER')")
    public ResponseEntity<FileResponse> uploadFileMetadata(
            @PathVariable UUID workspaceId,
            @RequestParam String fileName,
            @RequestParam String fileUrl,
            @RequestParam(required = false) Long size,
            @RequestParam(required = false) UUID taskId,
            @RequestParam(required = false) UUID documentId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        FileEntity file = fileStorageService.storeFileMetadata(
                workspaceId,
                userPrincipal.getUserId(),
                fileName,
                fileUrl,
                size,
                taskId,
                documentId);
        return ResponseEntity.ok(toFileResponse(file));
    }

    /**
     * List all files in a workspace.
     */
    @GetMapping
    @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'VIEWER')")
    public ResponseEntity<List<FileResponse>> getWorkspaceFiles(@PathVariable UUID workspaceId) {
        List<FileResponse> responses = fileStorageService.getWorkspaceFiles(workspaceId)
                .stream()
                .map(this::toFileResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * Get a specific file.
     */
    @GetMapping("/{fileId}")
    @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'VIEWER')")
    public ResponseEntity<FileResponse> getFile(
            @PathVariable UUID workspaceId,
            @PathVariable UUID fileId) {
        return ResponseEntity.ok(toFileResponse(fileStorageService.getFile(fileId)));
    }

    /**
     * Delete a file.
     */
    @DeleteMapping("/{fileId}")
    @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'MEMBER')")
    public ResponseEntity<Void> deleteFile(
            @PathVariable UUID workspaceId,
            @PathVariable UUID fileId) {
        fileStorageService.deleteFile(fileId);
        return ResponseEntity.noContent().build();
    }

    private FileResponse toFileResponse(FileEntity file) {
        return new FileResponse(
                file.getId(),
                file.getWorkspace().getId(),
                file.getUploadedBy().getId(),
                file.getUploadedBy().getUsername(),
                file.getTask() != null ? file.getTask().getId() : null,
                file.getDocument() != null ? file.getDocument().getId() : null,
                file.getFileName(),
                file.getFileUrl(),
                file.getSize(),
                file.getCreatedAt());
    }
}
