package com.example.demo.repository.community.block;

import com.example.demo.entity.community.block.Block;
import com.example.demo.entity.users.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface BlockRepository extends JpaRepository<Block,Long> {
    List<Block> findByUser(User user);

    @Transactional
    @Query("SELECT b FROM Block b WHERE b.user.userId = :userId AND b.blockingUser.userId = :blockingUserId")
    Optional<Block> findByUserIdAndBlockingUserId(@Param("userId") String userId, @Param("blockingUserId") String blockingUserId);

    @Transactional
    void deleteByUser_UserIdAndBlockingUser_UserId(String userId, String blockingUserId);

}
