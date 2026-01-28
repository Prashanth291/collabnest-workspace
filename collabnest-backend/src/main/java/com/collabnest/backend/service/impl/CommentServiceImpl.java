package com.collabnest.backend.service.impl;

import com.collabnest.backend.domain.entity.Comment;
import com.collabnest.backend.domain.entity.User;
import com.collabnest.backend.repository.CommentRepository;
import com.collabnest.backend.repository.UserRepository;
import com.collabnest.backend.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Comment createComment(UUID entityId, String entityType, String content, UUID createdById) {
        User createdBy = userRepository.findById(createdById)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Comment comment = Comment.builder()
                .entityId(entityId)
                .entityType(entityType)
                .content(content)
                .createdBy(createdBy)
                .build();
        
        return commentRepository.save(comment);
    }

    @Override
    public Comment getComment(UUID commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
    }

    @Override
    public List<Comment> getEntityComments(UUID entityId, String entityType) {
        return commentRepository.findByEntityIdAndEntityTypeOrderByCreatedAtDesc(entityId, entityType);
    }

    @Override
    @Transactional
    public void deleteComment(UUID commentId) {
        Comment comment = getComment(commentId);
        commentRepository.delete(comment);
    }
}
