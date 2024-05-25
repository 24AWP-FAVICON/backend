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
public class View {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @JsonBackReference // 순환 참조 방지
    @ManyToOne(fetch = FetchType.LAZY)
    private Post post;

    private LocalDateTime createdAt;

    private View(User user, Post post, LocalDateTime createdAt) {
        this.user = user;
        this.post = post;
        this.createdAt = createdAt;
    }

    public static View toEntity(User user, Post post){
        return new View(user,post,LocalDateTime.now());
    }
}
