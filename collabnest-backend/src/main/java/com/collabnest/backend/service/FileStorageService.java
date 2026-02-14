package com.collabnest.backend.service;

import com.collabnest.backend.domain.entity.FileEntity;

import java.util.List;
import java.util.UUID;

public interface FileStorageService {

    FileEntity storeFileMetadata(UUID workspaceId, UUID uploadedById,
            String fileName, String fileUrl, Long size,
            UUID taskId, UUID documentId);

    FileEntity getFile(UUID fileId);

    List<FileEntity> getWorkspaceFiles(UUID workspaceId);

    List<FileEntity> getTaskFiles(UUID taskId);

    List<FileEntity> getDocumentFiles(UUID documentId);

    void deleteFile(UUID fileId);
}
