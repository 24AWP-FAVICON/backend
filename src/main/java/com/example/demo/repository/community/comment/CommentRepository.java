package com.example.demo.repository;


import com.example.demo.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Transactional
    @Query("SELECT c FROM Comment c WHERE c.user.userId = :userId")
    List<Comment> findByUserId(@Param("userId") String userId);


    Optional<Comment> findByCommentId(Long commentId);

    void deleteByCommentId(Long commentId);

    @Transactional
    @Query("SELECT c FROM Comment c WHERE c.post.postId = :postId")
    List<Comment> findByPostId(@Param("postId") Long postId);

}
