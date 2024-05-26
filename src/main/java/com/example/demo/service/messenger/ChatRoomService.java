package com.example.demo.service.messenger;


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
    public ChatRoom createChatRoom(String name, String creatorUserId) {
        ChatRoom chatRoom = new ChatRoom(name);
        chatRoomRepository.save(chatRoom);

        ChatJoin chatJoin = new ChatJoin(creatorUserId, chatRoom.getRoomId());
        chatJoinRepository.save(chatJoin);

        return chatRoom;
    }
}
