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

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class WebSocketChatHandler extends TextWebSocketHandler {
    private final Map<Long, Map<String, WebSocketSession>> chatRooms = new ConcurrentHashMap<>();
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
            String id = session.getId();  // 메시지를 보낸 아이디
            chatRooms.get(roomId).forEach((userId, webSocketSession) -> {
                if (!webSocketSession.getId().equals(id)) {  // 같은 아이디가 아니면 메시지를 전달합니다.
                    try {
                        webSocketSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(chatMessage)));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("WebSocket connection established: " + session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("WebSocket connection closed: " + session.getId());
        chatRooms.values().forEach(room -> room.values().remove(session));
    }

    public void addSessionToRoom(Long roomId, String userId, WebSocketSession session) {
        chatRooms.computeIfAbsent(roomId, k -> new ConcurrentHashMap<>()).put(userId, session);
    }

    public void removeSessionFromRoom(Long roomId, String userId) {
        if (chatRooms.containsKey(roomId)) {
            chatRooms.get(roomId).remove(userId);
        }
    }

    private void broadcastMessage(Long roomId, String senderId, TextMessage message) {
        chatRooms.getOrDefault(roomId, new ConcurrentHashMap<>())
                .forEach((userId, webSocketSession) -> {
                    if (!webSocketSession.getId().equals(senderId)) {
                        try {
                            webSocketSession.sendMessage(message);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
}
