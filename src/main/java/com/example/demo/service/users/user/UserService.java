package com.example.demo.service.users.user;

import com.example.demo.dto.users.user.UserInfoDto;
import com.example.demo.entity.community.post.Post;
import com.example.demo.entity.users.user.User;
import com.example.demo.exception.ComponentNotFoundException;
import com.example.demo.repository.users.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    public void deleteUser(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ComponentNotFoundException("USER_NOT_FOUND"));
        user.setDeleteAt(LocalDate.now().plusDays(30L));
        userRepository.save(user);
    }

    public UserInfoDto getUserInfo(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ComponentNotFoundException("USER_NOT_FOUND"));
        return UserInfoDto.toDto(user);
    }

    public UserInfoDto getUserInfo(String userId,String requestedUserId) {
        userRepository.findById(userId).orElseThrow(() -> new ComponentNotFoundException("USER_NOT_FOUND"));
        User requestedUser = userRepository.findById(requestedUserId).orElseThrow(() -> new ComponentNotFoundException("USER_NOT_FOUND"));

        //다른 사용자가 요청했을때 비공개글은 필터링
        UserInfoDto userInfoDto = UserInfoDto.toDto(requestedUser);
        List<Post> openPostList = userInfoDto.getPostList().stream().filter(Post::isOpen).toList();
        userInfoDto.setPostList(openPostList);
        return userInfoDto;
    }
}
