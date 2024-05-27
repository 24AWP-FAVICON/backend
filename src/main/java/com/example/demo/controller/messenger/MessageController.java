package com.example.demo.controller.messenger;

import com.example.demo.entity.messenger.Message;
import com.example.demo.service.messenger.ChatRoomService;
import com.example.demo.service.messenger.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.RestController;


@RestController()
@RequiredArgsConstructor
@Slf4j
public class MessageController {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;

    private final SimpMessageSendingOperations simpMessageSendingOperations;

    @MessageMapping("/hello") // 1. 클라이언트에서 /pub/hello로 메시지 발행
    public void message(Message message){
        // 2. 메시지에 정의된 채널 id에 메시지 보냄.
        // /sub/channel/채널아이디 에 구독중인 클라이언트에게 메시지를 보냄
        log.info("Received Message: {}", message);
        simpMessageSendingOperations.convertAndSend("/sub/channel/" + message.getChannelId(), message);
    }

//    @MessageMapping("/chat.sendMessage")
//    @SendTo("/topic/public")
//    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
//        chatMessage.setSendAt(LocalDateTime.now());
//        chatMessageService.saveMessage(chatMessage);
//        return chatMessage;
//    }
//
//    @MessageMapping("/chat.addUser")
//    @SendTo("/topic/public")
//    public ChatMessage addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
//        headerAccessor.getSessionAttributes().put("username", chatMessage.getUser().getUserId());
//        return chatMessage;
//    }
}
