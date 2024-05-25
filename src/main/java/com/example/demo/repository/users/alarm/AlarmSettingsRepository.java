package com.example.demo.repository.users.alarm;

import com.example.demo.entity.users.Alarm.AlarmSettings;
import com.example.demo.entity.users.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlarmSettingsRepository extends JpaRepository<AlarmSettings,Long> {

    public AlarmSettings findByUser(User user);
}
