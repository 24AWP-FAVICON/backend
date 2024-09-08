package com.example.demo.controller.community;

import com.example.demo.dto.community.comment.CommentRequestDto;
import com.example.demo.dto.community.comment.CommentResponseDto;
import com.example.demo.entity.users.user.User;
import com.example.demo.service.community.comment.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RequiredArgsConstructor
@RequestMapping("/community")
@RestController
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/comments")
    public ResponseEntity<List<CommentResponseDto>> getAllComments(HttpServletRequest request,
                                                                   HttpServletResponse response) {
        return ResponseEntity.ok(commentService.getAllComments());
    }

    //게시글에 있는 모든 댓글 조회
    @GetMapping("/post/{postId}/comments")
    public ResponseEntity<List<CommentResponseDto>> getCommentsByPostId(@PathVariable Long postId,
                                                                        HttpServletRequest request,
                                                                        HttpServletResponse response) {
        return ResponseEntity.ok(commentService.getCommentsByPostId(postId));
    }

    //회원이 작성한 모든 댓글 조회
    @GetMapping("/comments/user/{userId}")
    public ResponseEntity<List<CommentResponseDto>> getCommentsByUserId(@PathVariable String userId,
                                                                        HttpServletRequest request,
                                                                        HttpServletResponse response) {
        return ResponseEntity.ok(commentService.getCommentsByUserId(userId));
    }

    @PostMapping("/post/{postId}/comment")
    public ResponseEntity<CommentResponseDto> createComment(@RequestBody CommentRequestDto commentRequestDto,
                                                            @PathVariable Long postId,
                                                            HttpServletRequest request,
                                                            HttpServletResponse response,
                                                            Authentication authentication) {
        String userId = ((User) authentication.getPrincipal()).getUserId();

        return ResponseEntity.ok(commentService.createComment(commentRequestDto, userId, postId));
    }

    @PutMapping("/post/{postId}/comment/{commentId}")
    public ResponseEntity<CommentResponseDto> updateComment(@RequestBody CommentRequestDto commentRequestDto,
                                                            @PathVariable Long commentId,
                                                            HttpServletRequest request,
                                                            HttpServletResponse response,
                                                            Authentication authentication) {

        String userId = ((User) authentication.getPrincipal()).getUserId();
        return ResponseEntity.ok(commentService.updateComment(commentRequestDto, commentId, userId));
    }

    @DeleteMapping("/post/{postId}/comment/{commentId}")
    public ResponseEntity<String> deleteArticleComment(@PathVariable Long commentId,
                                                       HttpServletRequest request,
                                                       HttpServletResponse response,
                                                       Authentication authentication) {

        String userId = ((User) authentication.getPrincipal()).getUserId();

        commentService.deleteArticleComment(commentId, userId);
        return ResponseEntity.ok().body("DELETE_SUCCESS");
    }

}
