package com.collabnest.backend.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "workspace_chats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkspaceChat {

    @Id
    @Column(name = "chat_id")
    private UUID chatId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "chat_id")
    private Chat chat;

    @ManyToOne
    @JoinColumn(name = "workspace_id", nullable = false, unique = true)
    private Workspace workspace;
}
