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

/**
 * CommunityService는 유저 간의 팔로우 및 차단 관련 로직을 처리하는 서비스 클래스입니다.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class CommunityService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final BlockRepository blockRepository;

    /**
     * 주어진 유저가 다른 유저를 팔로우합니다.
     *
     * @param userId 팔로우를 요청한 유저의 ID
     * @param requestedUserId 팔로우하려는 유저의 ID
     * @return Follow 객체를 반환하거나 이미 팔로우 중이면 null을 반환
     */
    public Follow followUser(String userId, String requestedUserId) {

        User user = userRepository.findById(userId).orElseThrow(() -> new ComponentNotFoundException("USER_NOT_FOUND"));
        User followingUser = userRepository.findById(requestedUserId).orElseThrow(() -> new ComponentNotFoundException("USER_NOT_FOUND"));

        // 이미 팔로우 중인 경우 팔로우를 생성하지 않음
        if (followRepository.findByUserAndFollowingUser(user.getUserId(), requestedUserId).isEmpty())
            return followRepository.save(Follow.of(user, followingUser));
        else
            return null;
    }

    /**
     * 특정 유저가 팔로우하고 있는 유저 목록을 반환합니다.
     *
     * @param userId 조회하려는 유저의 ID
     * @return 팔로우하고 있는 유저들의 목록
     */
    public List<Follow> getFollowingUserList(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ComponentNotFoundException("USER_NOT_FOUND"));
        return followRepository.findByUser(user);
    }

    /**
     * 특정 유저를 팔로우하는 유저 목록을 반환합니다.
     *
     * @param userId 조회하려는 유저의 ID
     * @return 해당 유저를 팔로우하는 유저들의 목록
     */
    public List<Follow> getFollowerUserList(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ComponentNotFoundException("USER_NOT_FOUND"));
        return followRepository.findByFollowingUser(user);
    }

    /**
     * 팔로우 관계를 삭제합니다.
     *
     * @param userId 팔로우를 취소하려는 유저의 ID
     * @param requestedUserId 팔로우를 취소할 대상 유저의 ID
     */
    public void deleteFollow(String userId, String requestedUserId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ComponentNotFoundException("USER_NOT_FOUND"));
        userRepository.findById(requestedUserId).orElseThrow(() -> new ComponentNotFoundException("USER_NOT_FOUND"));
        followRepository.deleteByUser_UserIdAndFollowingUser_UserId(user.getUserId(), requestedUserId);
    }

    /**
     * 특정 유저를 차단합니다.
     *
     * @param userId 차단 요청을 한 유저의 ID
     * @param requestedUserId 차단하려는 유저의 ID
     * @param blockReasonDTO 차단 사유를 담고 있는 DTO
     * @return 차단 정보가 담긴 Block 객체를 반환하거나 이미 차단 중이면 null 반환
     */
    public Block blockMember(String userId, String requestedUserId, BlockReasonDTO blockReasonDTO) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ComponentNotFoundException("USER_NOT_FOUND"));
        User blockingUser = userRepository.findById(requestedUserId).orElseThrow(() -> new ComponentNotFoundException("USER_NOT_FOUND"));

        // 이미 차단 중인 경우 차단을 생성하지 않음
        if (blockRepository.findByUserIdAndBlockingUserId(user.getUserId(), requestedUserId).isEmpty())
            return blockRepository.save(Block.of(user, blockingUser, blockReasonDTO.getBlockReason()));
        else
            return null;
    }

    /**
     * 특정 유저가 차단한 유저 목록을 반환합니다.
     *
     * @param userId 차단 목록을 조회하려는 유저의 ID
     * @return 차단한 유저들의 목록
     */
    public List<Block> getBlockingMemberList(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ComponentNotFoundException("USER_NOT_FOUND"));
        return blockRepository.findByUser(user);
    }

    /**
     * 특정 유저와의 차단 관계를 해제합니다.
     *
     * @param userId 차단 해제를 요청한 유저의 ID
     * @param requestedUserId 차단을 해제할 대상 유저의 ID
     */
    public void deleteBlock(String userId, String requestedUserId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ComponentNotFoundException("USER_NOT_FOUND"));
        userRepository.findById(requestedUserId).orElseThrow(() -> new ComponentNotFoundException("USER_NOT_FOUND"));
        blockRepository.deleteByUser_UserIdAndBlockingUser_UserId(user.getUserId(), requestedUserId);
    }

}
