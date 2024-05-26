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

    @PostMapping("/chatRoom")
    public ResponseEntity<ChatRoomResponseDTO> createChatRoom(@RequestBody ChatRoomRequestDTO.CreateDTO requestDTO) {
        try {
            ChatRoomResponseDTO responseDTO = chatRoomService.createChatRoom(requestDTO);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error creating chat room", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // localhost:8080/messeneger/chatRooms?userId=minbory925@gmail.com
    /*
    현재는 위의 방식처럼 뒤에 userId를 넣어줘야 하지만 추후 Authenticated를 추가하면 삭제될 예정임
     */
    @GetMapping("/chatRooms")
    public ResponseEntity<List<ChatRoomResponseDTO>> getAllChatRooms(@RequestParam String userId) {
        try {
            List<ChatRoomResponseDTO> chatRooms = chatRoomService.findAllChatRoomsByUserId(userId);
            return ResponseEntity.ok(chatRooms);
        } catch (Exception e) {
            logger.error("Error retrieving chat rooms", e);
            return ResponseEntity.internalServerError().build();
        }
    }


    @GetMapping("/chatRoom/{roomId}")
    public ResponseEntity<ChatRoomResponseDTO> getChatRoomById(@PathVariable Long roomId) {
        try {
            ChatRoomResponseDTO chatRoom = chatRoomService.findChatRoomById(roomId);
            return ResponseEntity.ok(chatRoom);
        } catch (Exception e) {
            logger.error("Error retrieving chat room", e);
            return ResponseEntity.internalServerError().build();
        }
    }

}