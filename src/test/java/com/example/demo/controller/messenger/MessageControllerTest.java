package com.example.demo.controller.messenger;

import com.example.demo.entity.users.user.User;
import com.example.demo.service.messenger.ChatMessageService;
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

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MessageController.class, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {com.example.demo.filter.CustomLogoutFilter.class})
}) // CustomLogoutFilter를 테스트에서 제외
@AutoConfigureMockMvc(addFilters = false) // 보안 필터 비활성화
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc; // MockMvc를 통해 HTTP 요청을 모의

    @MockBean
    private ChatMessageService chatMessageService; // ChatMessageService를 목(mock) 처리

    private Authentication authentication;

    @BeforeEach
    void setUp() {
        // 테스트 전 Authentication 객체 모킹 및 SecurityContext 설정
        User mockUser = new User();
        mockUser.setUserId("user1");

        authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.getPrincipal()).thenReturn(mockUser);

        SecurityContextHolder.setContext(new SecurityContextImpl(authentication)); // 보안 컨텍스트 설정
    }

    @Test
    @DisplayName("markMessagesAsRead 성공 - 메시지를 읽음으로 표시할 수 있다")
    void markMessagesAsRead_success() throws Exception {
        // 목 처리: chatMessageService의 markMessagesAsRead 메서드가 호출되었을 때 아무 동작도 하지 않음
        Mockito.doNothing().when(chatMessageService).markMessagesAsRead(anyLong(), anyString());

        // PUT 요청을 통해 메시지를 읽음 처리하는 API를 호출하고 응답을 검증
        mockMvc.perform(put("/messages/read/{roomId}", 1L)
                        .principal(authentication) // 인증 객체를 설정하여 사용자 식별
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()); // HTTP 200 상태 코드 기대
    }
}
