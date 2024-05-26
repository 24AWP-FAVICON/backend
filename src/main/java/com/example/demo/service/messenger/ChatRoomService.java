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

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ChatRoomService {

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatJoinRepository chatJoinRepository;

    // 채팅방 생성
    @Transactional
    public ChatRoomResponseDTO createChatRoom(ChatRoomRequestDTO.CreateDTO requestDTO) {
        ChatRoom chatRoom = requestDTO.toEntity(null);
        chatRoom = chatRoomRepository.save(chatRoom);

        ChatJoin chatJoin = new ChatJoin(requestDTO.getCreatorUserId(), chatRoom.getRoomId());
        chatJoinRepository.save(chatJoin);

        return new ChatRoomResponseDTO(chatRoom.getRoomId(), chatRoom.getName(), chatRoom.getCreateAt());
    }

    // 사용자가 참여한 모든 채팅방 조회
    @Transactional
    public List<ChatRoomResponseDTO> findAllChatRoomsByUserId(String userId) {
        List<ChatJoin> chatJoins = chatJoinRepository.findAllByUserId(userId);
        return chatJoins.stream()
                .map(chatJoin -> {
                    ChatRoom chatRoom = chatRoomRepository.findById(chatJoin.getRoomId()).orElseThrow(() -> new IllegalArgumentException("Invalid room ID"));
                    return new ChatRoomResponseDTO(chatRoom.getRoomId(), chatRoom.getName(), chatRoom.getCreateAt());
                })
                .collect(Collectors.toList());
    }

    // 특정 채팅방 조회
    @Transactional
    public ChatRoomResponseDTO findChatRoomById(Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new IllegalArgumentException("Invalid room ID"));
        return new ChatRoomResponseDTO(chatRoom.getRoomId(), chatRoom.getName(), chatRoom.getCreateAt());
    }

    // 특정 채팅방에 사용자 초대
    @Transactional
    public void inviteUserToChatRoom(Long roomId, ChatRoomRequestDTO.InviteDTO inviteRequest) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new IllegalArgumentException("Invalid room ID"));
        ChatJoin chatJoin = new ChatJoin(inviteRequest.getInviteUserId(), chatRoom.getRoomId());
        chatJoinRepository.save(chatJoin);
    }

    // 특정 채팅방 나가기 (사용자 삭제)
    @Transactional
    public void leaveChatRoom(Long roomId, String userId) {
        ChatJoin chatJoin = chatJoinRepository.findByRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found in chat room"));
        chatJoinRepository.delete(chatJoin);
    }


}
