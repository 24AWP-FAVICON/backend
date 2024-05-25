package com.example.demo.dto.users.alarm;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AlarmSettingsDto {
    private boolean announcementAlarm;
    private boolean inquiryAlarm;
    private boolean postAlarm;
    private boolean commentAlarm;
    private boolean likeAlarm;
    private boolean messageAlarm;

}
