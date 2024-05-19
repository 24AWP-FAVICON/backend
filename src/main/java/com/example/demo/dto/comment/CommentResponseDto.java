package com.example.demo.dto.comment;

import com.example.demo.entity.Comment;
import com.example.demo.repository.CommentRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class CommentResponseDto {

    private Long commentId;
    private Long parentCommentId;
    private Long postId;
    private String userId;
    private String content;
    private List<CommentResponseDto> childComments;

    public static CommentResponseDto toDto(Comment comment) {
        return new CommentResponseDto(
                comment.getCommentId(),
                comment.getParentCommentId(),
                comment.getPost().getPostId(),
                comment.getUser().getUserId(),
                comment.getContent(),
                new ArrayList<>()
        );
    }

}