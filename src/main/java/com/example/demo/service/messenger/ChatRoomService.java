package com.example.demo.service.messenger;

import com.example.demo.dto.messenger.ChatMessageDTO;
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

/**
 * 이 서비스 클래스는 채팅방과 관련된 로직을 처리합니다.
 * 채팅방 생성, 조회, 메시지 관리, 사용자 초대 및 채팅방 탈퇴와 같은 기능을 제공합니다.
 */
@Slf4j
@Service
public class ChatRoomService {

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatJoinRepository chatJoinRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    /**
     * 새로운 채팅방을 생성하고 참여자를 추가합니다.
     * 채팅방 이름과 참여자 목록을 기반으로 새로운 채팅방을 생성합니다.
     *
     * @param requestDTO 채팅방 생성 요청 데이터가 담긴 DTO
     * @return 생성된 채팅방의 응답 DTO
     */
    @Transactional
    public ChatRoomResponseDTO createChatRoom(ChatRoomRequestDTO.CreateDTO requestDTO) {
        // 채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .name(requestDTO.getName())
                .createAt(LocalDateTime.now())
                .build();
        chatRoomRepository.save(chatRoom);
        log.info("ChatRoom saved: {}", chatRoom);

        // ChatJoin 엔티티를 생성하여 참여자 추가
        List<ChatJoin> chatJoins = requestDTO.getParticipantIds().stream()
                .map(userId -> {
                    ChatJoin chatJoin = new ChatJoin(userId, chatRoom.getRoomId());
                    chatJoin.setRoom(chatRoom);
                    chatJoin.setUser(userRepository.findById(userId)
                            .orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + userId)));
                    return chatJoin;
                })
                .collect(Collectors.toList());

        chatJoinRepository.saveAll(chatJoins);
        log.info("ChatJoin entities saved: {}", chatJoins);

        List<String> users = chatJoins.stream().map(ChatJoin::getUserId).collect(Collectors.toList());
        return new ChatRoomResponseDTO(chatRoom.getRoomId(), chatRoom.getName(), chatRoom.getCreateAt(), users);
    }


    /**
     * 사용자가 참여한 모든 채팅방을 조회합니다.
     * 사용자가 속한 모든 채팅방을 반환합니다.
     *
     * @param userId 사용자의 ID
     * @return 사용자가 참여한 채팅방 목록
     */
    @Transactional
    public List<ChatRoomResponseDTO> findAllChatRoomsByUserId(String userId) {
        List<ChatJoin> chatJoins = chatJoinRepository.findAllByUserId(userId);
        return chatJoins.stream()
                .map(chatJoin -> {
                    ChatRoom chatRoom = chatRoomRepository.findById(chatJoin.getRoomId())
                            .orElseThrow(() -> new IllegalArgumentException("Invalid room ID: " + chatJoin.getRoomId()));
                    List<ChatJoin> usersInRoom = chatJoinRepository.findAllByRoomId(chatJoin.getRoomId());
                    List<String> users = usersInRoom.stream().map(ChatJoin::getUserId).collect(Collectors.toList());
                    return new ChatRoomResponseDTO(chatRoom.getRoomId(), chatRoom.getName(), chatRoom.getCreateAt(), users);
                })
                .collect(Collectors.toList());
    }

    /**
     * 특정 채팅방의 정보를 조회합니다.
     *
     * @param roomId 조회할 채팅방의 ID
     * @return 채팅방의 응답 DTO
     */
    @Transactional
    public ChatRoomResponseDTO findChatRoomById(Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid room ID: " + roomId));
        List<ChatJoin> chatJoins = chatJoinRepository.findAllByRoomId(roomId);
        List<String> users = chatJoins.stream().map(ChatJoin::getUserId).collect(Collectors.toList());
        return new ChatRoomResponseDTO(chatRoom.getRoomId(), chatRoom.getName(), chatRoom.getCreateAt(), users);
    }

    /**
     * 특정 채팅방의 모든 메시지를 조회합니다.
     *
     * @param roomId 채팅방의 ID
     * @return 채팅방의 메시지 목록
     */
    @Transactional
    public List<ChatMessageDTO> getChatMessagesByRoomId(Long roomId) {
        List<ChatMessage> messages = chatMessageRepository.findByRoom_RoomId(roomId);
        return messages.stream()
                .map(ChatMessage::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 특정 채팅방에 사용자를 초대합니다.
     *
     * @param roomId 초대할 채팅방의 ID
     * @param inviteRequest 초대할 사용자 정보가 담긴 DTO
     */
    @Transactional
    public void inviteUserToChatRoom(Long roomId, ChatRoomRequestDTO.InviteDTO inviteRequest) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid room ID: " + roomId));
        ChatJoin chatJoin = new ChatJoin(inviteRequest.getInviteUserId(), chatRoom.getRoomId());
        chatJoin.setRoom(chatRoom);
        chatJoin.setUser(userRepository.findById(inviteRequest.getInviteUserId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + inviteRequest.getInviteUserId())));
        chatJoinRepository.save(chatJoin);
    }

    /**
     * 특정 사용자가 채팅방에서 나가도록 처리합니다.
     *
     * @param roomId 채팅방 ID
     * @param userId 나가는 사용자의 ID
     */
    @Transactional
    public void leaveChatRoom(Long roomId, String userId) {
        ChatJoin chatJoin = chatJoinRepository.findByRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found in chat room: " + userId));
        chatJoinRepository.delete(chatJoin);
    }

    /**
     * 채팅방 이름을 수정합니다.
     *
     * @param roomId 수정할 채팅방의 ID
     * @param newName 새 채팅방 이름
     */
    @Transactional
    public void updateChatRoomName(Long roomId, String newName) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid room ID: " + roomId));
        chatRoom.setName(newName);
        chatRoomRepository.save(chatRoom);
    }

    /**
     * 사용자가 채팅방에 존재하는지 확인합니다.
     *
     * @param roomId 채팅방 ID
     * @param userId 사용자 ID
     * @return 사용자가 채팅방에 존재하면 true, 그렇지 않으면 false
     */
    @Transactional
    public boolean isUserInChatRoom(Long roomId, String userId) {
        return chatJoinRepository.existsByRoomIdAndUserId(roomId, userId);
    }


}
