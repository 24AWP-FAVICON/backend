package com.example.demo.repository.users.alarm;

import com.example.demo.entity.users.Alarm.AlarmEntity;
import com.example.demo.entity.users.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlarmEntityRepository extends JpaRepository<AlarmEntity,Long> {
    List<AlarmEntity> findAllByUser(User user);
}
