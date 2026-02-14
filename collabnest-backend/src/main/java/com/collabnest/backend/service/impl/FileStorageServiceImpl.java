package com.collabnest.backend.service.impl;

import com.collabnest.backend.domain.entity.*;
import com.collabnest.backend.exception.ResourceNotFoundException;
import com.collabnest.backend.repository.*;
import com.collabnest.backend.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    private final FileRepository fileRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final DocumentRepository documentRepository;

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

        return fileRepository.save(builder.build());
    }

    @Override
    public FileEntity getFile(UUID fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));
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
        fileRepository.delete(file);
    }
}
