package com.example.demo.controller.community;

import com.example.demo.dto.community.comment.CommentRequestDto;
import com.example.demo.dto.community.comment.CommentResponseDto;
import com.example.demo.entity.users.user.User;
import com.example.demo.service.community.comment.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CommentController는 커뮤니티 댓글과 관련된 요청을 처리하는 컨트롤러입니다.
 * 댓글 조회, 생성, 수정, 삭제 기능을 제공합니다.
 */
@RequiredArgsConstructor
@RequestMapping("/community")
@RestController
public class CommentController {

    private final CommentService commentService;

    /**
     * 모든 댓글을 조회합니다.
     *
     * @return 모든 댓글의 리스트가 포함된 ResponseEntity
     */
    @GetMapping("/comments")
    public ResponseEntity<List<CommentResponseDto>> getAllComments() {
        return ResponseEntity.ok(commentService.getAllComments());
    }

    /**
     * 특정 게시글에 있는 모든 댓글을 조회합니다.
     *
     * @param postId   게시글 ID
     * @return 해당 게시글에 대한 댓글 리스트가 포함된 ResponseEntity
     */
    @GetMapping("/post/{postId}/comments")
    public ResponseEntity<List<CommentResponseDto>> getCommentsByPostId(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getCommentsByPostId(postId));
    }

    /**
     * 특정 사용자가 작성한 모든 댓글을 조회합니다.
     *
     * @param userId   사용자 ID
     * @return 해당 사용자가 작성한 댓글 리스트가 포함된 ResponseEntity
     */
    @GetMapping("/comments/user/{userId}")
    public ResponseEntity<List<CommentResponseDto>> getCommentsByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(commentService.getCommentsByUserId(userId));
    }

    /**
     * 특정 게시글에 새로운 댓글을 작성합니다.
     *
     * @param commentRequestDto 작성할 댓글 내용이 포함된 요청 본문
     * @param postId            게시글 ID
     * @param authentication    현재 사용자 정보를 포함한 인증 객체
     * @return 생성된 댓글 정보를 담은 ResponseEntity
     */
    @PostMapping("/post/{postId}/comment")
    public ResponseEntity<CommentResponseDto> createComment(@RequestBody CommentRequestDto commentRequestDto,
                                                            @PathVariable Long postId,
                                                            Authentication authentication) {
        String userId = ((User) authentication.getPrincipal()).getUserId();
        return ResponseEntity.ok(commentService.createComment(commentRequestDto, userId, postId));
    }

    /**
     * 기존 댓글을 수정합니다.
     *
     * @param commentRequestDto 수정할 댓글 내용이 포함된 요청 본문
     * @param commentId         수정할 댓글 ID
     * @param authentication    현재 사용자 정보를 포함한 인증 객체
     * @return 수정된 댓글 정보를 담은 ResponseEntity
     */
    @PutMapping("/post/{postId}/comment/{commentId}")
    public ResponseEntity<CommentResponseDto> updateComment(@RequestBody CommentRequestDto commentRequestDto,
                                                            @PathVariable Long commentId,
                                                            Authentication authentication) {

        String userId = ((User) authentication.getPrincipal()).getUserId();
        return ResponseEntity.ok(commentService.updateComment(commentRequestDto, commentId, userId));
    }

    /**
     * 특정 게시글의 댓글을 삭제합니다.
     *
     * @param commentId     삭제할 댓글 ID
     * @param authentication 현재 사용자 정보를 포함한 인증 객체
     * @return 삭제 완료 메시지를 담은 ResponseEntity
     */
    @DeleteMapping("/post/{postId}/comment/{commentId}")
    public ResponseEntity<String> deleteArticleComment(@PathVariable Long commentId,
                                                       Authentication authentication) {

        String userId = ((User) authentication.getPrincipal()).getUserId();
        commentService.deleteArticleComment(commentId, userId);
        return ResponseEntity.ok().body("DELETE_SUCCESS");
    }
}
