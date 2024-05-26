package com.example.demo.service.messenger;

import com.example.demo.entity.messenger.ChatMessage;
import com.example.demo.repository.messenger.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;

    public ChatMessage saveMessage(ChatMessage message) {
        message.setSendAt(LocalDateTime.now());
        return chatMessageRepository.save(message);
    }

    public List<ChatMessage> getMessagesByRoomId(Long roomId) {
        return chatMessageRepository.findByRoom_RoomId(roomId);
    }
}