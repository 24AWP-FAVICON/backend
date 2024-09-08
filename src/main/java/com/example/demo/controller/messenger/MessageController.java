package com.example.demo.controller.messenger;

import com.example.demo.dto.messenger.ChatMessageDTO;
import com.example.demo.entity.messenger.ChatMessage;
import com.example.demo.entity.users.user.User;
import com.example.demo.service.messenger.ChatRoomService;
import com.example.demo.service.messenger.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController()
@RequiredArgsConstructor
@Slf4j
public class MessageController {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;

    private final SimpMessageSendingOperations simpMessageSendingOperations;

    @MessageMapping("/message") // 클라이언트에서 /pub/hello로 메시지 발행
    public void message(ChatMessage message
//                        HttpServletRequest request,
//                        HttpServletResponse response
    ) {
        // JWT 토큰 검증
        //String userId = jwtCheckService.checkJwt(request, response);

        // 메시지에 정의된 채널 id에 메시지 보냄.
        // /sub/channel/{roomId} 에 구독중인 클라이언트에게 메시지를 보냄
        log.info("Received Message: {}", message.getContent());
        log.info("Sender: {}", message.getUser());
        // 테스트: simpMessageSendingOperations.convertAndSend("/sub/channel/" + message.getRoom().getRoomId(), message);

        // 채팅방에 사용자가 있는지 확인
        boolean isUserInRoom = chatRoomService.isUserInChatRoom(message.getRoom().getRoomId(), message.getUser().getUserId());
        if (isUserInRoom) {
            // 메시지 저장
            ChatMessage savedMessage = chatMessageService.saveMessage(message);
            // 메시지를 DTO로 변환하여 해당 채널로 전송
            ChatMessageDTO messageDTO = savedMessage.toDTO();
            simpMessageSendingOperations.convertAndSend("/sub/channel/" + messageDTO.getRoomId(), messageDTO);
        } else {
            log.warn("User {} is not in room {}", message.getUser().getUserId(), message.getRoom().getRoomId());
        }
    }

    // 메시지 읽음 여부 표시
    @PutMapping("/messages/read/{roomId}")
    public void markMessagesAsRead(@PathVariable Long roomId,
                                   HttpServletRequest request,
                                   HttpServletResponse response,
                                   Authentication authentication) {
        String userId = ((User) authentication.getPrincipal()).getUserId();
        chatMessageService.markMessagesAsRead(roomId, userId);
    }
}