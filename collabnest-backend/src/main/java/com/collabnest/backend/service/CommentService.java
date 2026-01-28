package com.collabnest.backend.service;

import com.collabnest.backend.domain.entity.Comment;

import java.util.List;
import java.util.UUID;

public interface CommentService {

    Comment createComment(UUID entityId, String entityType, String content, UUID createdById);

    Comment getComment(UUID commentId);

    List<Comment> getEntityComments(UUID entityId, String entityType);

    void deleteComment(UUID commentId);
}
