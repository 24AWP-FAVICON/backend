package com.example.demo.controller.messenger;


import com.example.demo.entity.users.user.User;
import com.example.demo.service.messenger.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


/**
 * 이 컨트롤러 클래스는 메시지 관련 API 요청을 처리합니다.
 * 주로 메시지 읽음 상태를 처리하는 기능을 제공합니다.
 */
@RestController()
@RequiredArgsConstructor
@Slf4j
public class MessageController {

    private final ChatMessageService chatMessageService;

    /**
     * 채팅방에서 메시지를 읽음으로 표시합니다.
     * 사용자가 특정 채팅방에 참여하면 해당 채팅방의 모든 메시지를 읽음으로 처리합니다.
     *
     * @param roomId 읽음 처리할 채팅방의 ID
     * @param request HttpServletRequest 객체 (JWT 검증에 사용)
     * @param response HttpServletResponse 객체 (JWT 검증에 사용)
     */
    @PutMapping("/messages/read/{roomId}")
    public void markMessagesAsRead(@PathVariable Long roomId,
                                   HttpServletRequest request,
                                   HttpServletResponse response,
                                   Authentication authentication) {
        String userId = ((User) authentication.getPrincipal()).getUserId();
        chatMessageService.markMessagesAsRead(roomId, userId);
    }
}