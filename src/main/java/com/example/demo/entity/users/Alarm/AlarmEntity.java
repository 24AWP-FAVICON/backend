package com.example.demo.entity.users.Alarm;

import com.example.demo.entity.users.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * 알림 정보를 나타내는 엔티티 클래스.
 * 사용자에게 전송된 알림의 유형, 내용 및 생성 시간을 저장합니다.
 */
@RequiredArgsConstructor
@Getter
@Setter
@Entity
public class AlarmEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 알림 ID

    @ManyToOne
    @JoinColumn(name = "userId")
    private User user; // 알림을 수신하는 사용자

    @Enumerated(EnumType.STRING)
    private AlarmType alarmType; // 알림 유형

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private AlarmArgs args; // 알림에 포함된 추가 정보

    private LocalDateTime createdAt; // 알림 생성 시간

    private String text; // 알림 텍스트 내용

    /**
     * AlarmEntity의 인스턴스를 생성하는 정적 메서드.
     *
     * @param user 사용자 정보
     * @param alarmType 알림 유형
     * @param alarmArgs 알림 추가 정보
     * @return 생성된 AlarmEntity 객체
     */
    public static AlarmEntity of(User user, AlarmType alarmType, AlarmArgs alarmArgs) {
        AlarmEntity entity = new AlarmEntity();
        entity.setUser(user);
        entity.setAlarmType(alarmType);
        entity.setArgs(alarmArgs);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setText(alarmArgs.getFromUserId() + " adds " + alarmType.getAlarmText());
        return entity;
    }
}
