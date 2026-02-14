package com.collabnest.backend.service;

import com.collabnest.backend.domain.entity.ChatMessage;
import com.collabnest.backend.domain.entity.DirectChat;
import com.collabnest.backend.domain.entity.WorkspaceChat;
import com.collabnest.backend.domain.enums.LinkedEntityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ChatService {

    /**
     * Get or create workspace chat for a workspace.
     */
    WorkspaceChat getOrCreateWorkspaceChat(UUID workspaceId);

    /**
     * Get or create a direct chat between two users.
     */
    DirectChat getOrCreateDirectChat(UUID userOneId, UUID userTwoId);

    /**
     * Send a message in a chat.
     */
    ChatMessage sendMessage(UUID chatId, UUID senderId, String content,
            LinkedEntityType linkedEntityType, UUID linkedEntityId);

    /**
     * Get messages for a chat with pagination.
     */
    Page<ChatMessage> getChatMessages(UUID chatId, Pageable pageable);

    /**
     * Get all direct chats for a user.
     */
    List<DirectChat> getUserDirectChats(UUID userId);
}
