package com.example.demo.controller.user;


import com.example.demo.dto.users.alarm.AlarmSettingsDto;
import com.example.demo.event.AlarmMessage;
import com.example.demo.exception.AlarmConnectionException;
import com.example.demo.service.users.alarm.AlarmService;
import com.example.demo.service.jwt.JwtCheckService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class AlarmController {

    private final AlarmService alarmService;
    private final JwtCheckService jwtCheckService;

    @GetMapping(value = "/users/alarm/subscribe", produces = "text/event-stream")
    public ResponseEntity<SseEmitter> subscribeAlarm(HttpServletRequest request,
                                                     HttpServletResponse response,
                                                     @RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") String lastEventId) throws AlarmConnectionException {

        String userId = jwtCheckService.checkJwt(request, response);
        return ResponseEntity.ok(alarmService.subscribeAlarm(userId, lastEventId));
    }

    @GetMapping("/users/alarm")
    public ResponseEntity<List<AlarmMessage>> getAllAlarms(
            HttpServletRequest request,
            HttpServletResponse response) {

        String userId = jwtCheckService.checkJwt(request, response);

        return ResponseEntity.ok(alarmService.getAlarmList(userId));
    }

    @DeleteMapping("/users/alarm/{alarmId}")
    public ResponseEntity<String> deleteAlarmByAlarmId(@PathVariable Long alarmId,
                                                       HttpServletRequest request,
                                                       HttpServletResponse response) {
        jwtCheckService.checkJwt(request, response);
        alarmService.deleteAlarm(alarmId);
        return ResponseEntity.ok("DELETE_SUCCESS");
    }

    @PatchMapping("/users/alarm-setting")
    public ResponseEntity<AlarmSettingsDto> updateAlarmSettings(@RequestBody AlarmSettingsDto alarmSettingsDto,
                                                                HttpServletRequest request,
                                                                HttpServletResponse response) {

        String userId = jwtCheckService.checkJwt(request, response);
        return ResponseEntity.ok(alarmService.updateAlarmSettings(userId, alarmSettingsDto));
    }

    @GetMapping("/users/alarm-setting")
    public ResponseEntity<AlarmSettingsDto> getAlarmSettings(HttpServletRequest request,
                                                             HttpServletResponse response) {

        String userId = jwtCheckService.checkJwt(request, response);
        return ResponseEntity.ok(alarmService.getAlarmSettings(userId));
    }
}
