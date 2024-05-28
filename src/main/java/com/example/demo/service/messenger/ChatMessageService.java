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

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatJoinRepository chatJoinRepository;
    private final UnreadMemberRepository unreadMemberRepository;

    // 메시지를 DB에 저장
    // 이때 사용자가 읽었는지 유무 판단하여 unreadCount계산
    @Transactional
    public ChatMessage saveMessage(ChatMessage message) {

        message.setSendAt(LocalDateTime.now()); // 시간대는 메시지 전송하는 시간대로 설정

        // 해당 채팅방에 있는 사용자 수에서 메시지를 보낸 사람을 제외한 수로 unreadCount를 설정
        int unreadCount = chatJoinRepository.countByRoomId(message.getRoom().getRoomId()) - 1;
        message.setUnreadCount(unreadCount);

        // 메시지 -> DB 저장
        ChatMessage savedMessage = chatMessageRepository.save(message);

        // 메시지 보낸 사람 제외하고 같은 채팅방에 있는 모든 사람들을 unread한 것으로 처리
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

    // 메시지 읽음 표시
    // 사용자가 특정 채팅방에 참여하면 모두 읽게 되는 것이므로 채팅방 ID 기반으로 추적
    @Transactional
    public void markMessagesAsRead(Long roomId, String userId) {
        // 특정 채팅방에서 특정 사용자가 읽지 않은(Unread) 메시지(UnreadMember) 목록 조회
        List<UnreadMember> unreadMembers = unreadMemberRepository.findByRoomIdAndUserId(roomId, userId);

        // 조회된 UnreadMembers를 반복하면서 UnreadMember 엔티티 삭제. (메시지 읽엇으니까 더이상 UnreadMember 목록에 존재할 필요 없음)
        for (UnreadMember unreadMember : unreadMembers) {
            unreadMemberRepository.delete(unreadMember);

            // UnreadMember 엔티티가 참조하는 ChatMessage 가져와서 unreadcount 줄이기
            ChatMessage message = unreadMember.getChatMessage();
            message.setUnreadCount(message.getUnreadCount() - 1);

            chatMessageRepository.save(message);
        }
    }
}