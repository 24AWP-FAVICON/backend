package com.example.demo.websocket;

import com.example.demo.dto.messenger.ChatMessageDTO;
import com.example.demo.entity.messenger.ChatMessage;
import com.example.demo.service.messenger.ChatMessageService;
import com.example.demo.service.messenger.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketMessageHandler {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;
    private final SimpMessageSendingOperations simpMessageSendingOperations;

    @MessageMapping("/message") // 클라이언트에서 /pub/hello로 메시지 발행
    public void handleMessage(ChatMessage message) {
        // /sub/channel/{roomId} 에 구독중인 클라이언트에게 메시지를 보냄
        log.info("Received Message: {}", message.getContent());
        log.info("Sender: {}", message.getUser());

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
}
