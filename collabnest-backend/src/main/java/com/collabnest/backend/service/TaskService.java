package com.collabnest.backend.service;

import com.collabnest.backend.domain.entity.Task;
import com.collabnest.backend.domain.enums.TaskPriority;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TaskService {

    Task createTask(UUID columnId, String title, String description, TaskPriority priority, LocalDate dueDate, UUID assigneeId);

    Task getTask(UUID taskId);

    List<Task> getColumnTasks(UUID columnId);
    
    List<Task> getTasksByWorkspace(UUID workspaceId);

    Task updateTask(UUID taskId, String title, String description, TaskPriority priority, LocalDate dueDate);

    Task moveTask(UUID taskId, UUID newColumnId, Integer position);
    
    Task assignTask(UUID taskId, UUID userId);

    void deleteTask(UUID taskId);
}
