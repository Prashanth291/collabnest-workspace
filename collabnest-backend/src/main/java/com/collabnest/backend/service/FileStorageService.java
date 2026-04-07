package com.collabnest.backend.service;

import com.collabnest.backend.domain.entity.FileEntity;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface FileStorageService {

    FileEntity storeFileMetadata(UUID workspaceId, UUID uploadedById,
            String fileName, String fileUrl, Long size,
            UUID taskId, UUID documentId);

    FileEntity storeUploadedFile(UUID workspaceId, UUID uploadedById,
            MultipartFile file, UUID taskId, UUID documentId);

    FileEntity getFile(UUID fileId);

    FileEntity getWorkspaceFile(UUID workspaceId, UUID fileId);

    Resource loadFileAsResource(UUID workspaceId, UUID fileId);

    List<FileEntity> getWorkspaceFiles(UUID workspaceId);

    List<FileEntity> getTaskFiles(UUID taskId);

    List<FileEntity> getDocumentFiles(UUID documentId);

    void deleteFile(UUID fileId);
}
