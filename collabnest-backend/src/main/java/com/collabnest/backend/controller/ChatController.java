package com.collabnest.backend.controller;

import com.collabnest.backend.domain.entity.ChatMessage;
import com.collabnest.backend.domain.entity.DirectChat;
import com.collabnest.backend.domain.entity.WorkspaceChat;
import com.collabnest.backend.domain.enums.ChatType;
import com.collabnest.backend.dto.chat.ChatMessageResponse;
import com.collabnest.backend.dto.chat.ChatResponse;
import com.collabnest.backend.dto.chat.SendMessageRequest;
import com.collabnest.backend.security.UserPrincipal;
import com.collabnest.backend.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * Get or create a workspace chat.
     */
    @PostMapping("/workspace/{workspaceId}")
    @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'MEMBER')")
    public ResponseEntity<ChatResponse> getWorkspaceChat(@PathVariable UUID workspaceId) {
        WorkspaceChat wc = chatService.getOrCreateWorkspaceChat(workspaceId);
        ChatResponse response = new ChatResponse(
                wc.getChat().getId(),
                ChatType.WORKSPACE,
                workspaceId,
                null,
                null,
                wc.getChat().getCreatedAt());
        return ResponseEntity.ok(response);
    }

    /**
     * Get or create a direct chat with another user.
     */
    @PostMapping("/direct/{otherUserId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ChatResponse> getDirectChat(
            @PathVariable UUID otherUserId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        DirectChat dc = chatService.getOrCreateDirectChat(userPrincipal.getUserId(), otherUserId);
        UUID otherUser = dc.getUserOne().getId().equals(userPrincipal.getUserId())
                ? dc.getUserTwo().getId()
                : dc.getUserOne().getId();
        String otherUsername = dc.getUserOne().getId().equals(userPrincipal.getUserId())
                ? dc.getUserTwo().getUsername()
                : dc.getUserOne().getUsername();
        ChatResponse response = new ChatResponse(
                dc.getChat().getId(),
                ChatType.DIRECT,
                null,
                otherUser,
                otherUsername,
                dc.getChat().getCreatedAt());
        return ResponseEntity.ok(response);
    }

    /**
     * Get all direct chats for the authenticated user.
     */
    @GetMapping("/direct")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ChatResponse>> getMyDirectChats(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<DirectChat> chats = chatService.getUserDirectChats(userPrincipal.getUserId());
        List<ChatResponse> responses = chats.stream()
                .map(dc -> {
                    UUID otherUser = dc.getUserOne().getId().equals(userPrincipal.getUserId())
                            ? dc.getUserTwo().getId()
                            : dc.getUserOne().getId();
                    String otherUsername = dc.getUserOne().getId().equals(userPrincipal.getUserId())
                            ? dc.getUserTwo().getUsername()
                            : dc.getUserOne().getUsername();
                    return new ChatResponse(
                            dc.getChat().getId(),
                            ChatType.DIRECT,
                            null,
                            otherUser,
                            otherUsername,
                            dc.getChat().getCreatedAt());
                })
                .toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * Send a message in a chat.
     */
    @PostMapping("/{chatId}/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ChatMessageResponse> sendMessage(
            @PathVariable UUID chatId,
            @Valid @RequestBody SendMessageRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        ChatMessage msg = chatService.sendMessage(
                chatId,
                userPrincipal.getUserId(),
                request.content(),
                request.linkedEntityType(),
                request.linkedEntityId());
        return ResponseEntity.ok(toMessageResponse(msg));
    }

    /**
     * Get messages for a chat with pagination.
     */
    @GetMapping("/{chatId}/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<ChatMessageResponse>> getChatMessages(
            @PathVariable UUID chatId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ChatMessageResponse> messages = chatService.getChatMessages(chatId, pageable)
                .map(this::toMessageResponse);
        return ResponseEntity.ok(messages);
    }

    private ChatMessageResponse toMessageResponse(ChatMessage msg) {
        return new ChatMessageResponse(
                msg.getId(),
                msg.getChat().getId(),
                msg.getSender().getId(),
                msg.getSender().getUsername(),
                msg.getContent(),
                msg.getLinkedEntityType(),
                msg.getLinkedEntityId(),
                msg.getCreatedAt());
    }
}
