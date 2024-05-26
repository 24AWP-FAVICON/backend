package com.example.demo.repository.messenger;


import com.example.demo.entity.messenger.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage,Long> {
    List<ChatMessage> findByRoomId(Long roomId);

    List<ChatMessage> findByRoomRoomId(Long roomId);
}
