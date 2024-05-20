package com.example.demo.dto.comment;

import com.example.demo.entity.Comment;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;

public record CommentDto(
        Long commentId,
        Long postId,
        String userId,
        Long parentCommentId,
        String content
) {

    public static CommentDto of(Long postId, String userId, String content) {
        return CommentDto.of(postId, userId, null, content);
    }

    public static CommentDto of(Long postId, String userId, Long parentCommentId, String content) {
        return CommentDto.of(null, postId, userId, parentCommentId, content);
    }

    public static CommentDto of(Long commentId, Long postId, String userId, Long parentCommentId, String content) {
        return new CommentDto(commentId, postId, userId, parentCommentId, content);
    }

    public static CommentDto from(Comment entity) {
        return new CommentDto(
                entity.getCommentId(),
                entity.getPost().getPostId(),
                entity.getUser().getUserId(),
                entity.getParentCommentId(),
                entity.getContent()
        );
    }

    public Comment toEntity(Post post, User user, Long parentCommentId) {
        return Comment.of(
                post,
                user,
                content,
                parentCommentId
        );
    }

}
