package com.example.demo.service.users.user;

import com.example.demo.dto.users.user.JoinGoogleUserDTO;
import com.example.demo.entity.users.Alarm.AlarmSettings;
import com.example.demo.entity.users.user.User;
import com.example.demo.entity.users.user.Role;
import com.example.demo.exception.ComponentNotFoundException;
import com.example.demo.repository.users.alarm.AlarmSettingsRepository;
import com.example.demo.repository.users.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Google 사용자 가입 및 업데이트 서비스를 제공하는 클래스.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleJoinService {
    private final UserRepository userRepository;
    private final AlarmSettingsRepository alarmSettingsRepository;

    /**
     * 구글 사용자 가입 프로세스를 수행합니다.
     *
     * @param joinGoogleUserDTO 구글 사용자 정보 DTO
     */
    @Transactional
    public void joinGoogleProcess(JoinGoogleUserDTO joinGoogleUserDTO) {
        User newUser = setGoogleUserEntity(joinGoogleUserDTO);
        AlarmSettings alarmSettings = new AlarmSettings();
        alarmSettings.setUser(newUser);
        newUser.setAlarmSettings(alarmSettings);

        log.info("New User Is Created. {}", newUser);

        try {
            alarmSettingsRepository.save(alarmSettings);
            userRepository.save(newUser);
        } catch (Exception e) {
            log.error(e.toString());
        }
    }

    /**
     * 새로운 사용자 엔티티를 생성합니다.
     *
     * @param joinGoogleUserDTO 구글 사용자 정보 DTO
     * @return 생성된 사용자 엔티티
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

    /**
     * 기존 구글 사용자의 정보를 업데이트합니다.
     *
     * @param joinGoogleUserDTO 구글 사용자 정보 DTO
     */
    public void updateGoogleUser(JoinGoogleUserDTO joinGoogleUserDTO) {
        User user = userRepository.findById(joinGoogleUserDTO.getUserId())
                .orElseThrow(() -> new ComponentNotFoundException("USER_NOT_FOUND"));
        user.setNickname(joinGoogleUserDTO.getNickname());
        userRepository.save(user);
    }
}
