package com.example.demo.entity.users.Alarm;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AlarmType {
    NEW_COMMENT("new comment"),
    NEW_LIKE("new like"),
    NEW_POST("new post"),
    NEW_MESSAGE("new message"),
    NEW_INQUIRY("new inquiry"),
    NEW_ANNOUNCEMENT("new announcement");

    private final String alarmText;
}
