package com.collabnest.backend.repository;

import com.collabnest.backend.domain.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findByColumnIdOrderByPositionAsc(UUID columnId);
    
    List<Task> findByCreatedById(UUID userId);
    
    @Query("SELECT t FROM Task t WHERE t.column.board.workspace.id = :workspaceId")
    List<Task> findByWorkspaceId(@Param("workspaceId") UUID workspaceId);
    
    @Query("SELECT MAX(t.position) FROM Task t WHERE t.column.id = :columnId")
    Integer findMaxPositionByColumnId(@Param("columnId") UUID columnId);
}
