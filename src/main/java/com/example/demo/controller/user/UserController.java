package com.example.demo.controller.user;

import com.example.demo.dto.users.user.UserInfoDto;
import com.example.demo.entity.users.user.User;
import com.example.demo.service.users.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * UserController는 사용자 정보 조회 및 삭제와 같은 사용자 관련 기능을 처리하는 컨트롤러입니다.
 */
@RequiredArgsConstructor
@RestController
public class UserController {

    private final UserService userService;

    /**
     * 사용자를 30일 후에 탈퇴 처리합니다.
     *
     * @param authentication 인증된 사용자 정보
     * @return 회원 탈퇴 성공 메시지를 반환
     */
    @DeleteMapping("/users/delete")
    public ResponseEntity<String> deleteUser(Authentication authentication) {
        String userId = ((User)authentication.getPrincipal()).getUserId();
        userService.deleteUser(userId);
        return ResponseEntity.ok("DELETE_SUCCESS");
    }

    /**
     * 사용자의 정보를 조회합니다.
     *
     * @param authentication 인증된 사용자 정보
     * @return 사용자 정보 DTO를 반환
     */
    @GetMapping("/user/info")
    public ResponseEntity<UserInfoDto> getUserInfo(Authentication authentication) {
        String userId = ((User)authentication.getPrincipal()).getUserId();
        return ResponseEntity.ok(userService.getUserInfo(userId));
    }

    /**
     * 다른 사용자의 정보를 조회합니다.
     *
     * @param requestedUserId 조회하려는 사용자의 ID
     * @param authentication  인증된 사용자 정보
     * @return 조회된 사용자 정보 DTO를 반환
     */
    @GetMapping("/user/info/{requestedUserId}")
    public ResponseEntity<UserInfoDto> getOtherUserInfo(@PathVariable String requestedUserId,
                                                        Authentication authentication) {
        String userId = ((User)authentication.getPrincipal()).getUserId();
        return ResponseEntity.ok(userService.getUserInfo(userId, requestedUserId));
    }
}
