package com.example.demo.entity.community.comment;

import com.example.demo.entity.community.post.Post;
import com.example.demo.entity.users.user.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 댓글을 나타내는 엔티티 클래스.
 * 댓글은 특정 사용자에 의해 특정 게시물에 작성되며, 부모 댓글을 가질 수 있습니다.
 */
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
    private Long commentId; // 댓글 ID

    @JoinColumn(name = "userId")
    @ManyToOne(fetch = FetchType.LAZY)
    private User user; // 댓글 작성 사용자

    @JoinColumn(name = "postId")
    @JsonBackReference // 순환 참조 방지
    @ManyToOne(fetch = FetchType.LAZY)
    private Post post; // 댓글이 작성된 게시물

    @Builder.Default
    @Setter
    private Long parentCommentId = null; // 부모 댓글 ID

    @Setter
    private String content; // 댓글 본문

    @Setter
    private LocalDateTime createdAt; // 댓글 생성 시간

    /**
     * 댓글 객체를 생성하는 생성자.
     *
     * @param post           댓글이 작성된 게시물
     * @param user           댓글 작성 사용자
     * @param parentCommentId 부모 댓글 ID (없으면 null)
     * @param content        댓글 본문
     */
    private Comment(Post post, User user, Long parentCommentId, String content) {
        this.post = post;
        this.user = user;
        this.parentCommentId = parentCommentId;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 새 Comment 객체를 생성하는 정적 팩토리 메서드.
     *
     * @param post           댓글이 작성될 게시물
     * @param user           댓글 작성 사용자
     * @param content        댓글 본문
     * @param parentCommentId 부모 댓글 ID (없으면 null)
     * @return 새로 생성된 Comment 객체
     */
    public static Comment of(Post post, User user, String content, Long parentCommentId) {
        return new Comment(post, user, parentCommentId, content);
    }
}
