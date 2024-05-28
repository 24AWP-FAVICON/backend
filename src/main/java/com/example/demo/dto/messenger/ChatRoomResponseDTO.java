package com.example.demo.dto.messenger;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ChatRoomResponseDTO {
    private Long roomId;
    private String name;
    private LocalDateTime createAt;
    private List<String> users; // 사용자 ID 목록

    public ChatRoomResponseDTO(Long roomId, String name, LocalDateTime createAt) {
        this.roomId = roomId;
        this.name = name;
        this.createAt = createAt;
    }
}