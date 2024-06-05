package com.example.demo.controller.messenger;

import com.example.demo.dto.messenger.ChatMessageDTO;
import com.example.demo.entity.messenger.ChatMessage;
import com.example.demo.service.jwt.JwtCheckService;
import com.example.demo.service.messenger.ChatRoomService;
import com.example.demo.service.messenger.ChatMessageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MessageController {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;
    private final JwtCheckService jwtCheckService;

    private final SimpMessageSendingOperations simpMessageSendingOperations;

    @MessageMapping("/message") // 클라이언트에서 /pub/message로 메시지 발행
    public void message(ChatMessage message, HttpServletRequest request, HttpServletResponse response) {
        log.info("Received Message: {}", message);

        // JWT 토큰 검증
        String userId;
        try {
            userId = jwtCheckService.checkJwt(request, response);
        } catch (Exception e) {
            log.warn("Invalid JWT token", e);
            return;
        }

        // 메시지의 사용자 ID 설정
        message.getUser().setUserId(userId);

        // 채팅방에 사용자가 있는지 확인
        boolean isUserInRoom = chatRoomService.isUserInChatRoom(message.getRoom().getRoomId(), userId);
        if (isUserInRoom) {
            // 메시지 저장
            ChatMessage savedMessage = chatMessageService.saveMessage(message);
            // 메시지를 DTO로 변환하여 해당 채널로 전송
            ChatMessageDTO messageDTO = savedMessage.toDTO();
            simpMessageSendingOperations.convertAndSend("/sub/channel/" + messageDTO.getRoomId(), messageDTO);
        } else {
            log.warn("User {} is not in room {}", userId, message.getRoom().getRoomId());
        }
    }

    // 메시지 읽음 여부 표시
    @PutMapping("/messages/read/{roomId}")
    public void markMessagesAsRead(@PathVariable Long roomId,
                                   HttpServletRequest request,
                                   HttpServletResponse response) {
        String userId = jwtCheckService.checkJwt(request, response);
        chatMessageService.markMessagesAsRead(roomId, userId);
    }
}
