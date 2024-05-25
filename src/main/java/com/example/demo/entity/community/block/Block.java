package com.example.demo.entity.community.block;

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
@Table(name = "block")
public class Block {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long blockId;

    @ManyToOne
    @JoinColumn(name = "userId", referencedColumnName = "userId")
    private User user;

    @ManyToOne
    @JoinColumn(name = "blockingUserId", referencedColumnName = "userId")
    private User blockingUser;

    private String blockReason;

    private LocalDateTime createdAt;

    public static Block of(User user, User blockingUser, String blockReason) {
        Block block = new Block();
        block.setUser(user);
        block.setBlockingUser(blockingUser);
        block.setBlockReason(blockReason);
        block.setCreatedAt(LocalDateTime.now());
        return block;
    }
}
