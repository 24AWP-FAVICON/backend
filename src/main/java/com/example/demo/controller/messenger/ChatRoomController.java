package com.example.demo.controller.messenger;

import com.example.demo.dto.messenger.ChatRoomRequestDTO;
import com.example.demo.dto.messenger.ChatRoomResponseDTO;
import com.example.demo.service.jwt.JwtCheckService;
import com.example.demo.service.messenger.ChatRoomService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController()
@RequestMapping("/messenger")
@RequiredArgsConstructor
@Slf4j
public class ChatRoomController {

    private final JwtCheckService jwtCheckService;
    private final ChatRoomService chatRoomService;
    private static final Logger logger = LoggerFactory.getLogger(ChatRoomController.class);

    // 채팅방 생성
    @PostMapping("/chatRoom")
    public ResponseEntity<ChatRoomResponseDTO> createChatRoom(@RequestBody ChatRoomRequestDTO.CreateDTO requestDTO,
                                                              HttpServletRequest request,
                                                              HttpServletResponse response) {
        try {
            jwtCheckService.checkJwt(request, response);
            ChatRoomResponseDTO responseDTO = chatRoomService.createChatRoom(requestDTO);
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            logger.error("Error creating chat room", e);
            return ResponseEntity.internalServerError().body(null);
        }
    }

    // 사용자가 참여한 모든 채팅방 조회
    @GetMapping("/chatRooms")
    public ResponseEntity<List<ChatRoomResponseDTO>> getAllChatRooms(HttpServletRequest request,
                                                                     HttpServletResponse response) {
        logger.info("Received request to get all chat rooms");

        try {
            String userId = jwtCheckService.checkJwt(request, response);
            List<ChatRoomResponseDTO> responseDTOList = chatRoomService.findAllChatRoomsByUserId(userId);
            return ResponseEntity.ok(responseDTOList);
        } catch (Exception e) {
            logger.error("Error retrieving chat rooms", e);
            return ResponseEntity.internalServerError().body(null);
        }
    }

    // 특정 채팅방 조회
    @GetMapping("/chatRoom/{roomId}")
    public ResponseEntity<ChatRoomResponseDTO> getChatRoomById(@PathVariable("roomId") Long roomId,
                                                               HttpServletRequest request,
                                                               HttpServletResponse response) {
        try {
            jwtCheckService.checkJwt(request, response);
            ChatRoomResponseDTO responseDTO = chatRoomService.findChatRoomById(roomId);
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            logger.error("Error retrieving chat room", e);
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @GetMapping("/chatRoom/{roomId}/messages")
    public ResponseEntity<ChatRoomResponseDTO> getChatRoomContentsById(@PathVariable("roomId") Long roomId,
                                                               HttpServletRequest request,
                                                               HttpServletResponse response) {
        try {
            jwtCheckService.checkJwt(request, response);
            ChatRoomResponseDTO responseDTO = chatRoomService.findChatRoomById(roomId);
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            logger.error("Error retrieving chat room", e);
            return ResponseEntity.internalServerError().body(null);
        }
    }
    // 특정 채팅방 이름 변경
    @PutMapping("/chatRoom/{roomId}/name")
    public ResponseEntity<Void> updateChatRoomName(@PathVariable("roomId") Long roomId,
                                                   @RequestBody ChatRoomRequestDTO.UpdateDTO updateDTO,
                                                   HttpServletRequest request,
                                                   HttpServletResponse response) {
        try {
            jwtCheckService.checkJwt(request, response);
            chatRoomService.updateChatRoomName(roomId, updateDTO.getName());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error updating chat room name", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    // 특정 채팅방에 사용자 초대
    @PostMapping("/chatRoom/{roomId}")
    public ResponseEntity<Void> inviteUserToChatRoom(@PathVariable("roomId") Long roomId,
                                                     @RequestBody ChatRoomRequestDTO.InviteDTO inviteRequest,
                                                     HttpServletRequest request,
                                                     HttpServletResponse response) {
        try {
            jwtCheckService.checkJwt(request, response);
            chatRoomService.inviteUserToChatRoom(roomId, inviteRequest);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error inviting user to chat room", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // 특정 채팅방 나가기 (사용자 삭제)
    @DeleteMapping("/chatRoom/{roomId}")
    public ResponseEntity<Void> leaveChatRoom(@PathVariable("roomId") Long roomId,
                                              HttpServletRequest request,
                                              HttpServletResponse response) {
        try {
            String userId = jwtCheckService.checkJwt(request, response);
            chatRoomService.leaveChatRoom(roomId, userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error leaving chat room", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}