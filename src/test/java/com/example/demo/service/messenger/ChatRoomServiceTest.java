package com.example.demo.service.messenger;

import com.example.demo.dto.messenger.ChatRoomRequestDTO;
import com.example.demo.dto.messenger.ChatRoomResponseDTO;
import com.example.demo.entity.messenger.ChatJoin;
import com.example.demo.entity.messenger.ChatRoom;
import com.example.demo.entity.users.user.Role;
import com.example.demo.entity.users.user.User;
import com.example.demo.repository.messenger.ChatJoinRepository;
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
    @DisplayName("ChatRoom 생성 성공 테스트")
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
    @DisplayName("사용자가 참여한 모든 채팅방 조회 성공 테스트")
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
    @DisplayName("ChatRoom 정보 조회 성공 테스트")
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
    @DisplayName("ChatRoom에 사용자 초대 성공 테스트")
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
    @DisplayName("ChatRoom 나가기 성공 테스트")
    void leaveChatRoom_success() {
        // Given
        ChatJoin chatJoin = new ChatJoin("user1", 1L);

        when(chatJoinRepository.findByRoomIdAndUserId(1L, "user1")).thenReturn(Optional.of(chatJoin));

        // When
        chatRoomService.leaveChatRoom(1L, "user1");

        // Then
        verify(chatJoinRepository, times(1)).delete(chatJoin);
    }
}
