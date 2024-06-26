package com.example.demo.repository.messenger;


import com.example.demo.entity.messenger.ChatJoin;
import com.example.demo.entity.messenger.ChatJoinId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ChatJoinRepository extends JpaRepository<ChatJoin, ChatJoinId> {
    List<ChatJoin> findAllByUserId(String userId);
    Optional<ChatJoin> findByRoomIdAndUserId(Long roomId, String userId);
    List<ChatJoin> findAllByRoomId(Long roomId);
    int countByRoomId(Long roomId);

    // 채팅방에 사용자 존재 유무 판단
    boolean existsByRoomIdAndUserId(Long roomId, String userId);
}
