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
        ChatRoom chatRoom = requestDTO.toEntity(null);
        //chatRoom = chatRoomRepository.save(chatRoom);

        List<ChatJoin> chatJoins = requestDTO.getParticipantIds().stream()
                .map(userId -> {
                    ChatJoin chatJoin = new ChatJoin(userId, chatRoom.getRoomId());
                    chatJoin.setRoom(chatRoom);
                    chatJoin.setUser(userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("Invalid user ID")));
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

    // 메시지
    public List<ChatMessage> getAllMessagesByRoomId(Long roomId) {
        return chatMessageRepository.findByRoom_RoomId(roomId);
    }

    @Transactional
    public ChatMessage saveMessage(Long roomId, ChatMessage message) {
        int participantCount = chatJoinRepository.countByRoomId(roomId);
        message.setRoom(chatRoomRepository.findById(roomId).orElseThrow(() -> new IllegalArgumentException("Invalid room ID")));
        message.setUnreadCount(participantCount - 1); // 보낸 사람 제외
        message.setSendAt(LocalDateTime.now());
        return chatMessageRepository.save(message);
    }

}
