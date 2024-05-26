package com.example.demo.handler;

import com.example.demo.entity.messenger.ChatMessage;
import com.example.demo.service.messenger.ChatRoomService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
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

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        ChatMessage chatMessage = objectMapper.readValue(message.getPayload(), ChatMessage.class);
        Long roomId = chatMessage.getRoom().getRoomId();

        // Save message to the database
        ChatMessage savedMessage = chatRoomService.saveMessage(roomId, chatMessage.getUser().getUserId(), chatMessage.getContent());

        if (chatRooms.containsKey(roomId)) {
            for (WebSocketSession webSocketSession : chatRooms.get(roomId).values()) {
                if (webSocketSession.isOpen()) {
                    webSocketSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(savedMessage)));
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
