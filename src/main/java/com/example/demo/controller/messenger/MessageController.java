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

}