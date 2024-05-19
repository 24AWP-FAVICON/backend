package com.example.demo.dto.comment;

public record CommentRequestDto(
        Long parentCommentId,
        String content
) {

    public static CommentRequestDto of(String content) {
        return CommentRequestDto.of (null, content);
    }

    public static CommentRequestDto of(Long parentCommentId, String content) {
        return new CommentRequestDto(parentCommentId, content);
    }

    public CommentDto toDto(String userId, Long postId) {
        return CommentDto.of(
                postId,
                userId,
                parentCommentId,
                content
        );
    }

}
