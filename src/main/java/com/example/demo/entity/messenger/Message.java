package com.example.demo.entity.messenger;

import com.example.demo.entity.users.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long messageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private ChatRoom room;

    private String content;

    private LocalDateTime sendAt;

    private int unreadCount;
}
