package com.example.demo.service.messenger;


import com.example.demo.dto.messenger.ChatRoomRequestDTO;
import com.example.demo.dto.messenger.ChatRoomResponseDTO;
import com.example.demo.entity.messenger.ChatJoin;
import com.example.demo.entity.messenger.ChatRoom;
import com.example.demo.repository.messenger.ChatJoinRepository;
import com.example.demo.repository.messenger.ChatRoomRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ChatRoomService {

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatJoinRepository chatJoinRepository;

    @Transactional
    public ChatRoomResponseDTO createChatRoom(ChatRoomRequestDTO.CreateDTO requestDTO) {
        // ChatRoom 엔티티를 생성하고 저장
        ChatRoom chatRoom = requestDTO.toEntity(null);  // User 정보는 필요 시 추가
        chatRoom = chatRoomRepository.save(chatRoom);

        // 저장된 chatRoom의 roomId를 사용하여 ChatJoin 엔티티 생성
        ChatJoin chatJoin = new ChatJoin(requestDTO.getCreatorUserId(), chatRoom.getRoomId());
        chatJoinRepository.save(chatJoin);

        return new ChatRoomResponseDTO(chatRoom.getRoomId(), chatRoom.getName(), chatRoom.getCreateAt());
    }


}
