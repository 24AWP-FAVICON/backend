package com.example.demo.repository.community.follow;

import com.example.demo.entity.community.follow.Follow;
import com.example.demo.entity.users.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    List<Follow> findByUser(User user);
    List<Follow> findByFollowingUser(User user);

    @Transactional
    @Query("SELECT f FROM Follow f WHERE f.user.userId = :userId AND f.followingUser.userId = :followingUserId")
    Optional<Follow> findByUserAndFollowingUser(@Param("userId") String userId, @Param("followingUserId") String followingUserId);

    @Transactional
    void deleteByUser_UserIdAndFollowingUser_UserId(String userId, String followingUserId);

}
