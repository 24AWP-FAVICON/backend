package com.example.demo.entity.users.Alarm;

import com.example.demo.entity.users.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Getter @Setter
@Entity
public class AlarmEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="userId")
    private User user;

    @Enumerated(EnumType.STRING)
    private AlarmType alarmType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private AlarmArgs args;

    private LocalDateTime createdAt;

    private String text;

    public static AlarmEntity of(User user, AlarmType alarmType, AlarmArgs alarmArgs){
        AlarmEntity entity = new AlarmEntity();
        entity.setUser(user);
        entity.setAlarmType(alarmType);
        entity.setArgs(alarmArgs);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setText(alarmArgs.getFromUserId()+" adds "+ alarmType.getAlarmText());
        return entity;
    }

}
