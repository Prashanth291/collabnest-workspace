package com.collabnest.backend.repository;

import com.collabnest.backend.domain.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    List<Comment> findByEntityIdAndEntityTypeOrderByCreatedAtDesc(UUID entityId, String entityType);
}
