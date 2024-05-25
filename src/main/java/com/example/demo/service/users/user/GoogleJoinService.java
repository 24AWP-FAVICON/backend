package com.example.demo.service;

import com.example.demo.dto.JoinGoogleUserDTO;
import com.example.demo.entity.AlarmSettings;
import com.example.demo.entity.User;
import com.example.demo.entity.Role;
import com.example.demo.exception.ComponentNotFoundException;
import com.example.demo.repository.AlarmSettingsRepository;
import com.example.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleJoinService {
    private final UserRepository userRepository;
    private final AlarmSettingsRepository alarmSettingsRepository;

    /**
     * 구글 회원 회원가입 로직
     */

    @Transactional
    public boolean joinGoogleProcess(JoinGoogleUserDTO joinGoogleUserDTO) {
        User newUser = setGoogleUserEntity(joinGoogleUserDTO);
        AlarmSettings alarmSettings = new AlarmSettings();
        alarmSettings.setUser(newUser);
        newUser.setAlarmSettings(alarmSettings);

        log.info(newUser.getUserId());

        try {
            alarmSettingsRepository.save(alarmSettings);
            userRepository.save(newUser);
        } catch (Exception e) {
            log.error(e.toString());
            return false;
        }
        return true;
    }

    /**
     * 회원 엔티티 생성
     */
    private static User setGoogleUserEntity(JoinGoogleUserDTO joinGoogleUserDTO) {
        User newUser = new User();
        newUser.setNickname(joinGoogleUserDTO.getNickname());
        newUser.setUserId(joinGoogleUserDTO.getUserId());
        newUser.setRecentConnect(LocalDateTime.now());
        newUser.setCreatedAt(LocalDate.now());
        newUser.setRole(Role.valueOf(joinGoogleUserDTO.getRole()));
        newUser.setDeleteAt(null);

        return newUser;
    }


    public void updateGoogleUser(JoinGoogleUserDTO joinGoogleUserDTO) {
        User user = userRepository.findById(joinGoogleUserDTO.getUserId()).orElseThrow(() -> new ComponentNotFoundException("USER_NOT_FOUND"));
        user.setNickname(joinGoogleUserDTO.getNickname());
        userRepository.save(user);
    }
}
