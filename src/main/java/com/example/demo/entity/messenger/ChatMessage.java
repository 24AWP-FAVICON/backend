package com.example.demo.entity.messenger;

import com.example.demo.entity.users.user.User;
import jakarta.persistence.*;
import lombok.*;
import com.example.demo.dto.messenger.ChatMessageDTO;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@Entity
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long messageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User user; // sender

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom room; // channel Id

    private String content; // data
    private LocalDateTime sendAt;
    private int unreadCount;

    public ChatMessageDTO toDTO() {
        return new ChatMessageDTO(
                this.messageId,
                this.user.getUserId(),
                this.room.getRoomId(),
                this.content,
                this.sendAt,
                this.unreadCount
        );
    }

    public ChatMessage(Long messageId, User user, ChatRoom room, String content, LocalDateTime sendAt, int unreadCount) {
        this.messageId = messageId;
        this.user = user;
        this.room = room;
        this.content = content;
        this.sendAt = sendAt;
        this.unreadCount = unreadCount;
    }


}
