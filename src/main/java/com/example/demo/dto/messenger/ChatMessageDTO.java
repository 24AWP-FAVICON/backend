package com.example.demo.dto.messenger;

import lombok.*;

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
    private String sendAt;
    private int unreadCount;
}
