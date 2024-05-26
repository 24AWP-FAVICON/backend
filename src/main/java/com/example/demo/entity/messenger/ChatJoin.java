    package com.example.demo.entity.messenger;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@IdClass(ChatJoinId.class)
public class ChatJoin {
    @Id
    private String userId;

    @Id
    private Long roomId;

    private int msgCount;

    // Constructors, getters, and setters
    public ChatJoin(String userId, Long roomId) {
        this.userId = userId;
        this.roomId = roomId;
        this.msgCount = 0;
    }
}

