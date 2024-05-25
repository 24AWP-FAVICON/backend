package com.example.demo.service.community.community;

import com.example.demo.dto.community.block.BlockReasonDTO;
import com.example.demo.entity.community.block.Block;
import com.example.demo.entity.community.follow.Follow;
import com.example.demo.entity.users.user.User;
import com.example.demo.exception.ComponentNotFoundException;
import com.example.demo.repository.community.block.BlockRepository;
import com.example.demo.repository.community.follow.FollowRepository;
import com.example.demo.repository.users.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class CommunityService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final BlockRepository blockRepository;

    public Follow followUser(String userId, String requestedUserId) {

        User user = userRepository.findById(userId).orElseThrow(() -> new ComponentNotFoundException("USER_NOT_FOUND"));
        User followingUser = userRepository.findById(requestedUserId).orElseThrow(() -> new ComponentNotFoundException("USER_NOT_FOUND"));

        if (followRepository.findByUserAndFollowingUser(user.getUserId(), requestedUserId).isEmpty())
            return followRepository.save(Follow.of(user, followingUser));
        else
            return null;
    }

    public List<Follow> getFollowingUserList(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ComponentNotFoundException("USER_NOT_FOUND"));
        return followRepository.findByUser(user);
    }

    public List<Follow> getFollowerUserList(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ComponentNotFoundException("USER_NOT_FOUND"));
        return followRepository.findByFollowingUser(user);
    }

    public void deleteFollow(String userId, String requestedUserId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ComponentNotFoundException("USER_NOT_FOUND"));
        userRepository.findById(requestedUserId).orElseThrow(() -> new ComponentNotFoundException("USER_NOT_FOUND"));
        followRepository.deleteByUser_UserIdAndFollowingUser_UserId(user.getUserId(), requestedUserId);
    }

    public Block blockMember(String userId, String requestedUserId, BlockReasonDTO blockReasonDTO) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ComponentNotFoundException("USER_NOT_FOUND"));
        User blockingUser = userRepository.findById(requestedUserId).orElseThrow(() -> new ComponentNotFoundException("USER_NOT_FOUND"));

        if (blockRepository.findByUserIdAndBlockingUserId(user.getUserId(), requestedUserId).isEmpty())
            return blockRepository.save(Block.of(user, blockingUser, blockReasonDTO.getBlockReason()));
        else
            return null;
    }

    public List<Block> getBlockingMemberList(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ComponentNotFoundException("USER_NOT_FOUND"));
        return blockRepository.findByUser(user);
    }

    public void deleteBlock(String userId, String requestedUserId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ComponentNotFoundException("USER_NOT_FOUND"));
        userRepository.findById(requestedUserId).orElseThrow(() -> new ComponentNotFoundException("USER_NOT_FOUND"));
        blockRepository.deleteByUser_UserIdAndBlockingUser_UserId(user.getUserId(), requestedUserId);
    }


}


