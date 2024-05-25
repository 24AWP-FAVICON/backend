package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@ToString(callSuper = true)
@Entity
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId; //댓글 ID

    @JoinColumn(name = "userId")
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @JoinColumn(name = "postId")
    @JsonBackReference // 순환 참조 방지
    @ManyToOne(fetch = FetchType.LAZY)
    private Post post;

    @Builder.Default
    @Setter
    private Long parentCommentId = null; // 부모 댓글 ID

    @Setter
    private String content; // 본문

    @Setter
    private LocalDateTime createdAt;


    private Comment(Post post, User user, Long parentCommentId, String content) {
        this.post = post;
        this.user = user;
        this.parentCommentId = parentCommentId;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }

    public static Comment of(Post post, User user, String content, Long parentCommentId) {
        return new Comment(post, user, parentCommentId, content);
    }

}
