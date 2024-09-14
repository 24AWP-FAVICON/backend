package com.example.demo.controller.messenger;

import com.example.demo.service.jwt.JwtCheckService;
import com.example.demo.service.messenger.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController()
@RequiredArgsConstructor
@Slf4j
public class MessageController {

    private final ChatMessageService chatMessageService;
    private final JwtCheckService jwtCheckService;

    // 메시지 읽음 여부 표시
    @PutMapping("/messages/read/{roomId}")
    public void markMessagesAsRead(@PathVariable Long roomId,
                                   HttpServletRequest request,
                                   HttpServletResponse response) {
        String userId = jwtCheckService.checkJwt(request, response);
        chatMessageService.markMessagesAsRead(roomId, userId);
    }
}