package com.example.demo.entity.messenger;

import com.example.demo.entity.planner.Trip;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomId;

    private String name;

    private LocalDateTime createAt;

    // 이름 입력 constructor
    public ChatRoom(String name) {
        this.name = name;
    }
}

