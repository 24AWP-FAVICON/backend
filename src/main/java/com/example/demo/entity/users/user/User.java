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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 사용자 정보를 나타내는 엔티티 클래스.
 * 사용자의 기본 정보, 작성한 게시글, 팔로우/차단 관계 및 알림 설정을 저장합니다.
 */
@Entity
@Getter
@Setter
public class User {

    @Id
    @Column
    private String userId; // 사용자 ID

    @Column(nullable = false, length = 50, unique = true)
    private String nickname; // 사용자 닉네임

    @Column(nullable = false)
    private LocalDateTime recentConnect; // 최근 접속 시간

    @Column(nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    private Role role; // 사용자 역할

    private LocalDate deleteAt = null; // 삭제 예정 날짜

    @Column(nullable = false)
    private LocalDate createdAt; // 사용자 생성 날짜

    @ManyToMany(mappedBy = "participants")
    @JsonIgnore // JSON 직렬화 시 무시
    private List<Trip> tripList; // 사용자가 참여한 여행 목록

    @OneToMany(mappedBy = "user")
    @JsonIgnore // JSON 직렬화 시 무시
    private List<Post> postList; // 사용자가 작성한 게시글 목록

    @OneToMany(mappedBy = "user")
    @JsonIgnore // JSON 직렬화 시 무시
    private List<Follow> followerList; // 사용자를 팔로우하는 목록

    @OneToMany(mappedBy = "user")
    @JsonIgnore // JSON 직렬화 시 무시
    private List<Follow> followingList; // 사용자가 팔로우하는 목록

    @OneToMany(mappedBy = "user")
    @JsonIgnore // JSON 직렬화 시 무시
    private List<Block> blockList; // 사용자가 차단한 목록

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "alarmSettingsId", referencedColumnName = "alarmSettingsId")
    private AlarmSettings alarmSettings; // 사용자 알림 설정
}
