package com.example.demo.repository.messenger;


import com.example.demo.entity.messenger.ChatJoin;
import com.example.demo.entity.messenger.ChatJoinId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatJoinRepository extends JpaRepository<ChatJoin, ChatJoinId> {
    List<ChatJoin> findAllByUserId(String userId);
}
