package com.example.demo.entity.users.Alarm;

import com.example.demo.dto.users.alarm.AlarmSettingsDto;
import com.example.demo.entity.users.user.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class AlarmSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long alarmSettingsId;

    @JsonBackReference
    @OneToOne(mappedBy = "alarmSettings", cascade = CascadeType.ALL)
    private User user;

    private boolean announcementAlarm = true;

    private boolean inquiryAlarm = true;

    private boolean postAlarm = true;

    private boolean commentAlarm = true;

    private boolean likeAlarm = true;

    private boolean messageAlarm = true;

    public static AlarmSettings fromDto(AlarmSettings alarmSettings,
                                        AlarmSettingsDto alarmSettingsDto) {
        alarmSettings.setCommentAlarm(alarmSettingsDto.isCommentAlarm());
        alarmSettings.setPostAlarm(alarmSettingsDto.isPostAlarm());
        alarmSettings.setLikeAlarm(alarmSettingsDto.isLikeAlarm());
        alarmSettings.setMessageAlarm(alarmSettingsDto.isMessageAlarm());
        alarmSettings.setAnnouncementAlarm(alarmSettingsDto.isAnnouncementAlarm());
        alarmSettings.setInquiryAlarm(alarmSettingsDto.isInquiryAlarm());
        return alarmSettings;
    }

    public static AlarmSettingsDto toDto(AlarmSettings alarmSettings){
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
