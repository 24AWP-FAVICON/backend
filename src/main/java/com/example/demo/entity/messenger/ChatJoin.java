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
    private Long userId;

    @Id
    private Long roomId;

    private int msgCount;

}

