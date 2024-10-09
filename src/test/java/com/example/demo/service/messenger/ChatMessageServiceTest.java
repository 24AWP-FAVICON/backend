package com.example.demo.service.messenger;

import com.example.demo.entity.messenger.ChatJoin;
import com.example.demo.entity.messenger.ChatMessage;
import com.example.demo.entity.messenger.ChatRoom;
import com.example.demo.entity.messenger.UnreadMember;
import com.example.demo.entity.users.user.User;
import com.example.demo.repository.messenger.ChatJoinRepository;
import com.example.demo.repository.messenger.ChatMessageRepository;
import com.example.demo.repository.messenger.UnreadMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ChatMessageServiceTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private ChatJoinRepository chatJoinRepository;

    @Mock
    private UnreadMemberRepository unreadMemberRepository;

    @InjectMocks
    private ChatMessageService chatMessageService;

    private ChatMessage message;
    private ChatRoom room;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setUserId("user1");

        room = new ChatRoom();
        room.setRoomId(1L);
        room.setName("Test Room");

        message = new ChatMessage();
        message.setMessageId(1L);
        message.setContent("Hello World");
        message.setRoom(room);
        message.setUser(user);
    }

    @Test
    @DisplayName("메시지 저장 성공 테스트")
    void saveMessage_success() {
        // Given: ChatJoin 데이터 설정
        when(chatJoinRepository.countByRoomId(1L)).thenReturn(2); // 두 명의 사용자
        when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(message);
        when(chatJoinRepository.findAllByRoomId(1L)).thenReturn(List.of(new ChatJoin("user2", 1L)));

        // When: 메시지 저장
        ChatMessage savedMessage = chatMessageService.saveMessage(message);

        // Then: 검증
        assertNotNull(savedMessage);
        assertEquals(1L, savedMessage.getMessageId());
        assertEquals("Hello World", savedMessage.getContent());
        assertEquals(1, savedMessage.getUnreadCount());
        verify(unreadMemberRepository, times(1)).save(any(UnreadMember.class));
    }

    @Test
    @DisplayName("메시지 읽음 처리 성공 테스트")
    void markMessagesAsRead_success() {
        // Given: UnreadMember 데이터 설정
        UnreadMember unreadMember = new UnreadMember(1L, "user2", message, user);
        when(unreadMemberRepository.findByRoomIdAndUserId(1L, "user2")).thenReturn(List.of(unreadMember));

        // When: 메시지 읽음 처리
        chatMessageService.markMessagesAsRead(1L, "user2");

        // Then: 검증
        verify(unreadMemberRepository, times(1)).delete(unreadMember);
        verify(chatMessageRepository, times(1)).save(any(ChatMessage.class));
    }
}
