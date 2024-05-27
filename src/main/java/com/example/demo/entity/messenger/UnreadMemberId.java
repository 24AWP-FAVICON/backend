package com.example.demo.entity.messenger;

import lombok.*;

import java.io.Serializable;
import java.util.Objects;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UnreadMemberId implements Serializable {
    // UnreadMember 테이블 복합 키 클래스
    private Long msgId;
    private String userId;

    @Override
    public int hashCode() {
        return Objects.hash(msgId, userId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnreadMemberId that = (UnreadMemberId) o;
        return Objects.equals(msgId, that.msgId) && Objects.equals(userId, that.userId);
    }
}
