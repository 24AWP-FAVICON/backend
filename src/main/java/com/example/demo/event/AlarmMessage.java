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
    private String receiveUserId;
    private AlarmType alarmType;
    private AlarmArgs alarmArgs;
    private LocalDateTime createdAt;
    private String text;

    public static AlarmMessage fromEntity(AlarmEntity entity){
        return new AlarmMessage(
                entity.getUser().getUserId(),
                entity.getAlarmType(),
                entity.getArgs(),
                entity.getCreatedAt()
                ,entity.getText()
        );
    }
}
