package com.example.demo.event;

import com.example.demo.entity.users.Alarm.AlarmArgs;
import com.example.demo.entity.users.Alarm.AlarmEntity;
import com.example.demo.entity.users.Alarm.AlarmType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlarmMessage {
    private Long id; // 추가
    private String receiveUserId;
    private AlarmType alarmType;
    private AlarmArgs alarmArgs;
    private LocalDateTime createdAt;
    private String text;

    // 새 생성자 추가
    public AlarmMessage(String receiveUserId, AlarmType alarmType, AlarmArgs alarmArgs, LocalDateTime createdAt, String text) {
        this.receiveUserId = receiveUserId;
        this.alarmType = alarmType;
        this.alarmArgs = alarmArgs;
        this.createdAt = createdAt;
        this.text = text;
    }

    public static AlarmMessage fromEntity(AlarmEntity entity){
        return new AlarmMessage(
                entity.getId(), // 추가
                entity.getUser().getUserId(),
                entity.getAlarmType(),
                entity.getArgs(),
                entity.getCreatedAt(),
                entity.getText()
        );
    }
}
