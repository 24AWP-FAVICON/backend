package com.example.demo.controller.user;

import com.example.demo.dto.users.alarm.AlarmSettingsDto;
import com.example.demo.entity.users.user.User;
import com.example.demo.event.AlarmMessage;
import com.example.demo.exception.AlarmConnectionException;
import com.example.demo.service.users.alarm.AlarmService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * AlarmController는 사용자 알람 관련 기능을 제공하는 컨트롤러입니다.
 * 알람 구독, 알람 설정 조회 및 수정, 알람 삭제 등의 기능을 포함합니다.
 */
@RequiredArgsConstructor
@RestController
public class AlarmController {

    private final AlarmService alarmService;

    /**
     * 사용자가 서버로부터 실시간 알람을 구독합니다.
     *
     * @param authentication 인증된 사용자 정보
     * @param lastEventId    마지막으로 수신한 이벤트 ID (재연결을 위해 사용)
     * @return 서버로부터 실시간 알람 스트림을 제공하는 SseEmitter 객체
     * @throws AlarmConnectionException 알람 연결에 실패할 경우 발생하는 예외
     */
    @GetMapping(value = "/users/alarm/subscribe", produces = "text/event-stream")
    public ResponseEntity<SseEmitter> subscribeAlarm(Authentication authentication,
                                                     @RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") String lastEventId) throws AlarmConnectionException {

        String userId = ((User) authentication.getPrincipal()).getUserId();
        return ResponseEntity.ok(alarmService.subscribeAlarm(userId, lastEventId));
    }

    /**
     * 사용자가 받은 모든 알람을 조회합니다.
     *
     * @param authentication 인증된 사용자 정보
     * @return 사용자에게 전달된 모든 알람 리스트
     */
    @GetMapping("/users/alarm")
    public ResponseEntity<List<AlarmMessage>> getAllAlarms(
            Authentication authentication) {

        String userId = ((User) authentication.getPrincipal()).getUserId();
        return ResponseEntity.ok(alarmService.getAlarmList(userId));
    }

    /**
     * 특정 알람을 삭제합니다.
     *
     * @param alarmId  삭제할 알람 ID
     * @return 삭제 성공 메시지를 반환
     */
    @DeleteMapping("/users/alarm/{alarmId}")
    public ResponseEntity<String> deleteAlarmByAlarmId(@PathVariable Long alarmId) {
        alarmService.deleteAlarm(alarmId);
        return ResponseEntity.ok("DELETE_SUCCESS");
    }

    /**
     * 사용자의 알람 설정을 수정합니다.
     *
     * @param alarmSettingsDto 수정할 알람 설정 정보
     * @param authentication   인증된 사용자 정보
     * @return 수정된 알람 설정 정보를 반환
     */
    @PatchMapping("/users/alarm-setting")
    public ResponseEntity<AlarmSettingsDto> updateAlarmSettings(@RequestBody AlarmSettingsDto alarmSettingsDto,
                                                                Authentication authentication) {

        String userId = ((User) authentication.getPrincipal()).getUserId();
        return ResponseEntity.ok(alarmService.updateAlarmSettings(userId, alarmSettingsDto));
    }

    /**
     * 사용자의 알람 설정을 조회합니다.
     *
     * @param authentication 인증된 사용자 정보
     * @return 사용자의 알람 설정 정보를 반환
     */
    @GetMapping("/users/alarm-setting")
    public ResponseEntity<AlarmSettingsDto> getAlarmSettings(Authentication authentication) {

        String userId = ((User) authentication.getPrincipal()).getUserId();
        return ResponseEntity.ok(alarmService.getAlarmSettings(userId));
    }
}
