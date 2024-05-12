package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter @Setter
@ToString
public class User {

    @Id
    @Column(length = 20)
    private String googleId;

    @Column(nullable = false, length = 50, unique = true)
    private String nickname;

    @Column(nullable = false)
    private LocalDateTime recentConnect;

    @Column(nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    private Role role;

    private LocalDate deleteAt = null;

    @Column(nullable = false)
    private LocalDate createdAt;

    @ManyToMany(mappedBy = "participants")
    @JsonIgnore
    private List<Trip> trips;
}
