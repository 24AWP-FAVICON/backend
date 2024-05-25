package com.example.demo.consumer;

import com.example.demo.event.AlarmMessage;
import com.example.demo.service.users.alarm.AlarmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class AlarmConsumer {

    private final AlarmService alarmService;

    @KafkaListener(topics = "alarm")
    public void consumeAlarm(AlarmMessage message, Acknowledgment ack){
        log.info("consume the event {} ", message);
        alarmService.send(message.getAlarmType(),message.getAlarmArgs(),message.getReceiveUserId());
        ack.acknowledge();
    }
}
