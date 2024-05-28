package com.example.demo.dto.messenger;

import com.example.demo.entity.messenger.ChatRoom;
import com.example.demo.entity.users.user.User;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ChatRoomRequestDTO {


    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Getter
    public static class CreateDTO {
        private String name;
        private String creatorUserId;
        private List<String> participantIds;

        public ChatRoom toEntity(User user){
            return ChatRoom.builder()
                    .name(name)
                    .createAt(LocalDateTime.now())
                    .build();
        }
    }

    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Getter
    public static class UpdateDTO {
        private String name;
        private String creatorUserId;
    }

    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Getter
    public static class InviteDTO {
        private String inviteUserId;

        public InviteDTO(String inviteUserId) {
            this.inviteUserId = inviteUserId;
        }
    }
}
