package com.example.demo.service.messenger;

import com.example.demo.entity.messenger.ChatMessage;
import com.example.demo.repository.messenger.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import java.util.logging.Logger;
@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;

    // 메시지를 DB에 저장
    public ChatMessage saveMessage(ChatMessage message) {
        message.setSendAt(LocalDateTime.now());
        return chatMessageRepository.save(message);
    }

//    private final ChatMessageRepository chatMessageRepository;
//    private final static Logger log = Logger.getLogger(ChatMessageService.class.getName());
//
//    public ChatMessage saveMessage(ChatMessage message) {
//        message.setSendAt(LocalDateTime.now());
//        ChatMessage savedMessage = chatMessageRepository.save(message);
//        log.info("Saved message: " + savedMessage.toString());
//
//        return chatMessageRepository.save(message);
//    }
//
//    public List<ChatMessage> getMessagesByRoomId(Long roomId) {
//        return chatMessageRepository.findByRoom_RoomId(roomId);
//    }
}