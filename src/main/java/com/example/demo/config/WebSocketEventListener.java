package com.example.demo.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketEventListener {
    // 추후 RabbitMQ 도입 시 사용할 부분
//
//    private final SimpMessageSendingOperations messagingTemplate;
//    private final UserRepository userRepository;
//
//    @EventListener
//    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
//        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
//        String username = (String) headerAccessor.getSessionAttributes().get("username");
//        if (username != null) {
//            log.info("User Disconnected: {}", username);
//
//            User user = userRepository.findById(username).orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));
//
//            ChatMessage chatMessage = ChatMessage.builder()
//                    .type(MessageType.LEAVE)
//                    .user(user)
//                    .build();
//            messagingTemplate.convertAndSend("/topic/public", chatMessage);
//        }
//    }
}
