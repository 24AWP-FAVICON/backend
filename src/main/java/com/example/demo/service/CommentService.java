package com.example.demo.service;


import com.example.demo.dto.comment.CommentDto;
import com.example.demo.dto.comment.CommentRequestDto;
import com.example.demo.dto.comment.CommentResponseDto;
import com.example.demo.entity.Comment;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.exception.ComponentNotFoundException;
import com.example.demo.exception.UnAuthorizedUserException;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<CommentDto> searchArticleComments(Long postId) {
        return commentRepository.findByPostId(postId)
                .stream()
                .map(CommentDto::from)
                .toList();
    }

    @Transactional
    public Comment savePostComment( CommentDto dto) {

        try {
            //댓글을 등록할 게시글이 존재하는지 확인
            Post post = postRepository.findById(dto.postId()).orElseThrow(() -> new ComponentNotFoundException("POST_NOT_FOUND"));
            User user = userRepository.findById(dto.userId()).orElseThrow(() -> new ComponentNotFoundException("USER_NOT_FOUND"));

            Comment comment = dto.toEntity(post, user, dto.parentCommentId());
//
//            // alarm 생성
//            jwtCheckService.addAlarm(accessToken, dto.userId(), dto.postId(), post.getUserId(),"comment");

            commentRepository.save(comment);

            return comment;
        } catch (EntityNotFoundException e) {
            log.warn("댓글 저장 실패. 댓글 작성에 필요한 정보를 찾을 수 없습니다 - {}", e.getLocalizedMessage());
            throw new EntityNotFoundException("COMMENT_SAVE_FAIL. COULD_NOT_FOUND_COMMENT_INFO");
        }
    }

    public void deleteArticleComment(Long commentId, String userId) {
        Comment comment = commentRepository.findByCommentId(commentId).orElseThrow(() -> new ComponentNotFoundException("COMMENT_NOT_FOUND"));

        if (!comment.getUser().getUserId().equalsIgnoreCase(userId))
            throw new UnAuthorizedUserException("UNAUTHORIZED_USER");

        commentRepository.deleteByCommentId(commentId);
    }

    @Transactional(readOnly = true)
    public List<Comment> getAllPosts() {
        return commentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<CommentResponseDto> getCommentsByPostId(Long postId) {

        List<Comment> commentList = commentRepository.findByPostId(postId).stream()
                .sorted(Comparator.comparing(Comment::getCreatedAt)).toList();

        List<CommentResponseDto> returnList = new ArrayList<>();
        Map<Long, CommentResponseDto> commentMap = new HashMap<>();

        for (Comment comment : commentList) {
            CommentResponseDto commentResponseDto = CommentResponseDto.toDto(comment);
            commentMap.put(comment.getCommentId(), commentResponseDto);
            if (comment.getParentCommentId() == null) {
                returnList.add(commentResponseDto);
            } else {
                CommentResponseDto parentDto = commentMap.get(comment.getParentCommentId());
                if (parentDto != null) {
                    parentDto.getChildComments().add(commentResponseDto);
                }
            }
        }

        return returnList;
    }


    @Transactional(readOnly = true)
    public List<Comment> getCommentsByUserId(String userId) {
        return commentRepository.findByUserId(userId);
    }

    public Comment updateComment(CommentRequestDto commentRequestDto, Long commentId, String userId) {
        Comment comment = commentRepository.findByCommentId(commentId).orElseThrow(() -> new ComponentNotFoundException("COMMENT_NOT_FOUND"));

        if (!comment.getUser().getUserId().equalsIgnoreCase(userId))
            throw new UnAuthorizedUserException("UNAUTHORIZED_USER");

        comment.setContent(commentRequestDto.content());

        commentRepository.deleteByCommentId(commentId);
        commentRepository.save(comment);
        return comment;
    }
}
