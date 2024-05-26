package com.example.demo.dto.messenger;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ChatRoomResponseDTO {

    private Long roomId;
    private String name;
    private LocalDateTime createdAt;

}
