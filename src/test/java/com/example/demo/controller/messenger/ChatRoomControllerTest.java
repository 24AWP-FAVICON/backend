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
        chatRoomResponseDTO = new ChatRoomResponseDTO(1L, "Test Room", null);

        Authentication auth = Mockito.mock(Authentication.class);
        SecurityContextHolder.setContext(new SecurityContextImpl(auth));
    }

    @Test
    @DisplayName("createChatRoom 성공 - 새로운 채팅방을 생성할 수 있다")
    void createChatRoom_success() throws Exception {
        ChatRoomRequestDTO.CreateDTO requestDTO = ChatRoomRequestDTO.CreateDTO.builder()
                .name("Test Room")
                .creatorUserId("user1")
                .participantIds(List.of("user1", "user2"))
                .build();

        Mockito.when(chatRoomService.createChatRoom(any(ChatRoomRequestDTO.CreateDTO.class)))
                .thenReturn(chatRoomResponseDTO);

        mockMvc.perform(post("/messenger/chatRoom")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Test Room\", \"creatorUserId\": \"user1\", \"participantIds\": [\"user1\", \"user2\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Room"));
    }

    @Test
    @DisplayName("createChatRoom 실패 - 채팅방 생성 중 예외 발생 시 500 응답")
    void createChatRoom_fail() throws Exception {
        Mockito.when(chatRoomService.createChatRoom(any(ChatRoomRequestDTO.CreateDTO.class)))
                .thenThrow(new RuntimeException("Error creating chat room"));

        mockMvc.perform(post("/messenger/chatRoom")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Test Room\", \"creatorUserId\": \"user1\", \"participantIds\": [\"user1\", \"user2\"]}"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("getAllChatRooms 성공 - 사용자가 참여한 모든 채팅방을 조회할 수 있다")
    void getAllChatRooms_success() throws Exception {User mockUser = new User();
        mockUser.setUserId("user1");

        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.getPrincipal()).thenReturn(mockUser);

        Mockito.when(chatRoomService.findAllChatRoomsByUserId(any(String.class)))
                .thenReturn(List.of(new ChatRoomResponseDTO(1L, "Test Room", LocalDateTime.now())));

        mockMvc.perform(get("/messenger/chatRooms")
                        .principal(authentication)  // Authentication 객체 추가
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Room"));
    }

    @Test
    @DisplayName("getAllChatRooms 실패 - 채팅방 조회 중 예외 발생 시 500 응답")
    void getAllChatRooms_fail() throws Exception {
        User mockUser = new User();
        mockUser.setUserId("user1");

        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.getPrincipal()).thenReturn(mockUser);

        Mockito.when(chatRoomService.findAllChatRoomsByUserId(any(String.class)))
                .thenThrow(new RuntimeException("Error retrieving chat rooms"));

        mockMvc.perform(get("/messenger/chatRooms")
                        .principal(authentication)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("getChatRoomById 성공 - 특정 채팅방을 조회할 수 있다")
    void getChatRoomById_success() throws Exception {
        Mockito.when(chatRoomService.findChatRoomById(anyLong()))
                .thenReturn(chatRoomResponseDTO);

        mockMvc.perform(get("/messenger/chatRoom/{roomId}", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Room"));
    }

    @Test
    @DisplayName("getChatRoomById 실패 - 특정 채팅방 조회 중 예외 발생 시 500 응답")
    void getChatRoomById_fail() throws Exception {
        Mockito.when(chatRoomService.findChatRoomById(anyLong()))
                .thenThrow(new RuntimeException("Error retrieving chat room"));

        mockMvc.perform(get("/messenger/chatRoom/{roomId}", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("getChatRoomMessagesById 성공 - 특정 채팅방의 메시지를 조회할 수 있다")
    void getChatRoomMessagesById_success() throws Exception {
        ChatMessageDTO messageDTO = new ChatMessageDTO(1L, "user1", 1L, "Hello", LocalDateTime.now(), 0);

        Mockito.when(chatRoomService.getChatMessagesByRoomId(anyLong()))
                .thenReturn(Collections.singletonList(messageDTO));

        mockMvc.perform(get("/messenger/chatRoom/{roomId}/messages", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].senderId").value("user1"))
                .andExpect(jsonPath("$[0].content").value("Hello"));
    }

    @Test
    @DisplayName("getChatRoomMessagesById 실패 - 메시지 조회 중 예외 발생 시 500 응답")
    void getChatRoomMessagesById_fail() throws Exception {
        Mockito.when(chatRoomService.getChatMessagesByRoomId(anyLong()))
                .thenThrow(new RuntimeException("Error retrieving messages"));

        mockMvc.perform(get("/messenger/chatRoom/{roomId}/messages", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("updateChatRoomName 성공 - 채팅방 이름을 수정할 수 있다")
    void updateChatRoomName_success() throws Exception {
        Mockito.doNothing().when(chatRoomService).updateChatRoomName(anyLong(), any(String.class));

        mockMvc.perform(put("/messenger/chatRoom/{roomId}/name", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Updated Room\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("updateChatRoomName 실패 - 채팅방 이름 수정 중 예외 발생 시 500 응답")
    void updateChatRoomName_fail() throws Exception {
        Mockito.doThrow(new RuntimeException("Error updating chat room name"))
                .when(chatRoomService).updateChatRoomName(anyLong(), any(String.class));

        mockMvc.perform(put("/messenger/chatRoom/{roomId}/name", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Updated Room\"}"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("inviteUserToChatRoom 성공 - 특정 채팅방에 사용자를 초대할 수 있다")
    void inviteUserToChatRoom_success() throws Exception {
        Mockito.doNothing().when(chatRoomService).inviteUserToChatRoom(anyLong(), any(ChatRoomRequestDTO.InviteDTO.class));

        String inviteJson = "{ \"inviteUserId\": \"user2\" }";

        mockMvc.perform(post("/messenger/chatRoom/{roomId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(inviteJson))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("inviteUserToChatRoom 실패 - 초대 중 예외 발생 시 500 응답")
    void inviteUserToChatRoom_fail() throws Exception {
        Mockito.doThrow(new RuntimeException("Error inviting user"))
                .when(chatRoomService).inviteUserToChatRoom(anyLong(), any(ChatRoomRequestDTO.InviteDTO.class));

        String inviteJson = "{ \"inviteUserId\": \"user2\" }";

        mockMvc.perform(post("/messenger/chatRoom/{roomId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(inviteJson))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("leaveChatRoom 성공 - 사용자가 채팅방에서 나갈 수 있다")
    void leaveChatRoom_success() throws Exception {
        User mockUser = new User();
        mockUser.setUserId("user1");

        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.getPrincipal()).thenReturn(mockUser);

        Mockito.doNothing().when(chatRoomService).leaveChatRoom(anyLong(), any(String.class));

        mockMvc.perform(delete("/messenger/chatRoom/{roomId}", 1L)
                        .principal(authentication))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("leaveChatRoom 실패 - 나가는 중 예외 발생 시 500 응답")
    void leaveChatRoom_fail() throws Exception {
        User mockUser = new User();
        mockUser.setUserId("user1");

        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.getPrincipal()).thenReturn(mockUser);

        Mockito.doThrow(new RuntimeException("Error leaving chat room"))
                .when(chatRoomService).leaveChatRoom(anyLong(), any(String.class));

        mockMvc.perform(delete("/messenger/chatRoom/{roomId}", 1L)
                        .principal(authentication))
                .andExpect(status().isInternalServerError());
    }

}
