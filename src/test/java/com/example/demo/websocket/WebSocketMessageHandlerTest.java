package com.example.demo.websocket;

import com.example.demo.dto.messenger.ChatMessageDTO;
import com.example.demo.entity.messenger.ChatMessage;
import com.example.demo.entity.messenger.ChatRoom;
import com.example.demo.entity.users.user.User;
import com.example.demo.service.messenger.ChatMessageService;
import com.example.demo.service.messenger.ChatRoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;

class WebSocketMessageHandlerTest {

    @Mock
    private ChatRoomService chatRoomService;

    @Mock
    private ChatMessageService chatMessageService;

    @Mock
    private SimpMessageSendingOperations simpMessageSendingOperations;

    @InjectMocks
    private WebSocketMessageHandler webSocketMessageHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    @DisplayName("WebSocket 메시지 처리 테스트 - 사용자가 방에 있을 때")
    void handleMessage_success() {
        // Given
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setRoomId(1L);

        User user = new User();
        user.setUserId("test_user");

        ChatMessage message = ChatMessage.builder()
                .content("Hello!")
                .room(chatRoom)
                .user(user)
                .build();

        when(chatRoomService.isUserInChatRoom(1L, "test_user")).thenReturn(true);
        when(chatMessageService.saveMessage(any(ChatMessage.class))).thenReturn(message);

        // When
        webSocketMessageHandler.handleMessage(message);

        // ArgumentCaptor를 사용하여 전달된 객체 캡처
        ArgumentCaptor<ChatMessageDTO> messageDTOCaptor = ArgumentCaptor.forClass(ChatMessageDTO.class);
        verify(simpMessageSendingOperations).convertAndSend(eq("/sub/channel/1"), messageDTOCaptor.capture());

        // Then
        ChatMessageDTO capturedMessageDTO = messageDTOCaptor.getValue();
        assertEquals("Hello!", capturedMessageDTO.getContent());
        assertEquals("test_user", capturedMessageDTO.getSenderId());
        assertEquals(1L, capturedMessageDTO.getRoomId());
    }


    @Test
    @DisplayName("WebSocket 메시지 처리 실패 테스트 - 사용자가 방에 없을 때")
    void handleMessage_fail() {
        // Given: 가짜 메시지 데이터 설정
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setRoomId(1L);

        User user = new User();
        user.setUserId("test_user");

        ChatMessage message = ChatMessage.builder()
                .content("Hello!")
                .room(chatRoom)
                .user(user)
                .build();

        when(chatRoomService.isUserInChatRoom(1L, "test_user")).thenReturn(false);

        // When: 메시지 처리
        webSocketMessageHandler.handleMessage(message);

        // Then: SimpMessageSendingOperations이 호출되지 않는지 확인
        verify(simpMessageSendingOperations, times(0))
                .convertAndSend(anyString(), any(ChatMessageDTO.class));
    }
}
