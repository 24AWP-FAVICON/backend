package com.example.demo.repository.messenger;


import com.example.demo.entity.messenger.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom,Long> {
    //List<ChatRoom> findAllByUserId(String userId);
}
