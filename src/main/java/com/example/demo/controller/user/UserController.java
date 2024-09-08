package com.example.demo.controller.user;

import com.example.demo.dto.users.user.UserInfoDto;
import com.example.demo.entity.users.user.User;
import com.example.demo.service.users.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
public class UserController {

    private final UserService userService;

    //30일 후 회원 탈퇴
    @DeleteMapping("/users/delete")
    public ResponseEntity<String> deleteUser(HttpServletRequest request,
                                             HttpServletResponse response,
                                             Authentication authentication) {
        String userId = ((User)authentication.getPrincipal()).getUserId();
        userService.deleteUser(userId);
        return ResponseEntity.ok("DELETE_SUCCESS");
    }

    @GetMapping("/user/info")
    public ResponseEntity<UserInfoDto> getUserInfo(HttpServletRequest request,
                                                   HttpServletResponse response,
                                                   Authentication authentication) {
        String userId = ((User)authentication.getPrincipal()).getUserId();
        return ResponseEntity.ok(userService.getUserInfo(userId));
    }

    @GetMapping("/user/info/{requestedUserId}")
    public ResponseEntity<UserInfoDto> getOtherUserInfo(@PathVariable String requestedUserId,
                                                        HttpServletRequest request,
                                                        HttpServletResponse response,
                                                        Authentication authentication) {
        String userId = ((User)authentication.getPrincipal()).getUserId();
        return ResponseEntity.ok(userService.getUserInfo(userId, requestedUserId));
    }
}
