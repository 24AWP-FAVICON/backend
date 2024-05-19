package com.example.demo.controller;

import com.example.demo.dto.comment.CommentRequestDto;
import com.example.demo.dto.comment.CommentResponseDto;
import com.example.demo.entity.Comment;
import com.example.demo.service.CommentService;
import com.example.demo.service.JwtCheckService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@RequiredArgsConstructor
@RequestMapping("/comment")
@RestController
public class CommentController {

    private final CommentService commentService;
    private final JwtCheckService jwtCheckService;

    @GetMapping("/all-comments")
    public List<Comment> getAllPosts(HttpServletRequest request,
                                     HttpServletResponse response) {
        jwtCheckService.checkJwt(request, response);
        return commentService.getAllPosts();
    }

    //게시글에 있는 모든 댓글 조회
    @GetMapping("/post/{postId}/comments")
    public List<CommentResponseDto> getCommentsByPostId(@PathVariable Long postId,
                                                        HttpServletRequest request,
                                                        HttpServletResponse response) {
        jwtCheckService.checkJwt(request, response);

        return commentService.getCommentsByPostId(postId);
    }

    //회원이 작성한 모든 댓글 조회
    @GetMapping("/user/{userId}/comments")
    public List<CommentResponseDto> getCommentsByUserId(@PathVariable String userId,
                                                        HttpServletRequest request,
                                                        HttpServletResponse response) {
        jwtCheckService.checkJwt(request, response);

        List<CommentResponseDto> commentResponseDtoList = new ArrayList<>();
        commentService.getCommentsByUserId(userId).forEach(comment -> {
            commentResponseDtoList.add(CommentResponseDto.toDto(comment));
        });
        return commentResponseDtoList;
    }

    @PostMapping("/create/{postId}")
    public CommentResponseDto createComment(@RequestBody CommentRequestDto commentRequestDto,
                                 @PathVariable Long postId,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {
        String userId = jwtCheckService.checkJwt(request, response);

        return CommentResponseDto.toDto(commentService.savePostComment( commentRequestDto.toDto(userId, postId)));
    }

    @PostMapping("/update/{commentId}")
    public CommentResponseDto updateComment(@RequestBody CommentRequestDto commentRequestDto,
                                 @PathVariable Long commentId,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {

        String userId = jwtCheckService.checkJwt(request, response);
        return CommentResponseDto.toDto(commentService.updateComment(commentRequestDto, commentId, userId));
    }

    @DeleteMapping("/{commentId}/delete")
    public void deleteArticleComment(@PathVariable Long commentId,
                                     HttpServletRequest request,
                                     HttpServletResponse response) {

        String userId = jwtCheckService.checkJwt(request, response);

        commentService.deleteArticleComment(commentId, userId);
    }

}
