package com.collabnest.backend.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "direct_chats", uniqueConstraints = @UniqueConstraint(columnNames = { "user_one_id", "user_two_id" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DirectChat {

    @Id
    @Column(name = "chat_id")
    private UUID chatId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "chat_id")
    private Chat chat;

    @ManyToOne
    @JoinColumn(name = "user_one_id", nullable = false)
    private User userOne;

    @ManyToOne
    @JoinColumn(name = "user_two_id", nullable = false)
    private User userTwo;
}
