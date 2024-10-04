package com.example.demo.controller.messenger;

import com.example.demo.dto.messenger.ChatMessageDTO;
import com.example.demo.dto.messenger.ChatRoomRequestDTO;
import com.example.demo.dto.messenger.ChatRoomResponseDTO;
import com.example.demo.entity.users.user.User;
import com.example.demo.service.messenger.ChatRoomService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 이 컨트롤러 클래스는 채팅방 관련 API 요청을 처리합니다.
 * 채팅방 생성, 조회, 메시지 관리, 사용자 초대 및 채팅방 나가기와 같은 기능을 제공합니다.
 * JWT 토큰을 기반으로 사용자의 인증을 처리합니다.
 */
@RestController()
@RequestMapping("/messenger")
@RequiredArgsConstructor
@Slf4j
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private static final Logger logger = LoggerFactory.getLogger(ChatRoomController.class);

    /**
     * 새로운 채팅방을 생성합니다.
     * 클라이언트에서 받은 요청 데이터를 기반으로 채팅방을 생성하고, 해당 방 정보를 반환합니다.
     *
     * @param requestDTO 채팅방 생성 요청 데이터를 담은 DTO
     * @param request HttpServletRequest 객체 (JWT 검증에 사용)
     * @param response HttpServletResponse 객체 (JWT 검증에 사용)
     * @return 생성된 채팅방 정보와 함께 200 OK 응답
     */
    @PostMapping("/chatRoom")
    public ResponseEntity<ChatRoomResponseDTO> createChatRoom(@RequestBody ChatRoomRequestDTO.CreateDTO requestDTO,
                                                              HttpServletRequest request,
                                                              HttpServletResponse response) {
        try {
            ChatRoomResponseDTO responseDTO = chatRoomService.createChatRoom(requestDTO);
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            logger.error("Error creating chat room", e);
            return ResponseEntity.internalServerError().body(null);
        }
    }

    /**
     * 사용자가 참여한 모든 채팅방을 조회합니다.
     * 사용자가 참여 중인 채팅방 목록을 반환합니다.
     *
     * @param request HttpServletRequest 객체 (JWT 검증에 사용)
     * @param response HttpServletResponse 객체 (JWT 검증에 사용)
     * @return 사용자가 참여한 채팅방 목록과 함께 200 OK 응답
     */
    @GetMapping("/chatRooms")
    public ResponseEntity<List<ChatRoomResponseDTO>> getAllChatRooms(HttpServletRequest request,
                                                                     HttpServletResponse response,
                                                                     Authentication authentication) {
        logger.info("Received request to get all chat rooms");

        try {
            String userId = ((User) authentication.getPrincipal()).getUserId();
            List<ChatRoomResponseDTO> responseDTOList = chatRoomService.findAllChatRoomsByUserId(userId);
            return ResponseEntity.ok(responseDTOList);
        } catch (Exception e) {
            logger.error("Error retrieving chat rooms", e);
            return ResponseEntity.internalServerError().body(null);
        }
    }

    /**
     * 특정 채팅방의 정보를 조회합니다.
     *
     * @param roomId 조회할 채팅방의 ID
     * @param request HttpServletRequest 객체 (JWT 검증에 사용)
     * @param response HttpServletResponse 객체 (JWT 검증에 사용)
     * @return 채팅방 정보와 함께 200 OK 응답
     */
    @GetMapping("/chatRoom/{roomId}")
    public ResponseEntity<ChatRoomResponseDTO> getChatRoomById(@PathVariable("roomId") Long roomId,
                                                               HttpServletRequest request,
                                                               HttpServletResponse response) {
        try {
            ChatRoomResponseDTO responseDTO = chatRoomService.findChatRoomById(roomId);
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            logger.error("Error retrieving chat room", e);
            return ResponseEntity.internalServerError().body(null);
        }
    }

    /**
     * 특정 채팅방 내 모든 메시지를 조회합니다.
     *
     * @param roomId 조회할 채팅방의 ID
     * @param request HttpServletRequest 객체 (JWT 검증에 사용)
     * @param response HttpServletResponse 객체 (JWT 검증에 사용)
     * @return 채팅방 메시지 목록과 함께 200 OK 응답
     */
    @GetMapping("/chatRoom/{roomId}/messages")
    public ResponseEntity<List<ChatMessageDTO>> getChatRoomMessagesById(@PathVariable("roomId") Long roomId,
                                                               HttpServletRequest request,
                                                               HttpServletResponse response) {
        try {
            List<ChatMessageDTO> messages = chatRoomService.getChatMessagesByRoomId(roomId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            logger.error("Error retrieving chat room messages", e);
            return ResponseEntity.internalServerError().body(null);
        }
    }

    /**
     * 특정 채팅방의 이름을 변경합니다.
     *
     * @param roomId 변경할 채팅방의 ID
     * @param updateDTO 새 채팅방 이름이 담긴 DTO
     * @param request HttpServletRequest 객체 (JWT 검증에 사용)
     * @param response HttpServletResponse 객체 (JWT 검증에 사용)
     * @return 성공적으로 변경된 경우 200 OK 응답
     */
    @PutMapping("/chatRoom/{roomId}/name")
    public ResponseEntity<Void> updateChatRoomName(@PathVariable("roomId") Long roomId,
                                                   @RequestBody ChatRoomRequestDTO.UpdateDTO updateDTO,
                                                   HttpServletRequest request,
                                                   HttpServletResponse response) {
        try {
            chatRoomService.updateChatRoomName(roomId, updateDTO.getName());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error updating chat room name", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 특정 채팅방에 사용자를 초대합니다.
     *
     * @param roomId 초대할 채팅방의 ID
     * @param inviteRequest 초대할 사용자 정보가 담긴 DTO
     * @param request HttpServletRequest 객체 (JWT 검증에 사용)
     * @param response HttpServletResponse 객체 (JWT 검증에 사용)
     * @return 성공적으로 초대된 경우 200 OK 응답
     */
    @PostMapping("/chatRoom/{roomId}")
    public ResponseEntity<Void> inviteUserToChatRoom(@PathVariable("roomId") Long roomId,
                                                     @RequestBody ChatRoomRequestDTO.InviteDTO inviteRequest,
                                                     HttpServletRequest request,
                                                     HttpServletResponse response) {
        try {
            chatRoomService.inviteUserToChatRoom(roomId, inviteRequest);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error inviting user to chat room", e);
            return ResponseEntity.internalServerError().build();
        }
    }


    /**
     * 특정 채팅방에서 사용자가 나가도록 처리합니다.
     *
     * @param roomId 채팅방의 ID
     * @param request HttpServletRequest 객체 (JWT 검증에 사용)
     * @param response HttpServletResponse 객체 (JWT 검증에 사용)
     * @return 성공적으로 나간 경우 200 OK 응답
     */
    @DeleteMapping("/chatRoom/{roomId}")
    public ResponseEntity<Void> leaveChatRoom(@PathVariable("roomId") Long roomId,
                                              HttpServletRequest request,
                                              HttpServletResponse response,
                                              Authentication authentication) {
        try {
            String userId = ((User) authentication.getPrincipal()).getUserId();
            chatRoomService.leaveChatRoom(roomId, userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error leaving chat room", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}