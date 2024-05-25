package com.example.demo.entity.community.post;

import com.example.demo.entity.users.user.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class PostLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @JsonBackReference // 순환 참조 방지
    @ManyToOne(fetch = FetchType.LAZY)
    private Post post;

    private LocalDateTime createdAt;


    private PostLike(User user, Post post, LocalDateTime createdAt) {
        this.user = user;
        this.post = post;
        this.createdAt = createdAt;
    }

    public static PostLike toEntity(User user, Post post){
        return new PostLike(user,post,LocalDateTime.now());
    }
}
