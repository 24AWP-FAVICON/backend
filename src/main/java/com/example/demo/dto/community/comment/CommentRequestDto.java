package com.example.demo.dto.community.comment;

import com.example.demo.entity.community.comment.Comment;
import com.example.demo.entity.community.post.Post;
import com.example.demo.entity.users.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommentRequestDto {
    private Long parentCommentId;
    private String content;

    public static CommentRequestDto of(String content) {
        return new CommentRequestDto(null, content);
    }

    public static CommentRequestDto of(Long parentCommentId, String content) {
        return new CommentRequestDto(parentCommentId, content);
    }

    public static Comment toEntity(Post post, User user, String content, Long parentCommentId) {
        return Comment.of(
                post,
                user,
                content,
                parentCommentId
        );
    }
}
