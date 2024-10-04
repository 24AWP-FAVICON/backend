package com.example.demo.entity.community.post;

import com.example.demo.entity.users.user.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 게시글에 대한 좋아요 정보를 나타내는 엔티티 클래스.
 * 사용자가 특정 게시글에 좋아요를 눌렀을 때의 정보를 저장합니다.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class PostLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 좋아요 ID

    @ManyToOne(fetch = FetchType.LAZY)
    private User user; // 좋아요를 누른 사용자

    @JsonBackReference // 순환 참조 방지
    @ManyToOne(fetch = FetchType.LAZY)
    private Post post; // 좋아요가 눌린 게시글

    private LocalDateTime createdAt; // 좋아요가 생성된 시간

    /**
     * 좋아요 엔티티를 생성하는 프라이빗 생성자.
     * @param user 좋아요를 누른 사용자
     * @param post 좋아요가 눌린 게시글
     * @param createdAt 좋아요 생성 시간
     */
    private PostLike(User user, Post post, LocalDateTime createdAt) {
        this.user = user;
        this.post = post;
        this.createdAt = createdAt;
    }

    /**
     * 좋아요 엔티티를 생성하는 정적 팩토리 메서드.
     * @param user 좋아요를 누른 사용자
     * @param post 좋아요가 눌린 게시글
     * @return 새롭게 생성된 PostLike 인스턴스
     */
    public static PostLike toEntity(User user, Post post) {
        return new PostLike(user, post, LocalDateTime.now());
    }
}
