package com.example.demo.repository.users.alarm;

import com.example.demo.event.AlarmMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class EmitterRepository {
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final Map<String, AlarmMessage> eventCache = new ConcurrentHashMap<>();

    public SseEmitter save(String userId, SseEmitter sseEmitter) {
        final String key = getKey(userId);
        emitters.put(key, sseEmitter);
        log.info("set SseEmitter {}", key);
        return sseEmitter;
    }

    public void saveEventCache(String emitterId, AlarmMessage event) {
        eventCache.put(emitterId,event);
    }


    public Optional<SseEmitter> get(String userId) {
        final String key = getKey(userId);
        return Optional.ofNullable(emitters.get(key));
    }

    public Map<String, AlarmMessage> getAllEventCache(String memberId) {
        return eventCache.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(memberId))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public void delete(String userId){
        emitters.remove(getKey(userId));
    }

    public void deleteEventCache(String userId){
        eventCache.remove(getKey(userId));
    }

    private String getKey(String userId) {
        return "Emitter:UID:" + userId;
    }
}
