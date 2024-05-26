package com.example.demo.entity.messenger;

import com.example.demo.entity.users.user.User;
import jakarta.persistence.*;
import lombok.*;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@IdClass(UnreadMemberId.class)
public class UnreadMember {
    @Id
    private Long msgId;

    @Id
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "msgId", insertable = false, updatable = false)
    private Message message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    private User user;
}
