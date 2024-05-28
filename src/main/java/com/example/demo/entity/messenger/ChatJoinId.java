package com.example.demo.entity.messenger;


import java.io.Serializable;
import java.util.Objects;

public class ChatJoinId implements Serializable {
    private String userId;
    private Long roomId;

    public ChatJoinId() {}

    @Override
    public int hashCode() {
        return Objects.hash(userId, roomId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatJoinId that = (ChatJoinId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(roomId, that.roomId);
    }
}
