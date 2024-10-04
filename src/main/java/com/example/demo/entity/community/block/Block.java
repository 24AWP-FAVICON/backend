package com.example.demo.entity.community.block;

import com.example.demo.entity.users.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 사용자가 다른 사용자를 차단하는 정보를 저장하는 엔티티 클래스.
 * 차단된 사용자, 차단한 사용자 및 차단 사유를 포함합니다.
 */
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@Entity
@Table(name = "block")
public class Block {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long blockId; // 차단 ID

    @ManyToOne
    @JoinColumn(name = "userId", referencedColumnName = "userId")
    private User user; // 차단된 사용자

    @ManyToOne
    @JoinColumn(name = "blockingUserId", referencedColumnName = "userId")
    private User blockingUser; // 차단한 사용자

    private String blockReason; // 차단 사유

    private LocalDateTime createdAt; // 차단 생성 시간

    /**
     * 새 Block 객체를 생성하는 정적 팩토리 메서드.
     *
     * @param user          차단된 사용자
     * @param blockingUser  차단한 사용자
     * @param blockReason   차단 사유
     * @return 새로 생성된 Block 객체
     */
    public static Block of(User user, User blockingUser, String blockReason) {
        Block block = new Block();
        block.setUser(user);
        block.setBlockingUser(blockingUser);
        block.setBlockReason(blockReason);
        block.setCreatedAt(LocalDateTime.now());
        return block;
    }
}
