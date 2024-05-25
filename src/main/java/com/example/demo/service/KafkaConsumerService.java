package com.example.demo.service;

import com.example.demo.entity.Message;
import com.example.demo.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class KafkaConsumerService {
    @Autowired
    private MessageRepository messageRepository;

    @KafkaListener(topics = "chat-messages", groupId = "messenger-group")
    public void consumeMessage(String message) {
        System.out.println("Consumed message: " + message);
        // 메시지를 데이터베이스에 저장
        Message msg = new Message();
        msg.setContent(message);
        msg.setSendAt(LocalDateTime.now());
        msg.setUnreadCount(0); // 초기 값으로 설정
        messageRepository.save(msg);
    }

}
