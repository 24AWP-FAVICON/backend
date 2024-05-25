package com.example.demo.producer;

import com.example.demo.event.AlarmMessage;
import com.example.demo.repository.users.alarm.EmitterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class AlarmProducer {

    private final KafkaTemplate<String, AlarmMessage> kafkaTemplate;
    private final EmitterRepository emitterRepository;

    private String topic = "alarm";

    public void send(AlarmMessage message) {
        emitterRepository.saveEventCache(message.getReceiveUserId()+"_"+System.currentTimeMillis(),message);
        kafkaTemplate.send(topic, message.getReceiveUserId(), message);
        log.info("send to kafka finished");
    }
}
