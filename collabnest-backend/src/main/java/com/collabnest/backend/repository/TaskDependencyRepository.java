package com.collabnest.backend.repository;

import com.collabnest.backend.domain.entity.TaskDependency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskDependencyRepository
        extends JpaRepository<TaskDependency, TaskDependency.TaskDependencyId> {

    List<TaskDependency> findByTaskId(UUID taskId);

    List<TaskDependency> findByDependsOnTaskId(UUID dependsOnTaskId);
}
