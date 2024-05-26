//package com.example.demo.service.kafka;
//
//import com.example.demo.repository.messenger.ChatMessageRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//
//@Service
//public class KafkaConsumerService {
//    @Autowired
//    private ChatMessageRepository chatMessageRepository;
//
//    @KafkaListener(topics = "chat-messages", groupId = "messenger-group")
//    public void consumeMessage(String message) {
//        System.out.println("Consumed message: " + message);
//        // 메시지를 데이터베이스에 저장
//        ChatMessage msg = new ChatMessage();
//        msg.setContent(message);
//        msg.setSendAt(LocalDateTime.now());
//        msg.setUnreadCount(0); // 초기 값으로 설정
//        chatMessageRepository.save(msg);
//    }
//
//}
