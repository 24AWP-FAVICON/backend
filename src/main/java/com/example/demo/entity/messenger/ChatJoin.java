package com.example.demo.entity.messenger;

import com.example.demo.entity.users.user.User;
import jakarta.persistence.*;
import lombok.*;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roomId", insertable = false, updatable = false)
    private ChatRoom room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    private User user;

    public ChatJoin(String userId, Long roomId) {
        this.userId = userId;
        this.roomId = roomId;
    }
}
