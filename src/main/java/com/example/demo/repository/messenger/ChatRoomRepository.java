package com.example.demo.repository.messenger;


import com.example.demo.entity.messenger.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom,Long> {


}
