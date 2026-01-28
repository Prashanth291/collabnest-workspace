package com.collabnest.backend.service.impl;

import com.collabnest.backend.domain.entity.BoardColumn;
import com.collabnest.backend.domain.entity.Task;
import com.collabnest.backend.domain.entity.User;
import com.collabnest.backend.domain.enums.TaskPriority;
import com.collabnest.backend.repository.BoardColumnRepository;
import com.collabnest.backend.repository.TaskRepository;
import com.collabnest.backend.repository.UserRepository;
import com.collabnest.backend.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final BoardColumnRepository columnRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Task createTask(UUID columnId, String title, String description, TaskPriority priority, LocalDate dueDate, UUID assigneeId) {
        BoardColumn column = columnRepository.findById(columnId)
                .orElseThrow(() -> new RuntimeException("Column not found"));
        
        User createdBy = userRepository.findById(assigneeId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Get next position (max position + 1)
        Integer maxPosition = taskRepository.findMaxPositionByColumnId(columnId);
        Integer newPosition = (maxPosition == null) ? 0 : maxPosition + 1;
        
        Task task = Task.builder()
                .column(column)
                .title(title)
                .description(description)
                .priority(priority)
                .dueDate(dueDate)
                .position(newPosition)
                .createdBy(createdBy)
                .build();
        
        return taskRepository.save(task);
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
        return taskRepository.findByWorkspaceId(workspaceId);
    }

    @Override
    @Transactional
    public Task updateTask(UUID taskId, String title, String description, TaskPriority priority, LocalDate dueDate) {
        Task task = getTask(taskId);
        
        if (title != null) {
            task.setTitle(title);
        }
        if (description != null) {
            task.setDescription(description);
        }
        if (priority != null) {
            task.setPriority(priority);
        }
        if (dueDate != null) {
            task.setDueDate(dueDate);
        }
        
        return taskRepository.save(task);
    }

    @Override
    @Transactional
    public Task moveTask(UUID taskId, UUID newColumnId, Integer position) {
        Task task = getTask(taskId);
        BoardColumn newColumn = columnRepository.findById(newColumnId)
                .orElseThrow(() -> new RuntimeException("Column not found"));
        
        UUID oldColumnId = task.getColumn().getId();
        
        // If moving to a different column
        if (!oldColumnId.equals(newColumnId)) {
            task.setColumn(newColumn);
        }
        
        task.setPosition(position);
        return taskRepository.save(task);
    }
    
    @Override
    @Transactional
    public Task assignTask(UUID taskId, UUID userId) {
        Task task = getTask(taskId);
        User assignee = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        task.setAssignee(assignee);
        return taskRepository.save(task);
    }

    @Override
    @Transactional
    public void deleteTask(UUID taskId) {
        Task task = getTask(taskId);
        taskRepository.delete(task);
    }
}
