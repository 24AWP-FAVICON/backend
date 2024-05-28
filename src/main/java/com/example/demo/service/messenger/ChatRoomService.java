package com.example.demo.service.messenger;

import com.example.demo.dto.messenger.ChatRoomRequestDTO;
import com.example.demo.dto.messenger.ChatRoomResponseDTO;
import com.example.demo.entity.messenger.ChatJoin;
import com.example.demo.entity.messenger.ChatMessage;
import com.example.demo.entity.messenger.ChatRoom;
import com.example.demo.repository.messenger.ChatJoinRepository;
import com.example.demo.repository.messenger.ChatMessageRepository;
import com.example.demo.repository.messenger.ChatRoomRepository;
import com.example.demo.repository.users.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ChatRoomService {

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatJoinRepository chatJoinRepository;

    @Autowired
    private UserRepository userRepository;

    private final ChatMessageRepository chatMessageRepository;

    public ChatRoomService(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    // 채팅방 생성
    @Transactional
    public ChatRoomResponseDTO createChatRoom(ChatRoomRequestDTO.CreateDTO requestDTO) {

        // 먼저 ChatRoom 엔티티를 저장
        ChatRoom chatRoom = ChatRoom.builder()
                .name(requestDTO.getName())
                .createAt(LocalDateTime.now())
                .build();
        chatRoomRepository.save(chatRoom);

        // ChatJoin 엔티티를 생성하고 저장
        List<ChatJoin> chatJoins = requestDTO.getParticipantIds().stream()
                .map(userId -> {
                    ChatJoin chatJoin = new ChatJoin(userId, chatRoom.getRoomId());
                    chatJoin.setRoom(chatRoom);
                    chatJoin.setUser(userRepository.findById(userId)
                            .orElseThrow(() -> new IllegalArgumentException("Invalid user ID")));
                    return chatJoin;
                })
                .collect(Collectors.toList());

        chatJoinRepository.saveAll(chatJoins);

        List<String> users = chatJoins.stream().map(ChatJoin::getUserId).collect(Collectors.toList());
        return new ChatRoomResponseDTO(chatRoom.getRoomId(), chatRoom.getName(), chatRoom.getCreateAt(), users);
    }

    // 사용자가 참여한 모든 채팅방 조회
    @Transactional
    public List<ChatRoomResponseDTO> findAllChatRoomsByUserId(String userId) {
        List<ChatJoin> chatJoins = chatJoinRepository.findAllByUserId(userId);
        return chatJoins.stream()
                .map(chatJoin -> {
                    ChatRoom chatRoom = chatRoomRepository.findById(chatJoin.getRoomId()).orElseThrow(() -> new IllegalArgumentException("Invalid room ID"));
                    List<ChatJoin> usersInRoom = chatJoinRepository.findAllByRoomId(chatJoin.getRoomId());
                    List<String> users = usersInRoom.stream().map(ChatJoin::getUserId).collect(Collectors.toList());
                    return new ChatRoomResponseDTO(chatRoom.getRoomId(), chatRoom.getName(), chatRoom.getCreateAt(), users);
                })
                .collect(Collectors.toList());
    }

    // 특정 채팅방 조회
    @Transactional
    public ChatRoomResponseDTO findChatRoomById(Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new IllegalArgumentException("Invalid room ID"));
        List<ChatJoin> chatJoins = chatJoinRepository.findAllByRoomId(roomId);
        List<String> users = chatJoins.stream().map(ChatJoin::getUserId).collect(Collectors.toList());
        return new ChatRoomResponseDTO(chatRoom.getRoomId(), chatRoom.getName(), chatRoom.getCreateAt(), users);
    }

    // 특정 채팅방에 사용자 초대
    @Transactional
    public void inviteUserToChatRoom(Long roomId, ChatRoomRequestDTO.InviteDTO inviteRequest) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new IllegalArgumentException("Invalid room ID"));
        ChatJoin chatJoin = new ChatJoin(inviteRequest.getInviteUserId(), chatRoom.getRoomId());
        chatJoin.setRoom(chatRoom);
        chatJoin.setUser(userRepository.findById(inviteRequest.getInviteUserId()).orElseThrow(() -> new IllegalArgumentException("Invalid user ID")));
        chatJoinRepository.save(chatJoin);
    }

    // 특정 채팅방 나가기 (사용자 삭제)
    @Transactional
    public void leaveChatRoom(Long roomId, String userId) {
        ChatJoin chatJoin = chatJoinRepository.findByRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found in chat room"));
        chatJoinRepository.delete(chatJoin);
    }

    // 채팅방 이름 변경
    @Transactional
    public void updateChatRoomName(Long roomId, String newName) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid room ID"));
        chatRoom.setName(newName);
        chatRoomRepository.save(chatRoom);
    }

    // 유저 채팅방 내 존재 유무 판단
    @Transactional
    public boolean isUserInChatRoom(Long roomId, String userId) {
        return chatJoinRepository.existsByRoomIdAndUserId(roomId, userId);
    }
}
