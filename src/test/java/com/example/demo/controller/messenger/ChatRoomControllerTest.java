package com.example.demo.controller.messenger;

import com.example.demo.dto.messenger.ChatMessageDTO;
import com.example.demo.dto.messenger.ChatRoomRequestDTO;
import com.example.demo.dto.messenger.ChatRoomResponseDTO;
import com.example.demo.entity.users.user.User;
import com.example.demo.service.messenger.ChatRoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ChatRoomController.class, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {com.example.demo.filter.CustomLogoutFilter.class})
}) // CustomLogoutFilter를 테스트에서 제외
@AutoConfigureMockMvc(addFilters = false) // Spring Security 필터 비활성화
class ChatRoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatRoomService chatRoomService;

    private ChatRoomResponseDTO chatRoomResponseDTO;

    @BeforeEach
    void setUp() {
        // 채팅방 생성 시 사용하는 DTO
        chatRoomResponseDTO = new ChatRoomResponseDTO(1L, "Test Room", null);

        // Authentication 모의 설정
        Authentication auth = Mockito.mock(Authentication.class);
        SecurityContextHolder.setContext(new SecurityContextImpl(auth));
    }

    @Test
    @DisplayName("createChatRoom 성공 - 새로운 채팅방을 생성할 수 있다")
    void createChatRoom_success() throws Exception {
        // 채팅방 생성 요청을 위한 DTO 설정
        ChatRoomRequestDTO.CreateDTO requestDTO = ChatRoomRequestDTO.CreateDTO.builder()
                .name("Test Room")
                .creatorUserId("user1")
                .participantIds(List.of("user1", "user2"))
                .build();

        // createChatRoom 메서드가 chatRoomResponseDTO를 반환하도록 목(mock) 설정
        Mockito.when(chatRoomService.createChatRoom(any(ChatRoomRequestDTO.CreateDTO.class)))
                .thenReturn(chatRoomResponseDTO);

        // POST 요청을 통해 채팅방 생성 API를 호출하고 응답을 검증
        mockMvc.perform(post("/messenger/chatRoom")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Test Room\", \"creatorUserId\": \"user1\", \"participantIds\": [\"user1\", \"user2\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Room"));
    }

    @Test
    @DisplayName("getAllChatRooms 성공 - 사용자가 참여한 모든 채팅방을 조회할 수 있다")
    void getAllChatRooms_success() throws Exception {
        // Mock User 객체 생성 및 설정
        User mockUser = new User();
        mockUser.setUserId("user1");

        // Mock Authentication 객체
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.getPrincipal()).thenReturn(mockUser);

        // chatRoomService.findAllChatRoomsByUserId 메서드가 리스트를 반환하도록 목 설정
        Mockito.when(chatRoomService.findAllChatRoomsByUserId(any(String.class)))
                .thenReturn(List.of(new ChatRoomResponseDTO(1L, "Test Room", LocalDateTime.now())));

        // GET 요청을 통해 사용자가 참여한 모든 채팅방을 조회하고 응답을 검증
        mockMvc.perform(get("/messenger/chatRooms")
                        .principal(authentication)  // Authentication 객체 추가
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Room"));
    }


    @Test
    @DisplayName("getChatRoomById 성공 - 특정 채팅방을 조회할 수 있다")
    void getChatRoomById_success() throws Exception {
        // chatRoomService.findChatRoomById 메서드가 chatRoomResponseDTO를 반환하도록 목 설정
        Mockito.when(chatRoomService.findChatRoomById(anyLong()))
                .thenReturn(chatRoomResponseDTO);

        // GET 요청을 통해 특정 채팅방을 조회하고 응답을 검증
        mockMvc.perform(get("/messenger/chatRoom/{roomId}", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Room"));
    }

    @Test
    @DisplayName("getChatRoomMessagesById 성공 - 특정 채팅방의 메시지를 조회할 수 있다")
    void getChatRoomMessagesById_success() throws Exception {
        // 테스트용 메시지 DTO 생성
        ChatMessageDTO messageDTO = new ChatMessageDTO(1L, "user1", 1L, "Hello", LocalDateTime.now(), 0);

        // chatRoomService.getChatMessagesByRoomId 메서드가 메시지를 반환하도록 목 설정
        Mockito.when(chatRoomService.getChatMessagesByRoomId(anyLong()))
                .thenReturn(Collections.singletonList(messageDTO));

        // GET 요청을 통해 특정 채팅방의 메시지를 조회하고 응답을 검증
        mockMvc.perform(get("/messenger/chatRoom/{roomId}/messages", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].senderId").value("user1"))
                .andExpect(jsonPath("$[0].content").value("Hello"));
    }

    @Test
    @DisplayName("updateChatRoomName 성공 - 채팅방 이름을 수정할 수 있다")
    void updateChatRoomName_success() throws Exception {
        // chatRoomService.updateChatRoomName 메서드를 목(mock) 처리하여 아무 동작도 하지 않음
        Mockito.doNothing().when(chatRoomService).updateChatRoomName(anyLong(), any(String.class));

        // PUT 요청을 통해 채팅방 이름을 수정하고 응답을 검증
        mockMvc.perform(put("/messenger/chatRoom/{roomId}/name", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Updated Room\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("inviteUserToChatRoom 성공 - 특정 채팅방에 사용자를 초대할 수 있다")
    void inviteUserToChatRoom_success() throws Exception {
        // 목(mock) 처리
        Mockito.doNothing().when(chatRoomService).inviteUserToChatRoom(anyLong(), any(ChatRoomRequestDTO.InviteDTO.class));

        // 초대할 사용자의 정보가 담긴 JSON 문자열
        String inviteJson = "{ \"inviteUserId\": \"user2\" }";

        // POST 요청을 통해 사용자를 초대하는 API를 호출하고 응답을 검증
        mockMvc.perform(post("/messenger/chatRoom/{roomId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(inviteJson))
                .andExpect(status().isOk());
    }


    @Test
    @DisplayName("leaveChatRoom 성공 - 사용자가 채팅방에서 나갈 수 있다")
    void leaveChatRoom_success() throws Exception {
        // Mock User 객체
        User mockUser = new User();
        mockUser.setUserId("user1");

        // Mock Authentication 객체
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.getPrincipal()).thenReturn(mockUser);

        // leaveChatRoom 메서드를 목(mock) 처리하여 아무 동작도 하지 않음
        Mockito.doNothing().when(chatRoomService).leaveChatRoom(anyLong(), any(String.class));

        // DELETE 요청을 통해 채팅방을 나가는 API를 호출하고 응답을 검증
        mockMvc.perform(delete("/messenger/chatRoom/{roomId}", 1L)
                        .principal(authentication))  // Authentication 객체 추가
                .andExpect(status().isOk());
    }

}
