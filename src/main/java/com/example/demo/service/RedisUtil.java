package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Redis 데이터베이스에 접근하고 조작하는 유틸리티 클래스.
 * Redis에 데이터 저장, 조회, 삭제 및 만료 설정 기능을 제공합니다.
 */
@Service
@RequiredArgsConstructor
public class RedisUtil {
    private final RedisTemplate<Object, Object> redisTemplate; // Redis에 접근하기 위한 Spring의 Redis 템플릿 클래스

    /**
     * 지정된 키(key)에 해당하는 데이터를 Redis에서 가져오는 메서드.
     *
     * @param key Redis에서 가져올 데이터의 키
     * @return 지정된 키에 대한 데이터, 데이터가 없으면 null
     */
    public String getData(String key) {
        ValueOperations<Object, Object> valueOperations = redisTemplate.opsForValue();
        return (String) valueOperations.get(key);
    }

    /**
     * 지정된 키(key)에 값을 저장하는 메서드.
     *
     * @param key     저장할 데이터의 키
     * @param value   저장할 데이터
     * @param timeout 만료 시간
     * @param timeUnit 시간 단위 (예: 초, 분 등)
     */
    public void setData(String key, Object value, long timeout, TimeUnit timeUnit) {
        ValueOperations<Object, Object> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(key, value, timeout, timeUnit);
    }

    /**
     * 지정된 키(key)에 값을 저장하고, 지정된 시간(duration) 후에 데이터가 만료되도록 설정하는 메서드.
     *
     * @param key      저장할 데이터의 키
     * @param value    저장할 데이터
     * @param duration 만료될 시간(초 단위)
     */
    public void setDataExpire(String key, Object value, long duration) {
        ValueOperations<Object, Object> valueOperations = redisTemplate.opsForValue();
        Duration expireDuration = Duration.ofSeconds(duration);
        valueOperations.set(key, value, expireDuration);
    }

    /**
     * 지정된 키(key)에 해당하는 데이터를 Redis에서 삭제하는 메서드.
     *
     * @param key 삭제할 데이터의 키
     */
    public void deleteData(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 특정 키가 Redis에 저장되어 있는지 확인하는 메서드.
     *
     * @param key 확인할 키
     * @return 키가 존재하면 true, 그렇지 않으면 false
     */
    public boolean checkIfKeyExists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
