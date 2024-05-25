package com.example.demo.controller.community;

import com.example.demo.dto.community.block.BlockReasonDTO;
import com.example.demo.entity.community.block.Block;
import com.example.demo.entity.community.follow.Follow;
import com.example.demo.service.community.community.CommunityService;
import com.example.demo.service.jwt.JwtCheckService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RequiredArgsConstructor
@RestController
public class CommunityController {

    private final JwtCheckService jwtCheckService;
    private final CommunityService communityService;

    @PostMapping("/community/follow/{requestedUserId}")
    public ResponseEntity<String> followUser(@PathVariable String requestedUserId,
                                             HttpServletRequest request,
                                             HttpServletResponse response) {

        String userId = jwtCheckService.checkJwt(request, response);

        if (communityService.followUser(userId, requestedUserId) != null)
            return ResponseEntity.ok().body("FOLLOW_SUCCESS");
        else
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ALREADY_FOLLOWED");
    }

    @GetMapping("/community/following")
    public ResponseEntity<List<Follow>> getFollowingUser(
            HttpServletRequest request,
            HttpServletResponse response) {
        String userId = jwtCheckService.checkJwt(request, response);

        return ResponseEntity.ok().body(communityService.getFollowingUserList(userId));
    }

    @GetMapping("/community/follower")
    public ResponseEntity<List<Follow>> getFollowerUser(
            HttpServletRequest request,
            HttpServletResponse response) {
        String userId = jwtCheckService.checkJwt(request, response);

        return ResponseEntity.ok().body(communityService.getFollowerUserList(userId));
    }

    @DeleteMapping("/community/follow")
    public ResponseEntity<String> deleteFollow(@PathVariable String requestedUserId,
                                               HttpServletRequest request,
                                               HttpServletResponse response) {
        String userId = jwtCheckService.checkJwt(request, response);

        communityService.deleteFollow(userId, requestedUserId);
        return ResponseEntity.ok().body("FOLLOW_DELETE_SUCCESS");
    }

    @PostMapping("/community/block/{requestedUserId}")
    public ResponseEntity<String> blockMember(@PathVariable String requestedUserId,
                                              @RequestBody BlockReasonDTO blockReasonDTO,
                                              HttpServletRequest request,
                                              HttpServletResponse response) {
        String userId = jwtCheckService.checkJwt(request, response);

        if (communityService.blockMember(userId, requestedUserId, blockReasonDTO) != null)
            return ResponseEntity.ok().body("BLOCK_SUCCESS");
        else
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ALREADY_BLOCKED");
    }

    @GetMapping("/community/blocks")
    public ResponseEntity<List<Block>> getBlockingMember(
            HttpServletRequest request,
            HttpServletResponse response) {
        String userId = jwtCheckService.checkJwt(request, response);

        return ResponseEntity.ok().body(communityService.getBlockingMemberList(userId));
    }

    @DeleteMapping("/delete-block/{requestedUserId}")
    public ResponseEntity<String> deleteBlock(@PathVariable String requestedUserId,
                                              HttpServletRequest request,
                                              HttpServletResponse response) {
        String userId = jwtCheckService.checkJwt(request, response);

        communityService.deleteBlock(userId, requestedUserId);
        return ResponseEntity.ok().body("FOLLOW_DELETE_SUCCESS");
    }

}
