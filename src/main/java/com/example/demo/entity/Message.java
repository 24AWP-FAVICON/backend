package com.example.demo.entity;

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
    private User user;

    private String content;

    private LocalDateTime sendAt;

    private Long unreadCount;
}
