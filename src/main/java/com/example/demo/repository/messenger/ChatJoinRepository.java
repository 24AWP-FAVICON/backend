package com.example.demo.repository.messenger;


import com.example.demo.entity.messenger.ChatJoin;
import com.example.demo.entity.messenger.ChatJoinId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatJoinRepository extends JpaRepository<ChatJoin, ChatJoinId> {


}
