package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@ToString
public class User {

    @Id
    @Column(length = 20)
    private String googleId;

    @Column(nullable = false, length = 20, unique = true)
    private String nickname;

    @Column(nullable = false)
    private LocalDateTime recentConnect;

    @Column(nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    private Role role;

    private LocalDate deleteAt = null;

    @Column(nullable = false)
    private LocalDate createdAt;

}
