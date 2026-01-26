package com.collabnest.backend.service.impl;

import com.collabnest.backend.domain.entity.Task;
import com.collabnest.backend.domain.enums.TaskPriority;
import com.collabnest.backend.repository.TaskRepository;
import com.collabnest.backend.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    @Override
    public Task createTask(UUID columnId, String title, String description, TaskPriority priority, LocalDate dueDate, UUID assigneeId) {
        throw new UnsupportedOperationException("Implemented in Step 6");
    }

    @Override
    public Task getTask(UUID taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
    }

    @Override
    public List<Task> getColumnTasks(UUID columnId) {
        return taskRepository.findByColumnIdOrderByPositionAsc(columnId);
    }
    
    @Override
    public List<Task> getTasksByWorkspace(UUID workspaceId) {
        throw new UnsupportedOperationException("Implemented in Step 6");
    }

    @Override
    public Task updateTask(UUID taskId, String title, String description, TaskPriority priority, LocalDate dueDate) {
        throw new UnsupportedOperationException("Implemented in Step 6");
    }

    @Override
    public Task moveTask(UUID taskId, UUID newColumnId, Integer position) {
        throw new UnsupportedOperationException("Implemented in Step 6");
    }
    
    @Override
    public Task assignTask(UUID taskId, UUID userId) {
        throw new UnsupportedOperationException("Implemented in Step 6");
    }

    @Override
    public void deleteTask(UUID taskId) {
        throw new UnsupportedOperationException("Implemented in Step 6");
    }
}
