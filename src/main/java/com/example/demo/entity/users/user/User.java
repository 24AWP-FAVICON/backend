package com.example.demo.entity.users.user;

import com.example.demo.entity.community.block.Block;
import com.example.demo.entity.community.follow.Follow;
import com.example.demo.entity.community.post.Post;
import com.example.demo.entity.planner.Trip;
import com.example.demo.entity.users.Alarm.AlarmSettings;
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
public class User {

    @Id
    @Column
    private String userId;

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
    private List<Trip> tripList;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<Post> postList;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<Follow> followerList;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<Follow> followingList;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<Block> blockList;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "alarmSettingsId", referencedColumnName = "alarmSettingsId")
    private AlarmSettings alarmSettings;
}
