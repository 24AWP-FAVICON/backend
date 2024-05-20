package com.example.demo.repository;

import com.example.demo.entity.Post;
import com.example.demo.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    Set<PostLike> getPostLikeByPost(Post post);
    @Transactional
    @Modifying
    @Query("DELETE FROM PostLike pl WHERE pl.post.postId = :postId AND pl.user.userId = :userId")
    void deleteByPostId(@Param("postId") Long postId, @Param("userId") String userId);
}
