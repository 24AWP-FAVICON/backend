package com.example.demo.dto.messenger;

import com.example.demo.entity.messenger.ChatRoom;
import com.example.demo.entity.users.user.User;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
public class ChatRoomRequestDTO {

    // 기본 생성자 수동 정의
    public ChatRoomRequestDTO() {
        // 필요한 초기화 작업이 있으면 여기에 추가
    }
    @Getter
    @Builder // Builder 패턴 사용
    public static class CreateDTO {
        private String name;
        private String creatorUserId;
        private List<String> participantIds;

        public ChatRoom toEntity(User user) {
            return ChatRoom.builder()
                    .name(name)
                    .createAt(LocalDateTime.now())
                    .build();
        }
    }

    @Getter
    public static class UpdateDTO {
        private String name;
        private String creatorUserId;
    }

    @Getter
    @NoArgsConstructor
    public static class InviteDTO {
        private String inviteUserId;

        public InviteDTO(String inviteUserId) {
            this.inviteUserId = inviteUserId;
        }
    }
}
