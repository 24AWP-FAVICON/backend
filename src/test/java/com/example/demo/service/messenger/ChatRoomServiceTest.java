package com.example.demo.service.messenger;

import com.example.demo.dto.messenger.ChatMessageDTO;
import com.example.demo.dto.messenger.ChatRoomRequestDTO;
import com.example.demo.dto.messenger.ChatRoomResponseDTO;
import com.example.demo.entity.messenger.ChatJoin;
import com.example.demo.entity.messenger.ChatMessage;
import com.example.demo.entity.messenger.ChatRoom;
import com.example.demo.entity.users.user.Role;
import com.example.demo.entity.users.user.User;
import com.example.demo.repository.messenger.ChatJoinRepository;
import com.example.demo.repository.messenger.ChatMessageRepository;
import com.example.demo.repository.messenger.ChatRoomRepository;
import com.example.demo.repository.users.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ChatRoomServiceTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;


    @Mock
    private ChatJoinRepository chatJoinRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ChatRoomService chatRoomService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        User user1 = new User();
        user1.setUserId("minbory925@gmail.com");
        user1.setNickname("Minjeong");
        user1.setRole(Role.ROLE_USER);
        user1.setRecentConnect(LocalDateTime.now());
        user1.setCreatedAt(LocalDate.now());

        User user2 = new User();
        user2.setUserId("deepdevming@gmail.com");
        user2.setNickname("Mingguriguri");
        user2.setRole(Role.ROLE_USER);
        user2.setRecentConnect(LocalDateTime.now());
        user2.setCreatedAt(LocalDate.now());

        // 유저가 userRepository에 요청되었을 때 반환되도록 설정
        when(userRepository.findById("minbory925@gmail.com")).thenReturn(Optional.of(user1));
        when(userRepository.findById("deepdevming@gmail.com")).thenReturn(Optional.of(user2));

    }

    @Test
    @DisplayName("createChatRoom 성공 - 사용자는 채팅방을 생성할 수 있다")
    void createChatRoom_success() {
        // Given: 채팅방 생성 DTO 설정
        ChatRoomRequestDTO.CreateDTO requestDTO = ChatRoomRequestDTO.CreateDTO.builder()
                .name("Test Room")
                .creatorUserId("minbory925@gmail.com")
                .participantIds(Arrays.asList("minbory925@gmail.com", "deepdevming@gmail.com"))
                .build();

        // ChatRoom 엔티티 설정 (roomId는 null로 시작)
        ChatRoom chatRoom = ChatRoom.builder()
                .name("Test Room")
                .createAt(LocalDateTime.now())
                .build();

        // Mock 동작 설정: ChatRoom 저장 시 roomId가 자동 생성된 것처럼 동작
        when(chatRoomRepository.save(any(ChatRoom.class))).thenAnswer(invocation -> {
            ChatRoom savedRoom = invocation.getArgument(0);
            savedRoom.setRoomId(1L);  // roomId가 자동 생성된 것처럼 설정
            return savedRoom;
        });

        // Mock 동작 설정: UserRepository에서 User 객체 반환
        when(userRepository.findById(anyString())).thenReturn(Optional.of(mock(com.example.demo.entity.users.user.User.class)));

        // When: 서비스 호출하여 채팅방 생성
        ChatRoomResponseDTO responseDTO = chatRoomService.createChatRoom(requestDTO);

        // Then: 생성된 결과 검증
        assertNotNull(responseDTO);
        assertEquals("Test Room", responseDTO.getName());
        assertEquals(1L, responseDTO.getRoomId());  // 자동 생성된 roomId를 확인
        assertEquals(2, responseDTO.getUsers().size());
    }


    @Test
    @DisplayName("findAllChatRoomsByUserId 성공 - 사용자가 참여한 모든 채팅방 조회할 수 있다")
    void findAllChatRoomsByUserId_success() {
        // Given
        ChatJoin chatJoin = new ChatJoin("user1", 1L);
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setRoomId(1L);
        chatRoom.setName("Test Room");

        when(chatJoinRepository.findAllByUserId("user1")).thenReturn(Collections.singletonList(chatJoin));
        when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(chatRoom));

        // When
        List<ChatRoomResponseDTO> responseDTOList = chatRoomService.findAllChatRoomsByUserId("user1");

        // Then
        assertNotNull(responseDTOList);
        assertEquals(1, responseDTOList.size());
        assertEquals("Test Room", responseDTOList.get(0).getName());
    }

    @Test
    @DisplayName("findChatRoomById - 사용자는 특정 채팅방의 정보를 조회할 수 있다")
    void findChatRoomById_success() {
        // Given
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setRoomId(1L);
        chatRoom.setName("Test Room");

        when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(chatRoom));

        // When
        ChatRoomResponseDTO responseDTO = chatRoomService.findChatRoomById(1L);

        // Then
        assertNotNull(responseDTO);
        assertEquals("Test Room", responseDTO.getName());
    }

    @Test
    @DisplayName("getChatMessagesByRoomId 성공 - 특정 채팅방의 메시지를 조회할 수 있다")
    void getChatMessagesByRoomId_success() {
        // Given: ChatMessage 리스트 생성
        User user1 = new User();
        ChatRoom room = new ChatRoom();
        ChatMessage message1 = new ChatMessage(1L, user1, room, "Hello", LocalDateTime.now(), 0);
        ChatMessage message2 = new ChatMessage(2L, user1, room, "Hi", LocalDateTime.now(), 0);

        // Mock 설정: 채팅방 ID에 따른 메시지 반환
        when(chatMessageRepository.findByRoom_RoomId(1L)).thenReturn(Arrays.asList(message1, message2));

        // When: 서비스 호출
        List<ChatMessageDTO> result = chatRoomService.getChatMessagesByRoomId(1L);

        // Then: 메시지 리스트 검증
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Hello", result.get(0).getContent());
        assertEquals("Hi", result.get(1).getContent());
    }


    @Test
    @DisplayName("inviteUserToChatRoom 성공 - 채팅방에 다른 사용자를 초대할 수 있다")
    void inviteUserToChatRoom_success() {
        // Given
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setRoomId(1L);
        chatRoom.setName("Test Room");

        ChatRoomRequestDTO.InviteDTO inviteDTO = new ChatRoomRequestDTO.InviteDTO("user2");

        when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(chatRoom));
        when(userRepository.findById("user2")).thenReturn(Optional.of(mock(com.example.demo.entity.users.user.User.class)));

        // When
        chatRoomService.inviteUserToChatRoom(1L, inviteDTO);

        // Then
        verify(chatJoinRepository, times(1)).save(any(ChatJoin.class));
    }

    @Test
    @DisplayName("leaveChatRoom 성공 - 사용자는 채팅방을 나갈 수 있다")
    void leaveChatRoom_success() {
        // Given
        ChatJoin chatJoin = new ChatJoin("user1", 1L);

        when(chatJoinRepository.findByRoomIdAndUserId(1L, "user1")).thenReturn(Optional.of(chatJoin));

        // When
        chatRoomService.leaveChatRoom(1L, "user1");

        // Then
        verify(chatJoinRepository, times(1)).delete(chatJoin);
    }

    @Test
    @DisplayName("updateChatRoomName 성공 - 채팅방 이름을 수정할 수 있다")
    void updateChatRoomName_success() {
        // Given: 기존 ChatRoom 설정
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setRoomId(1L);
        chatRoom.setName("Old Room Name");

        // Mock 설정: 채팅방 찾기 및 저장 동작
        when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(chatRoom));

        // When: 채팅방 이름 수정
        chatRoomService.updateChatRoomName(1L, "New Room Name");

        // Then: 채팅방 이름이 수정되었는지 확인
        assertEquals("New Room Name", chatRoom.getName());
        verify(chatRoomRepository, times(1)).save(chatRoom);
    }


    @Test
    @DisplayName("isUserInChatRoom 성공 - 사용자가 채팅방에 존재하는지 확인할 수 있다")
    void isUserInChatRoom_success() {
        // Mock 설정: 사용자가 채팅방에 존재하는 경우
        when(chatJoinRepository.existsByRoomIdAndUserId(1L, "user1")).thenReturn(true);

        // When: 서비스 호출
        boolean result = chatRoomService.isUserInChatRoom(1L, "user1");

        // Then: 결과 검증
        assertTrue(result);
    }

}
