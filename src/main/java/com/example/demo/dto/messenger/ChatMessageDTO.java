package com.example.demo.dto.messenger;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ChatMessageDTO {
    private Long messageId;
    private String senderId;
    private Long roomId;
    private String content;
    private LocalDateTime sendAt;
    private int unreadCount;
}
