package com.collabnest.backend.service.impl;

import com.collabnest.backend.domain.entity.*;
import com.collabnest.backend.domain.enums.ChatType;
import com.collabnest.backend.domain.enums.LinkedEntityType;
import com.collabnest.backend.exception.ResourceNotFoundException;
import com.collabnest.backend.repository.*;
import com.collabnest.backend.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRepository chatRepository;
    private final WorkspaceChatRepository workspaceChatRepository;
    private final DirectChatRepository directChatRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public WorkspaceChat getOrCreateWorkspaceChat(UUID workspaceId) {
        return workspaceChatRepository.findByWorkspaceId(workspaceId)
                .orElseGet(() -> {
                    Workspace workspace = workspaceRepository.findById(workspaceId)
                            .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));

                    Chat chat = Chat.builder()
                            .chatType(ChatType.WORKSPACE)
                            .build();
                    chat = chatRepository.save(chat);

                    WorkspaceChat workspaceChat = WorkspaceChat.builder()
                            .chat(chat)
                            .workspace(workspace)
                            .build();
                    return workspaceChatRepository.save(workspaceChat);
                });
    }

    @Override
    @Transactional
    public DirectChat getOrCreateDirectChat(UUID userOneId, UUID userTwoId) {
        // Ensure consistent ordering (user_one_id < user_two_id per DB constraint)
        UUID smallerId = userOneId.compareTo(userTwoId) < 0 ? userOneId : userTwoId;
        UUID largerId = userOneId.compareTo(userTwoId) < 0 ? userTwoId : userOneId;

        return directChatRepository.findByUsers(smallerId, largerId)
                .orElseGet(() -> {
                    User userOne = userRepository.findById(smallerId)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                    User userTwo = userRepository.findById(largerId)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                    Chat chat = Chat.builder()
                            .chatType(ChatType.DIRECT)
                            .build();
                    chat = chatRepository.save(chat);

                    DirectChat directChat = DirectChat.builder()
                            .chat(chat)
                            .userOne(userOne)
                            .userTwo(userTwo)
                            .build();
                    return directChatRepository.save(directChat);
                });
    }

    @Override
    @Transactional
    public ChatMessage sendMessage(UUID chatId, UUID senderId, String content,
            LinkedEntityType linkedEntityType, UUID linkedEntityId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ChatMessage message = ChatMessage.builder()
                .chat(chat)
                .sender(sender)
                .content(content)
                .linkedEntityType(linkedEntityType != null ? linkedEntityType : LinkedEntityType.NONE)
                .linkedEntityId(linkedEntityId)
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(message);

        // Broadcast via WebSocket
        messagingTemplate.convertAndSend("/topic/chat/" + chatId, savedMessage);

        return savedMessage;
    }

    @Override
    public Page<ChatMessage> getChatMessages(UUID chatId, Pageable pageable) {
        return chatMessageRepository.findByChatIdOrderByCreatedAtDesc(chatId, pageable);
    }

    @Override
    public List<DirectChat> getUserDirectChats(UUID userId) {
        return directChatRepository.findByUserId(userId);
    }
}
