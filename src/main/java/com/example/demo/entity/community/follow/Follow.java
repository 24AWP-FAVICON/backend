package com.example.demo.entity.community.follow;

import com.example.demo.entity.users.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 사용자 간의 팔로우 관계를 나타내는 엔티티 클래스.
 * 사용자가 다른 사용자를 팔로우할 때 이 객체가 생성됩니다.
 */
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@Entity
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long followId; // 팔로우 ID

    @ManyToOne
    @JoinColumn(name = "userId", referencedColumnName = "userId")
    private User user; // 팔로우하는 사용자

    @ManyToOne
    @JoinColumn(name = "followingUserId", referencedColumnName = "userId")
    private User followingUser; // 팔로우 당하는 사용자

    private LocalDateTime createdAt; // 팔로우 생성 시간

    /**
     * 팔로우 객체를 생성하는 정적 팩토리 메서드.
     *
     * @param user           팔로우하는 사용자
     * @param followingUser  팔로우 당하는 사용자
     * @return 새로 생성된 Follow 객체
     */
    public static Follow of(User user, User followingUser) {
        Follow follow = new Follow();
        follow.setUser(user);
        follow.setFollowingUser(followingUser);
        follow.setCreatedAt(LocalDateTime.now());
        return follow;
    }
}
