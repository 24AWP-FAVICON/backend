package com.example.demo.controller.messenger;

import com.example.demo.dto.messenger.ChatRoomRequestDTO;
import com.example.demo.dto.messenger.ChatRoomResponseDTO;
import com.example.demo.service.messenger.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController()
@RequestMapping("/messeneger")
@RequiredArgsConstructor
@Slf4j
public class MessageController {

    private final ChatRoomService chatRoomService;
    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    // 채팅방 생성
    @PostMapping("/chatRoom")
    public ResponseEntity<ChatRoomResponseDTO> createChatRoom(@RequestBody ChatRoomRequestDTO.CreateDTO requestDTO) {
        try {
            ChatRoomResponseDTO responseDTO = chatRoomService.createChatRoom(requestDTO);
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            logger.error("Error creating chat room", e);
            return ResponseEntity.internalServerError().body(null);
        }
    }

    // 사용자가 참여한 모든 채팅방 조회
    // localhost:8080/messeneger/chatRooms?userId=minbory925@gmail.com
    /*
    현재는 위의 방식처럼 뒤에 userId를 넣어줘야 하지만 추후 JWT 토큰 인증m 추가하면 삭제될 예정임
     */
    @GetMapping("/chatRooms")
    public ResponseEntity<List<ChatRoomResponseDTO>> getAllChatRooms(@RequestParam("userId") String userId) {
        try {
            List<ChatRoomResponseDTO> responseDTOList = chatRoomService.findAllChatRoomsByUserId(userId);
            return ResponseEntity.ok(responseDTOList);
        } catch (Exception e) {
            logger.error("Error retrieving chat rooms", e);
            return ResponseEntity.internalServerError().body(null);
        }
    }

    // 특정 채팅방 조회
    @GetMapping("/chatRoom/{roomId}")
    public ResponseEntity<ChatRoomResponseDTO> getChatRoomById(@PathVariable("roomId") Long roomId) {
        try {
            ChatRoomResponseDTO responseDTO = chatRoomService.findChatRoomById(roomId);
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            logger.error("Error retrieving chat room", e);
            return ResponseEntity.internalServerError().body(null);
        }
    }

    // 특정 채팅방에 사용자 초대
    @PostMapping("/chatRoom/{roomId}")
    public ResponseEntity<Void> inviteUserToChatRoom(@PathVariable("roomId") Long roomId, @RequestBody ChatRoomRequestDTO.InviteDTO inviteRequest) {
        try {
            chatRoomService.inviteUserToChatRoom(roomId, inviteRequest);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error inviting user to chat room", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // 특정 채팅방 나가기 (사용자 삭제)
     /*
    모든 채팅방 조회와 마찬가지로
    DELETE /messages/chatRoom/{room_id}?userId={userId}
    userId를 입력해야 하지만 추후 JWT 토큰 인증을 넣으면 필요없어짐 (API 명세대로 구현할 수 있음)
     */
    @DeleteMapping("/chatRoom/{roomId}")
    public ResponseEntity<Void> leaveChatRoom(@PathVariable("roomId") Long roomId, @RequestParam("userId") String userId) {
        try {
            chatRoomService.leaveChatRoom(roomId, userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error leaving chat room", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}