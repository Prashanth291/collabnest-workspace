package com.collabnest.backend.repository;

import com.collabnest.backend.domain.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BoardRepository extends JpaRepository<Board, UUID> {
    List<Board> findByWorkspaceIdOrderByPositionAsc(UUID workspaceId);
}
