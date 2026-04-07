package com.collabnest.backend.service.impl;

import com.collabnest.backend.domain.entity.*;
import com.collabnest.backend.exception.InvalidOperationException;
import com.collabnest.backend.exception.ResourceNotFoundException;
import com.collabnest.backend.repository.*;
import com.collabnest.backend.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    private final FileRepository fileRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final DocumentRepository documentRepository;

    @Value("${app.files.storage-path:uploads}")
    private String storagePath;

    @Override
    @Transactional
    public FileEntity storeFileMetadata(UUID workspaceId, UUID uploadedById,
            String fileName, String fileUrl, Long size,
            UUID taskId, UUID documentId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));
        User uploadedBy = userRepository.findById(uploadedById)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        FileEntity.FileEntityBuilder builder = FileEntity.builder()
                .workspace(workspace)
                .uploadedBy(uploadedBy)
                .fileName(fileName)
                .fileUrl(fileUrl)
                .size(size);

        if (taskId != null) {
            Task task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
            builder.task(task);
        }
        if (documentId != null) {
            Document document = documentRepository.findById(documentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
            builder.document(document);
        }

        FileEntity savedFile = fileRepository.save(builder.build());
        return getFile(savedFile.getId());
    }

    @Override
    @Transactional
    public FileEntity storeUploadedFile(UUID workspaceId, UUID uploadedById,
            MultipartFile file, UUID taskId, UUID documentId) {
        if (file == null || file.isEmpty()) {
            throw new InvalidOperationException("File is required");
        }

        String originalFileName = sanitizeFileName(file.getOriginalFilename());
        FileEntity savedFile = storeFileMetadata(
                workspaceId,
                uploadedById,
                originalFileName,
                "",
                file.getSize(),
                taskId,
                documentId);

        Path targetPath = resolveStoredFilePath(workspaceId, savedFile.getId());
        try {
            Files.createDirectories(targetPath.getParent());
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            throw new InvalidOperationException("Failed to store file");
        }

        savedFile.setFileUrl(buildDownloadUrl(workspaceId, savedFile.getId()));
        fileRepository.save(savedFile);
        return getFile(savedFile.getId());
    }

    @Override
    public FileEntity getFile(UUID fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));
    }

    @Override
    public FileEntity getWorkspaceFile(UUID workspaceId, UUID fileId) {
        FileEntity file = getFile(fileId);
        if (!file.getWorkspace().getId().equals(workspaceId)) {
            throw new ResourceNotFoundException("File not found");
        }
        return file;
    }

    @Override
    public Resource loadFileAsResource(UUID workspaceId, UUID fileId) {
        Path filePath = resolveStoredFilePath(workspaceId, fileId);
        if (!Files.exists(filePath)) {
            throw new ResourceNotFoundException("File content not found");
        }
        return new PathResource(filePath);
    }

    @Override
    public List<FileEntity> getWorkspaceFiles(UUID workspaceId) {
        return fileRepository.findByWorkspaceId(workspaceId);
    }

    @Override
    public List<FileEntity> getTaskFiles(UUID taskId) {
        return fileRepository.findByTaskId(taskId);
    }

    @Override
    public List<FileEntity> getDocumentFiles(UUID documentId) {
        return fileRepository.findByDocumentId(documentId);
    }

    @Override
    @Transactional
    public void deleteFile(UUID fileId) {
        FileEntity file = getFile(fileId);
        Path filePath = resolveStoredFilePath(file.getWorkspace().getId(), fileId);
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new InvalidOperationException("Failed to delete file content");
        }
        fileRepository.delete(file);
    }

    private Path resolveStoredFilePath(UUID workspaceId, UUID fileId) {
        return Path.of(storagePath, workspaceId.toString(), fileId.toString());
    }

    private String buildDownloadUrl(UUID workspaceId, UUID fileId) {
        return "/api/workspaces/" + workspaceId + "/files/" + fileId + "/download";
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "file";
        }
        String normalized = Path.of(fileName).getFileName().toString();
        return normalized.toLowerCase(Locale.ROOT).replace("\\", "_").replace("/", "_");
    }
}
