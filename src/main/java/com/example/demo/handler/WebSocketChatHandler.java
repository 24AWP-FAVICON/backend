package com.example.demo.handler;

import com.example.demo.entity.messenger.ChatMessage;
import com.example.demo.entity.messenger.MessageType;
import com.example.demo.repository.messenger.ChatMessageRepository;
import com.example.demo.service.messenger.ChatRoomService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class WebSocketChatHandler extends TextWebSocketHandler {
    private final Map<Long, Map<String, WebSocketSession>> chatRooms = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ChatRoomService chatRoomService;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        ChatMessage chatMessage = objectMapper.readValue(message.getPayload(), ChatMessage.class);

        if (chatMessage.getType() == MessageType.READ) {
            // 메시지 읽기 처리
            ChatMessage existingMessage = chatMessageRepository.findById(chatMessage.getMessageId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid message ID"));
            int unreadCount = existingMessage.getUnreadCount();
            if (unreadCount > 0) {
                existingMessage.setUnreadCount(unreadCount - 1);
                chatMessageRepository.save(existingMessage);
            }
        } else {
            // 메시지 전송 처리
            Long roomId = chatMessage.getRoom().getRoomId();
            for (WebSocketSession webSocketSession : chatRooms.get(roomId).values()) {
                if (webSocketSession.isOpen()) {
                    webSocketSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(chatMessage)));
                }
            }
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // You can add logic to identify and add the session to a chat room
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // You can add logic to remove the session from a chat room
    }

    public void addSessionToRoom(Long roomId, String userId, WebSocketSession session) {
        chatRooms.computeIfAbsent(roomId, k -> new HashMap<>()).put(userId, session);
    }

    public void removeSessionFromRoom(Long roomId, String userId) {
        if (chatRooms.containsKey(roomId)) {
            chatRooms.get(roomId).remove(userId);
        }
    }
}
