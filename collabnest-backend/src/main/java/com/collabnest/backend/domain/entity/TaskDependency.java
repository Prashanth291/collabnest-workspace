package com.collabnest.backend.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "task_dependencies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(TaskDependency.TaskDependencyId.class)
public class TaskDependency {

    @Id
    @Column(name = "task_id")
    private UUID taskId;

    @Id
    @Column(name = "depends_on_task_id")
    private UUID dependsOnTaskId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", insertable = false, updatable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depends_on_task_id", insertable = false, updatable = false)
    private Task dependsOnTask;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskDependencyId implements Serializable {
        private UUID taskId;
        private UUID dependsOnTaskId;
    }
}
