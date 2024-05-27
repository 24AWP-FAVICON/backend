package com.example.demo.repository.messenger;


import com.example.demo.entity.messenger.UnreadMember;
import com.example.demo.entity.messenger.UnreadMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UnreadMemberRepository extends JpaRepository<UnreadMember, UnreadMemberId> {

    @Query("SELECT um " +
            "FROM UnreadMember um " +
            "WHERE um.chatMessage.room.roomId = :roomId " +
            "AND um.user.userId = :userId")
    List<UnreadMember> findByRoomIdAndUserId(@Param("roomId") Long roomId, @Param("userId") String userId);
}
