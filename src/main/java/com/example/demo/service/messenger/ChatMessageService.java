package com.example.demo.service.messenger;

import com.example.demo.entity.messenger.ChatJoin;
import com.example.demo.entity.messenger.ChatMessage;

import com.example.demo.entity.messenger.UnreadMember;
import com.example.demo.repository.messenger.ChatMessageRepository;
import com.example.demo.repository.messenger.ChatJoinRepository;
import com.example.demo.repository.messenger.UnreadMemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import java.util.logging.Logger;

/**
 * 이 서비스 클래스는 채팅 메시지와 관련된 주요 비즈니스 로직을 처리합니다.
 * 메시지 저장, 읽음 상태 처리 및 읽지 않은 메시지 수 관리를 위한 기능을 제공합니다.
 */
@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatJoinRepository chatJoinRepository;
    private final UnreadMemberRepository unreadMemberRepository;

    /**
     * 새로운 채팅 메시지를 저장하고 읽지 않은 사용자 목록을 업데이트합니다.
     * 메시지 전송 시간을 현재 시간으로 설정하고, 채팅방 내 메시지를 읽지 않은 사용자 수를 계산하여 저장합니다.
     *
     * @param message 저장할 채팅 메시지 객체
     * @return 저장된 채팅 메시지 객체
     */
    @Transactional
    public ChatMessage saveMessage(ChatMessage message) {

        message.setSendAt(LocalDateTime.now()); // 메시지 전송 시간을 현재 시간으로 설정

        // 채팅방 내 사용자 수에서 메시지를 보낸 사람을 제외한 사용자 수로 unreadCount 설정
        int unreadCount = chatJoinRepository.countByRoomId(message.getRoom().getRoomId()) - 1;
        message.setUnreadCount(unreadCount);

        // 메시지를 데이터베이스에 저장
        ChatMessage savedMessage = chatMessageRepository.save(message);

        // 메시지를 보낸 사람을 제외한 모든 채팅방 참여자를 읽지 않은 사용자로 처리
        // => 현재 채팅방의 사용자 정보를 가져오고, 메시지를 보낸 사람 제외 나머지 사용자들은 UnreadMember에 집어넣음
        List<ChatJoin> chatJoins = chatJoinRepository.findAllByRoomId(message.getRoom().getRoomId());
        for (ChatJoin chatJoin : chatJoins) {
            if (!chatJoin.getUserId().equals(message.getUser().getUserId())) {
                UnreadMember unreadMember = new UnreadMember(savedMessage.getMessageId(), chatJoin.getUserId(), savedMessage, chatJoin.getUser());
                unreadMemberRepository.save(unreadMember);
            }
        }

        return savedMessage;

    }

    /**
     * 특정 채팅방에서 사용자가 읽지 않은 모든 메시지를 읽음으로 표시합니다.
     * 사용자가 채팅방에 참여하면 해당 채팅방의 모든 메시지가 읽음으로 처리됩니다.
     *
     * @param roomId 읽음 처리할 채팅방의 ID
     * @param userId 읽음 처리할 사용자의 ID
     */
    @Transactional
    public void markMessagesAsRead(Long roomId, String userId) {
        // 읽지 않은(UnreadMember) 메시지 목록 조회
        List<UnreadMember> unreadMembers = unreadMemberRepository.findByRoomIdAndUserId(roomId, userId);

        // 각 메시지를 읽음으로 처리하고 unreadCount를 감소시킴
        for (UnreadMember unreadMember : unreadMembers) {
            unreadMemberRepository.delete(unreadMember);

            ChatMessage message = unreadMember.getChatMessage();
            message.setUnreadCount(message.getUnreadCount() - 1);

            chatMessageRepository.save(message);
        }
    }
}