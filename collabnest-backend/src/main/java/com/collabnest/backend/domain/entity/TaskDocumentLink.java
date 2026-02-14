package com.collabnest.backend.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "task_document_links")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(TaskDocumentLink.TaskDocumentLinkId.class)
public class TaskDocumentLink {

    @Id
    @Column(name = "task_id")
    private UUID taskId;

    @Id
    @Column(name = "document_id")
    private UUID documentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", insertable = false, updatable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", insertable = false, updatable = false)
    private Document document;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskDocumentLinkId implements Serializable {
        private UUID taskId;
        private UUID documentId;
    }
}
