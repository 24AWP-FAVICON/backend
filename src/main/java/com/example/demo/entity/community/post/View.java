package com.example.demo.entity.community.post;

import com.example.demo.entity.users.user.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 게시글 조회 정보를 나타내는 엔티티 클래스.
 * 사용자가 특정 게시글을 조회했을 때의 정보를 저장합니다.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class View {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 조회 ID

    @ManyToOne(fetch = FetchType.LAZY)
    private User user; // 조회한 사용자

    @JsonBackReference // 순환 참조 방지
    @ManyToOne(fetch = FetchType.LAZY)
    private Post post; // 조회된 게시글

    private LocalDateTime createdAt; // 조회가 생성된 시간

    /**
     * 조회 엔티티를 생성하는 프라이빗 생성자.
     * @param user 조회한 사용자
     * @param post 조회된 게시글
     * @param createdAt 조회 생성 시간
     */
    private View(User user, Post post, LocalDateTime createdAt) {
        this.user = user;
        this.post = post;
        this.createdAt = createdAt;
    }

    /**
     * 조회 엔티티를 생성하는 정적 팩토리 메서드.
     * @param user 조회한 사용자
     * @param post 조회된 게시글
     * @return 새롭게 생성된 View 인스턴스
     */
    public static View toEntity(User user, Post post) {
        return new View(user, post, LocalDateTime.now());
    }
}
