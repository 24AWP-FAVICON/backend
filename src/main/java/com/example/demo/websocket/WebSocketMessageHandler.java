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

/**
 * 이 컨트롤러 클래스는 WebSocket을 통해 수신된 메시지를 처리하는 역할을 합니다.
 * 클라이언트로부터 수신한 메시지를 저장하고, 특정 채팅방에 구독 중인 클라이언트에게 전송합니다.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketMessageHandler {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;
    private final SimpMessageSendingOperations simpMessageSendingOperations;

    /**
     * 클라이언트로부터 메시지를 수신하여 처리합니다.
     * 수신된 메시지를 저장하고, 채팅방에 구독 중인 모든 클라이언트에게 전송합니다.
     *
     * @param message 클라이언트로부터 수신된 채팅 메시지
     */
    @MessageMapping("/message") // 클라이언트에서 /pub/message 경로로 메시지 발행
    public void handleMessage(ChatMessage message) {

        // 메시지 내용과 송신자 정보 로그 기록
        log.info("Received Message: {}", message.getContent());
        log.info("Sender: {}", message.getUser());

        // 메시지를 보낸 사용자가 해당 채팅방에 존재하는지 확인
        boolean isUserInRoom = chatRoomService.isUserInChatRoom(message.getRoom().getRoomId(), message.getUser().getUserId());
        if (isUserInRoom) {
            // 메시지를 데이터베이스에 저장
            ChatMessage savedMessage = chatMessageService.saveMessage(message);
            // 저장된 메시지를 DTO로 변환하여 구독 중인 채팅방 클라이언트들에게 전송
            ChatMessageDTO messageDTO = savedMessage.toDTO();
            simpMessageSendingOperations.convertAndSend("/sub/channel/" + messageDTO.getRoomId(), messageDTO);
        } else {
            // 사용자가 채팅방에 없을 경우 경고 로그 출력
            log.warn("User {} is not in room {}", message.getUser().getUserId(), message.getRoom().getRoomId());
        }
    }
}
