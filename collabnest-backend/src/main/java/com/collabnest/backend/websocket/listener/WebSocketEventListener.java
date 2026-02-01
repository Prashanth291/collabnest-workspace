package com.collabnest.backend.websocket.listener;

import com.collabnest.backend.security.UserPrincipal;
import com.collabnest.backend.websocket.dto.PresenceEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = headerAccessor.getUser();
        
        if (user instanceof UserPrincipal userPrincipal) {
            log.info("User connected: {}", userPrincipal.getUsername());
            
            // Broadcast user online event
            PresenceEvent presenceEvent = PresenceEvent.builder()
                    .type(PresenceEvent.EventType.USER_ONLINE)
                    .userId(userPrincipal.getUserId())
                    .userName(userPrincipal.getUsername())
                    .timestamp(LocalDateTime.now())
                    .build();
            
            // Broadcast to all workspaces (in production, you'd track which workspaces the user is in)
            messagingTemplate.convertAndSend("/topic/presence", presenceEvent);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = headerAccessor.getUser();
        
        if (user instanceof UserPrincipal userPrincipal) {
            log.info("User disconnected: {}", userPrincipal.getUsername());
            
            // Broadcast user offline event
            PresenceEvent presenceEvent = PresenceEvent.builder()
                    .type(PresenceEvent.EventType.USER_OFFLINE)
                    .userId(userPrincipal.getUserId())
                    .userName(userPrincipal.getUsername())
                    .timestamp(LocalDateTime.now())
                    .build();
            
            // Broadcast to all workspaces
            messagingTemplate.convertAndSend("/topic/presence", presenceEvent);
        }
    }
}
