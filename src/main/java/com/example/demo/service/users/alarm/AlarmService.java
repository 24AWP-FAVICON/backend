package com.example.demo.service.users.alarm;


import com.example.demo.dto.users.alarm.AlarmSettingsDto;
import com.example.demo.entity.community.follow.Follow;
import com.example.demo.entity.users.Alarm.AlarmArgs;
import com.example.demo.entity.users.Alarm.AlarmEntity;
import com.example.demo.entity.users.Alarm.AlarmSettings;
import com.example.demo.entity.users.Alarm.AlarmType;
import com.example.demo.entity.users.user.User;
import com.example.demo.event.AlarmMessage;
import com.example.demo.exception.AlarmConnectionException;
import com.example.demo.exception.ComponentNotFoundException;
import com.example.demo.producer.AlarmProducer;
import com.example.demo.repository.community.follow.FollowRepository;
import com.example.demo.repository.users.user.UserRepository;
import com.example.demo.repository.users.alarm.AlarmEntityRepository;
import com.example.demo.repository.users.alarm.AlarmSettingsRepository;
import com.example.demo.repository.users.alarm.EmitterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class AlarmService {

    private final static Long TIMEOUT = 60L * 60 * 1000;
    private final static String ALARM_NAME = "alarm";

    private final EmitterRepository emitterRepository;
    private final AlarmEntityRepository alarmEntityRepository;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final AlarmSettingsRepository alarmSettingsRepository;
    private final AlarmProducer alarmProducer;

    public void send(AlarmType type, AlarmArgs args, String receiverUserId) {
        User user = userRepository.findById(receiverUserId).orElseThrow(() -> new ComponentNotFoundException("USER_NOT_FOUND"));

        //alarm save
        AlarmEntity alarmEntity = alarmEntityRepository.save(AlarmEntity.of(user, type, args));

        emitterRepository.get(receiverUserId).ifPresentOrElse(
                sseEmitter -> {
                    try {
                        sseEmitter.send(SseEmitter.event()
                                .id((alarmEntity.getId().toString()))
                                .name(ALARM_NAME)
                                .data(alarmEntity.getText())
                        );
                    } catch (IOException e) {

                        emitterRepository.delete(receiverUserId);

                        try {
                            throw new AlarmConnectionException("ALARM_CONNECTION_FAILED");
                        } catch (AlarmConnectionException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }, () -> log.info("No emitter found")
        );
    }


    public SseEmitter subscribeAlarm(String userId, String lastEventId) throws AlarmConnectionException {
        SseEmitter sseEmitter = new SseEmitter(TIMEOUT);
        emitterRepository.save(userId, sseEmitter);

        sseEmitter.onCompletion(() -> emitterRepository.delete(userId));
        sseEmitter.onTimeout(() -> emitterRepository.delete(userId));
        try {
            sseEmitter.send(SseEmitter.event().id("").name(ALARM_NAME).data("connect complete"));
        } catch (IOException e) {
            throw new AlarmConnectionException("CONNECTION_FAILED");
        }

        /* client가 미수신한 event 목록이 존재하는 경우 */
        if (!lastEventId.isEmpty()) {
            Map<String, AlarmMessage> eventCaches = emitterRepository.getAllEventCache(userId);
            eventCaches.entrySet().stream()
                    .filter(entry -> lastEventId.compareTo(entry.getKey()) < 0)
                    .forEach(entry -> send(entry.getValue().getAlarmType(), entry.getValue().getAlarmArgs(), userId));
        }

        return sseEmitter;
    }

    public List<AlarmMessage> getAlarmList(String userId) {

        User user = userRepository.findById(userId).orElseThrow(() -> new ComponentNotFoundException("USER_NOT_FOUND"));
        List<AlarmMessage> alarmList = new ArrayList<>();
        List<AlarmEntity> alarmEntityList = alarmEntityRepository.findAllByUser(user);

        if (alarmEntityList != null) {
            alarmEntityList.forEach(alarmEntity -> alarmList.add(AlarmMessage.fromEntity(alarmEntity)));
            return alarmList;
        } else {
            return null;
        }
    }
    public void createLikeAlarm(String userId, Long postId, String postOwnerId) {
        if (validateAlarmSettings(userId, AlarmType.NEW_LIKE))
            alarmProducer.send(new AlarmMessage(postOwnerId, AlarmType.NEW_LIKE, new AlarmArgs(userId, postId), LocalDateTime.now(), userId + " adds " + "new like."));
    }

    public void createCommentAlarm(String userId, Long postId, String postOwnerId) {
        if (validateAlarmSettings(userId, AlarmType.NEW_COMMENT))
            alarmProducer.send(new AlarmMessage(postOwnerId, AlarmType.NEW_COMMENT, new AlarmArgs(userId, postId), LocalDateTime.now(), userId + " adds " + "new comment."));
    }
    public void createFollowersCreatePostAlarm(Long postId, String postOwnerId) {
        User user = userRepository.findById(postOwnerId).orElseThrow(() -> new ComponentNotFoundException("USER_NOT_FOUND"));
        List<Follow> followList = followRepository.findByFollowingUser(user);
        followList.forEach(follow -> {
                    if (validateAlarmSettings(follow.getUser().getUserId(), AlarmType.NEW_POST))
                        alarmProducer.send(new AlarmMessage(follow.getUser().getUserId(), AlarmType.NEW_POST, new AlarmArgs(postOwnerId, postId), LocalDateTime.now(), follow.getUser().getUserId() + " creates " + "new post."));
                }
        );
    }
    public void deleteAlarm(Long alarmId) {
        alarmEntityRepository.deleteById(alarmId);
    }

    public AlarmSettingsDto updateAlarmSettings(String userId, AlarmSettingsDto alarmSettingsDto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ComponentNotFoundException("USER_NOT_FOUND"));
        AlarmSettings alarmSettings = alarmSettingsRepository.findByUser(user);
        AlarmSettings newAlarmSettings = alarmSettingsRepository.save(AlarmSettings.fromDto(alarmSettings, alarmSettingsDto));
        return AlarmSettings.toDto(newAlarmSettings);
    }

    public AlarmSettingsDto getAlarmSettings(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ComponentNotFoundException("USER_NOT_FOUND"));
        AlarmSettings alarmSettings = alarmSettingsRepository.findByUser(user);
        return AlarmSettings.toDto(alarmSettings);
    }

    //알람 설정을 확인하는 함수
    public boolean validateAlarmSettings(String userId, AlarmType alarmType) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ComponentNotFoundException("USER_NOT_FOUND"));
        return switch (alarmType) {
            case NEW_COMMENT -> user.getAlarmSettings().isCommentAlarm();
            case NEW_POST -> user.getAlarmSettings().isPostAlarm();
            case NEW_LIKE -> user.getAlarmSettings().isLikeAlarm();
            case NEW_MESSAGE -> user.getAlarmSettings().isMessageAlarm();
            case NEW_ANNOUNCEMENT -> user.getAlarmSettings().isAnnouncementAlarm();
            case NEW_INQUIRY -> user.getAlarmSettings().isInquiryAlarm();
        };
    }
}
