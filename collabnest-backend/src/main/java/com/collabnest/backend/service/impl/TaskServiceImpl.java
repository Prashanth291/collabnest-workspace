package com.collabnest.backend.service.impl;

import com.collabnest.backend.activity.ActivityLogService;
import com.collabnest.backend.domain.entity.BoardColumn;
import com.collabnest.backend.domain.entity.Task;
import com.collabnest.backend.domain.entity.User;
import com.collabnest.backend.domain.enums.ActivityType;
import com.collabnest.backend.domain.enums.TaskPriority;
import com.collabnest.backend.exception.ResourceNotFoundException;
import com.collabnest.backend.exception.UnauthorizedException;
import com.collabnest.backend.repository.BoardColumnRepository;
import com.collabnest.backend.repository.TaskRepository;
import com.collabnest.backend.repository.UserRepository;
import com.collabnest.backend.repository.WorkspaceMemberRepository;
import com.collabnest.backend.service.TaskService;
import com.collabnest.backend.websocket.dto.TaskEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final BoardColumnRepository columnRepository;
    private final UserRepository userRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ActivityLogService activityLogService;

    @Override
    @Transactional
    public Task createTask(UUID columnId, String title, String description, TaskPriority priority, LocalDate dueDate, UUID assigneeId) {
        BoardColumn column = columnRepository.findById(columnId)
                .orElseThrow(() -> new ResourceNotFoundException("Column not found"));
        
        User createdBy = userRepository.findById(assigneeId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
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
        
        Task savedTask = taskRepository.save(task);
        
        // Broadcast task creation event
        TaskEvent event = TaskEvent.builder()
                .type(TaskEvent.EventType.TASK_CREATED)
                .taskId(savedTask.getId())
                .boardId(column.getBoard().getId())
                .workspaceId(column.getBoard().getWorkspace().getId())
                .userId(createdBy.getId())
                .userName(createdBy.getUsername())
                .payload(savedTask)
                .timestamp(LocalDateTime.now())
                .build();
        
        messagingTemplate.convertAndSend("/topic/board/" + column.getBoard().getId(), event);
        
        // Log activity
        activityLogService.logActivity(
                column.getBoard().getWorkspace().getId(),
                createdBy.getId(),
                ActivityType.TASK_CREATED,
                "TASK",
                savedTask.getId(),
                savedTask.getTitle(),
                String.format("Created task: %s", savedTask.getTitle())
        );
        
        return savedTask;
    }

    @Override
    public Task getTask(UUID taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
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
        
        Task updatedTask = taskRepository.save(task);
        
        // Broadcast task update event
        TaskEvent event = TaskEvent.builder()
                .type(TaskEvent.EventType.TASK_UPDATED)
                .taskId(updatedTask.getId())
                .boardId(updatedTask.getColumn().getBoard().getId())
                .workspaceId(updatedTask.getColumn().getBoard().getWorkspace().getId())
                .userId(updatedTask.getCreatedBy().getId())
                .userName(updatedTask.getCreatedBy().getUsername())
                .payload(updatedTask)
                .timestamp(LocalDateTime.now())
                .build();
        
        messagingTemplate.convertAndSend("/topic/board/" + updatedTask.getColumn().getBoard().getId(), event);
        
        // Log activity
        activityLogService.logActivity(
                updatedTask.getColumn().getBoard().getWorkspace().getId(),
                updatedTask.getCreatedBy().getId(),
                ActivityType.TASK_UPDATED,
                "TASK",
                updatedTask.getId(),
                updatedTask.getTitle(),
                String.format("Updated task: %s", updatedTask.getTitle())
        );
        
        return updatedTask;
    }

    @Override
    @Transactional
    public Task moveTask(UUID taskId, UUID newColumnId, Integer position) {
        Task task = getTask(taskId);
        BoardColumn newColumn = columnRepository.findById(newColumnId)
                .orElseThrow(() -> new ResourceNotFoundException("Column not found"));
        
        UUID oldColumnId = task.getColumn().getId();
        boolean movedToNewColumn = !oldColumnId.equals(newColumnId);
        
        // If moving to a different column, update positions in both columns
        if (movedToNewColumn) {
            // Update positions in old column (shift down tasks after removed position)
            List<Task> oldColumnTasks = taskRepository.findByColumnIdOrderByPositionAsc(oldColumnId);
            int oldPosition = task.getPosition();
            for (Task t : oldColumnTasks) {
                if (t.getPosition() > oldPosition && !t.getId().equals(taskId)) {
                    t.setPosition(t.getPosition() - 1);
                    taskRepository.save(t);
                }
            }
            
            // Update positions in new column (shift up tasks at or after new position)
            List<Task> newColumnTasks = taskRepository.findByColumnIdOrderByPositionAsc(newColumnId);
            for (Task t : newColumnTasks) {
                if (t.getPosition() >= position) {
                    t.setPosition(t.getPosition() + 1);
                    taskRepository.save(t);
                }
            }
            
            task.setColumn(newColumn);
        } else {
            // Moving within same column - adjust positions
            List<Task> columnTasks = taskRepository.findByColumnIdOrderByPositionAsc(oldColumnId);
            int oldPosition = task.getPosition();
            
            if (position != oldPosition) {
                for (Task t : columnTasks) {
                    if (t.getId().equals(taskId)) continue;
                    
                    if (oldPosition < position) {
                        // Moving down: shift tasks between old and new position up
                        if (t.getPosition() > oldPosition && t.getPosition() <= position) {
                            t.setPosition(t.getPosition() - 1);
                            taskRepository.save(t);
                        }
                    } else {
                        // Moving up: shift tasks between new and old position down
                        if (t.getPosition() >= position && t.getPosition() < oldPosition) {
                            t.setPosition(t.getPosition() + 1);
                            taskRepository.save(t);
                        }
                    }
                }
            }
        }
        
        task.setPosition(position);
        Task movedTask = taskRepository.save(task);
        
        // Broadcast task move event
        TaskEvent event = TaskEvent.builder()
                .type(TaskEvent.EventType.TASK_MOVED)
                .taskId(movedTask.getId())
                .boardId(newColumn.getBoard().getId())
                .workspaceId(newColumn.getBoard().getWorkspace().getId())
                .userId(movedTask.getCreatedBy().getId())
                .userName(movedTask.getCreatedBy().getUsername())
                .payload(movedTask)
                .timestamp(LocalDateTime.now())
                .build();
        
        messagingTemplate.convertAndSend("/topic/board/" + newColumn.getBoard().getId(), event);
        
        return movedTask;
    }
    
    @Override
    @Transactional
    public Task assignTask(UUID taskId, UUID userId) {
        Task task = getTask(taskId);
        User assignee = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Validate that assignee is a member of the workspace
        UUID workspaceId = task.getColumn().getBoard().getWorkspace().getId();
        boolean isMember = workspaceMemberRepository
                .findByWorkspaceIdAndUserId(workspaceId, userId)
                .isPresent();
        
        if (!isMember) {
            throw new UnauthorizedException("User is not a member of this workspace");
        }
        
        task.setAssignee(assignee);
        Task assignedTask = taskRepository.save(task);
        
        // Broadcast task assignment event
        TaskEvent event = TaskEvent.builder()
                .type(TaskEvent.EventType.TASK_ASSIGNED)
                .taskId(assignedTask.getId())
                .boardId(assignedTask.getColumn().getBoard().getId())
                .workspaceId(assignedTask.getColumn().getBoard().getWorkspace().getId())
                .userId(userId)
                .userName(assignee.getUsername())
                .payload(assignedTask)
                .timestamp(LocalDateTime.now())
                .build();
        
        messagingTemplate.convertAndSend("/topic/board/" + assignedTask.getColumn().getBoard().getId(), event);
        
        return assignedTask;
    }

    @Override
    @Transactional
    public void deleteTask(UUID taskId) {
        Task task = getTask(taskId);
        UUID boardId = task.getColumn().getBoard().getId();
        UUID workspaceId = task.getColumn().getBoard().getWorkspace().getId();
        UUID userId = task.getCreatedBy().getId();
        String userName = task.getCreatedBy().getUsername();
        
        taskRepository.delete(task);
        
        // Broadcast task deletion event
        TaskEvent event = TaskEvent.builder()
                .type(TaskEvent.EventType.TASK_DELETED)
                .taskId(taskId)
                .boardId(boardId)
                .workspaceId(workspaceId)
                .userId(userId)
                .userName(userName)
                .payload(null)
                .timestamp(LocalDateTime.now())
                .build();
        
        messagingTemplate.convertAndSend("/topic/board/" + boardId, event);
    }
}
