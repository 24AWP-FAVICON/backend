package com.example.demo.service.users.user;

import com.example.demo.entity.users.user.User;
import com.example.demo.repository.users.user.UserRepository;
import com.example.demo.service.RedisUtil;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
public class UserCleanupScheduler {

    private final UserRepository userRepository;
    private final RedisUtil redisUtil;

    public UserCleanupScheduler(UserRepository userRepository, RedisUtil redisUtil) {
        this.userRepository = userRepository;
        this.redisUtil = redisUtil;
    }

    @Scheduled(cron = "0 50 23 * * ?")
    @Transactional
    public void cleanupMembers() {
        List<User> deleteUserList = userRepository.findByDeleteAt(LocalDate.now());
        for (User user : deleteUserList) {
            if (user.getUserId() != null)
                redisUtil.deleteData(user.getUserId());   //redis의 userId 제거

            userRepository.delete(user);
        }
    }
}
