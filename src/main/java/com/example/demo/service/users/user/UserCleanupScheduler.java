package com.example.demo.service.users.user;

import com.example.demo.entity.users.user.User;
import com.example.demo.repository.users.user.UserRepository;
import com.example.demo.service.RedisUtil;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 사용자의 정리 작업을 수행하는 스케줄러 클래스.
 * 매일 특정 시간에 삭제 예정인 사용자를 정리합니다.
 */
@Component
public class UserCleanupScheduler {

    private final UserRepository userRepository;
    private final RedisUtil redisUtil;

    /**
     * UserCleanupScheduler 생성자
     *
     * @param userRepository 사용자 리포지토리
     * @param redisUtil      Redis 유틸리티 클래스
     */
    public UserCleanupScheduler(UserRepository userRepository, RedisUtil redisUtil) {
        this.userRepository = userRepository;
        this.redisUtil = redisUtil;
    }

    /**
     * 매일 오후 11시 50분에 삭제할 사용자를 정리합니다.
     * Redis에서 사용자 ID를 제거하고, 데이터베이스에서 사용자를 삭제합니다.
     */
    @Scheduled(cron = "0 50 23 * * ?")
    @Transactional
    public void cleanupMembers() {
        // 현재 날짜에 삭제 예정인 사용자 목록을 가져옵니다.
        List<User> deleteUserList = userRepository.findByDeleteAt(LocalDate.now());

        for (User user : deleteUserList) {
            // Redis에서 사용자 ID를 제거합니다.
            if (user.getUserId() != null)
                redisUtil.deleteData(user.getUserId());

            // 데이터베이스에서 사용자를 삭제합니다.
            userRepository.delete(user);
        }
    }
}
