package com.example.demo.service.users.user;

import com.example.demo.dto.users.user.UserInfoDto;
import com.example.demo.entity.community.post.Post;
import com.example.demo.entity.users.user.User;
import com.example.demo.exception.ComponentNotFoundException;
import com.example.demo.repository.users.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * 사용자 관련 서비스 클래스.
 * 사용자 정보를 조회하고 삭제하는 기능을 제공합니다.
 */
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    /**
     * 사용자 삭제 메소드.
     * 사용자를 삭제하지 않고, 삭제 예정일을 현재 날짜로부터 30일 후로 설정합니다.
     *
     * @param userId 삭제할 사용자의 ID
     */
    public void deleteUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ComponentNotFoundException("USER_NOT_FOUND"));
        user.setDeleteAt(LocalDate.now().plusDays(30L));
        userRepository.save(user);
    }

    /**
     * 특정 사용자 정보를 조회하는 메소드.
     *
     * @param userId 조회할 사용자의 ID
     * @return 사용자의 정보를 담고 있는 UserInfoDto 객체
     */
    public UserInfoDto getUserInfo(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ComponentNotFoundException("USER_NOT_FOUND"));
        return UserInfoDto.toDto(user);
    }

    /**
     * 요청한 사용자 정보를 조회하는 메소드.
     *
     * @param userId        요청자의 ID
     * @param requestedUserId 조회할 사용자의 ID
     * @return 요청한 사용자의 정보를 담고 있는 UserInfoDto 객체
     */
    public UserInfoDto getUserInfo(String userId, String requestedUserId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ComponentNotFoundException("USER_NOT_FOUND"));
        User requestedUser = userRepository.findById(requestedUserId)
                .orElseThrow(() -> new ComponentNotFoundException("USER_NOT_FOUND"));

        return UserInfoDto.toDto(requestedUser);
    }
}
