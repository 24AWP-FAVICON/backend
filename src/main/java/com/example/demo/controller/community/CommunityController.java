package com.example.demo.controller.community;

import com.example.demo.dto.community.block.BlockReasonDTO;
import com.example.demo.entity.community.block.Block;
import com.example.demo.entity.community.follow.Follow;
import com.example.demo.entity.users.user.User;
import com.example.demo.service.community.community.CommunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CommunityController는 커뮤니티 내 팔로우 및 차단 관련 기능을 제공하는 컨트롤러입니다.
 * 팔로우, 차단 및 관련 정보 조회, 삭제 등의 기능을 포함합니다.
 */
@RequiredArgsConstructor
@RestController
public class CommunityController {

    private final CommunityService communityService;

    /**
     * 요청한 사용자를 팔로우합니다.
     *
     * @param requestedUserId 팔로우할 사용자의 ID
     * @param authentication  인증된 사용자 정보
     * @return 팔로우 성공 여부에 따른 메시지를 반환
     */
    @PostMapping("/community/follow/{requestedUserId}")
    public ResponseEntity<String> followUser(@PathVariable String requestedUserId,
                                             Authentication authentication) {

        String userId = ((User) authentication.getPrincipal()).getUserId();

        if (communityService.followUser(userId, requestedUserId) != null)
            return ResponseEntity.ok().body("FOLLOW_SUCCESS");
        else
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ALREADY_FOLLOWED");
    }

    /**
     * 현재 사용자가 팔로우하는 모든 사용자를 조회합니다.
     *
     * @param authentication 인증된 사용자 정보
     * @return 팔로우 중인 사용자 리스트를 반환
     */
    @GetMapping("/community/following")
    public ResponseEntity<List<Follow>> getFollowingUser(Authentication authentication) {
        String userId = ((User) authentication.getPrincipal()).getUserId();

        return ResponseEntity.ok().body(communityService.getFollowingUserList(userId));
    }

    /**
     * 현재 사용자를 팔로우하는 모든 사용자를 조회합니다.
     *
     * @param authentication 인증된 사용자 정보
     * @return 팔로워 리스트를 반환
     */
    @GetMapping("/community/follower")
    public ResponseEntity<List<Follow>> getFollowerUser(Authentication authentication) {
        String userId = ((User) authentication.getPrincipal()).getUserId();

        return ResponseEntity.ok().body(communityService.getFollowerUserList(userId));
    }

    /**
     * 요청한 사용자를 팔로우 취소합니다.
     *
     * @param requestedUserId 팔로우 취소할 사용자 ID
     * @param authentication  인증된 사용자 정보
     * @return 팔로우 삭제 성공 메시지를 반환
     */
    @DeleteMapping("/community/follow")
    public ResponseEntity<String> deleteFollow(@PathVariable String requestedUserId,
                                               Authentication authentication) {
        String userId = ((User) authentication.getPrincipal()).getUserId();

        communityService.deleteFollow(userId, requestedUserId);
        return ResponseEntity.ok().body("FOLLOW_DELETE_SUCCESS");
    }

    /**
     * 요청한 사용자를 차단합니다.
     *
     * @param requestedUserId 차단할 사용자 ID
     * @param blockReasonDTO  차단 사유를 담은 객체
     * @param authentication  인증된 사용자 정보
     * @return 차단 성공 여부에 따른 메시지를 반환
     */
    @PostMapping("/community/block/{requestedUserId}")
    public ResponseEntity<String> blockMember(@PathVariable String requestedUserId,
                                              @RequestBody BlockReasonDTO blockReasonDTO,
                                              Authentication authentication) {
        String userId = ((User) authentication.getPrincipal()).getUserId();

        if (communityService.blockMember(userId, requestedUserId, blockReasonDTO) != null)
            return ResponseEntity.ok().body("BLOCK_SUCCESS");
        else
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ALREADY_BLOCKED");
    }

    /**
     * 현재 사용자가 차단한 사용자 리스트를 조회합니다.
     *
     * @param authentication 인증된 사용자 정보
     * @return 차단한 사용자 리스트를 반환
     */
    @GetMapping("/community/blocks")
    public ResponseEntity<List<Block>> getBlockingMember(Authentication authentication) {
        String userId = ((User) authentication.getPrincipal()).getUserId();

        return ResponseEntity.ok().body(communityService.getBlockingMemberList(userId));
    }

    /**
     * 차단 해제 요청을 처리합니다.
     *
     * @param requestedUserId 차단 해제할 사용자 ID
     * @param authentication  인증된 사용자 정보
     * @return 차단 해제 성공 메시지를 반환
     */
    @DeleteMapping("/delete-block/{requestedUserId}")
    public ResponseEntity<String> deleteBlock(@PathVariable String requestedUserId,
                                              Authentication authentication) {
        String userId = ((User) authentication.getPrincipal()).getUserId();

        communityService.deleteBlock(userId, requestedUserId);
        return ResponseEntity.ok().body("BLOCK_DELETE_SUCCESS");
    }
}
