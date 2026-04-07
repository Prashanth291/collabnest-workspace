package com.collabnest.backend.controller;

import com.collabnest.backend.domain.entity.FileEntity;
import com.collabnest.backend.dto.file.FileResponse;
import com.collabnest.backend.security.UserPrincipal;
import com.collabnest.backend.service.FileStorageService;
import com.collabnest.backend.security.WorkspacePermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/files")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;
    private final WorkspacePermissionService workspacePermissionService;

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

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'MEMBER')")
    public ResponseEntity<FileResponse> uploadFile(
            @PathVariable UUID workspaceId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) UUID taskId,
            @RequestParam(required = false) UUID documentId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        FileEntity savedFile = fileStorageService.storeUploadedFile(
                workspaceId,
                userPrincipal.getUserId(),
                file,
                taskId,
                documentId);
        return ResponseEntity.ok(toFileResponse(savedFile));
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
        return ResponseEntity.ok(toFileResponse(fileStorageService.getWorkspaceFile(workspaceId, fileId)));
    }

    @GetMapping("/{fileId}/download")
    @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'VIEWER')")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable UUID workspaceId,
            @PathVariable UUID fileId) {
        FileEntity file = fileStorageService.getWorkspaceFile(workspaceId, fileId);
        Resource resource = fileStorageService.loadFileAsResource(workspaceId, fileId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    /**
     * Delete a file.
     */
    @DeleteMapping("/{fileId}")
    @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'VIEWER')")
    public ResponseEntity<Void> deleteFile(
            @PathVariable UUID workspaceId,
            @PathVariable UUID fileId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        FileEntity file = fileStorageService.getWorkspaceFile(workspaceId, fileId);
        boolean isUploader = file.getUploadedBy().getId().equals(userPrincipal.getUserId());
        boolean isWorkspaceAdmin = workspacePermissionService.isWorkspaceAdmin(userPrincipal.getUserId(), workspaceId);

        if (!isUploader && !isWorkspaceAdmin) {
            throw new AccessDeniedException("Only uploader or workspace admin can delete this file");
        }

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
