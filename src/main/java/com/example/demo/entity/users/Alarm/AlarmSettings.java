package com.example.demo.entity.users.Alarm;

import com.example.demo.dto.users.alarm.AlarmSettingsDto;
import com.example.demo.entity.users.user.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

/**
 * 사용자 알림 설정을 나타내는 엔티티 클래스.
 * 사용자가 수신할 알림의 유형에 대한 설정을 저장합니다.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class AlarmSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long alarmSettingsId; // 알림 설정 ID

    @JsonBackReference
    @OneToOne(mappedBy = "alarmSettings", cascade = CascadeType.ALL)
    private User user; // 이 설정을 사용하는 사용자

    private boolean announcementAlarm = true; // 공지 알림 여부

    private boolean inquiryAlarm = true; // 문의 알림 여부

    private boolean postAlarm = true; // 게시글 알림 여부

    private boolean commentAlarm = true; // 댓글 알림 여부

    private boolean likeAlarm = true; // 좋아요 알림 여부

    private boolean messageAlarm = true; // 메시지 알림 여부

    /**
     * AlarmSettingsDto를 사용하여 AlarmSettings 인스턴스를 업데이트합니다.
     *
     * @param alarmSettings 기존 AlarmSettings 인스턴스
     * @param alarmSettingsDto 업데이트할 알림 설정 DTO
     * @return 업데이트된 AlarmSettings 인스턴스
     */
    public static AlarmSettings fromDto(AlarmSettings alarmSettings, AlarmSettingsDto alarmSettingsDto) {
        alarmSettings.setCommentAlarm(alarmSettingsDto.isCommentAlarm());
        alarmSettings.setPostAlarm(alarmSettingsDto.isPostAlarm());
        alarmSettings.setLikeAlarm(alarmSettingsDto.isLikeAlarm());
        alarmSettings.setMessageAlarm(alarmSettingsDto.isMessageAlarm());
        alarmSettings.setAnnouncementAlarm(alarmSettingsDto.isAnnouncementAlarm());
        alarmSettings.setInquiryAlarm(alarmSettingsDto.isInquiryAlarm());
        return alarmSettings;
    }

    /**
     * AlarmSettings 인스턴스를 AlarmSettingsDto로 변환합니다.
     *
     * @param alarmSettings 변환할 AlarmSettings 인스턴스
     * @return 변환된 AlarmSettingsDto 객체
     */
    public static AlarmSettingsDto toDto(AlarmSettings alarmSettings) {
        return new AlarmSettingsDto(
                alarmSettings.isAnnouncementAlarm(),
                alarmSettings.isInquiryAlarm(),
                alarmSettings.isPostAlarm(),
                alarmSettings.isCommentAlarm(),
                alarmSettings.isLikeAlarm(),
                alarmSettings.isMessageAlarm()
        );
    }
}
