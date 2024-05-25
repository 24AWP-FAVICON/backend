package com.example.demo.entity.community.follow;

import com.example.demo.entity.users.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@Entity
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long followId;

    @ManyToOne
    @JoinColumn(name = "userId", referencedColumnName = "userId")
    private User user;

    @ManyToOne
    @JoinColumn(name = "followingUserId", referencedColumnName = "userId")
    private User followingUser;

    private LocalDateTime createdAt;

    public static Follow of(User user, User followingUser) {
        Follow follow = new Follow();
        follow.setUser(user);
        follow.setFollowingUser(followingUser);
        follow.setCreatedAt(LocalDateTime.now());
        return follow;
    }


}
