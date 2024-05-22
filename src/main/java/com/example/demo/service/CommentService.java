package com.example.demo.service;


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
    public List<CommentResponseDto> getAllComments() {
        return fromCommentListToCommentResponseDtoList(commentRepository.findAll());
    }

    @Transactional(readOnly = true)
    public List<CommentResponseDto> getCommentsByPostId(Long postId) {

        //생성시간 순으로 정렬
        List<Comment> commentList = commentRepository.findByPostId(postId)
                .stream()
                .sorted(Comparator.comparing(Comment::getCreatedAt))
                .toList();

        List<CommentResponseDto> returnList = new ArrayList<>();
        Map<Long, CommentResponseDto> commentMap = new HashMap<>();

        for (Comment comment : commentList) {
            CommentResponseDto commentResponseDto = CommentResponseDto.toDto(comment);
            commentMap.put(comment.getCommentId(), commentResponseDto);
            if (comment.getParentCommentId() == null) {
                returnList.add(commentResponseDto);
            } else {
                CommentResponseDto parentDto = commentMap.get(comment.getParentCommentId());
                //TODO 대댓글이 달린 댓글을 나중에 삭제할 경우 고려해야 함
                if (parentDto != null) {
                    parentDto.getChildComments().add(commentResponseDto);
                }
            }
        }
        return returnList;
    }


    @Transactional(readOnly = true)
    public List<CommentResponseDto> getCommentsByUserId(String userId) {
        return fromCommentListToCommentResponseDtoList(commentRepository.findByUserId(userId));
    }

    public CommentResponseDto savePostComment(CommentRequestDto commentRequestDto, String userId, Long postId) {

        try {
            //댓글을 등록할 게시글이 존재하는지 확인
            Post post = postRepository.findById(postId).orElseThrow(() -> new ComponentNotFoundException("POST_NOT_FOUND"));
            User user = userRepository.findById(userId).orElseThrow(() -> new ComponentNotFoundException("USER_NOT_FOUND"));

            Comment comment = CommentRequestDto.toEntity(post,
                    user,
                    commentRequestDto.getContent(),
                    commentRequestDto.getParentCommentId());
//
//            // alarm 생성
//            jwtCheckService.addAlarm(accessToken, dto.userId(), dto.postId(), post.getUserId(),"comment");

            commentRepository.save(comment);

            return CommentResponseDto.toDto(comment);
        } catch (EntityNotFoundException e) {
            log.warn("COMMENT_SAVE_FAILED - {}", e.getLocalizedMessage());
            throw new EntityNotFoundException("COMMENT_SAVE_FAIL_COULD_NOT_FOUND_COMMENT_INFO");
        }
    }

    public CommentResponseDto updateComment(CommentRequestDto commentRequestDto, Long commentId, String userId) {
        Comment comment = commentRepository.findByCommentId(commentId).orElseThrow(() -> new ComponentNotFoundException("COMMENT_NOT_FOUND"));

        //댓글 수정 요청한 사람이 작성자가 아니면 권한이 없다는 에러 발생시킨다
        if (!comment.getUser().getUserId().equalsIgnoreCase(userId))
            throw new UnAuthorizedUserException("UNAUTHORIZED_USER");

        comment.setContent(commentRequestDto.getContent());

        commentRepository.deleteByCommentId(commentId);
        commentRepository.save(comment);
        return CommentResponseDto.toDto(comment);
    }

    public void deleteArticleComment(Long commentId, String userId) {
        Comment comment = commentRepository.findByCommentId(commentId).orElseThrow(() -> new ComponentNotFoundException("COMMENT_NOT_FOUND"));

        //댓글 삭제 요청한 사람이 작성자가 아니면 권한이 없다는 에러 발생시킨다
        if (!comment.getUser().getUserId().equalsIgnoreCase(userId))
            throw new UnAuthorizedUserException("UNAUTHORIZED_USER");

        commentRepository.deleteByCommentId(commentId);
    }

    public List<CommentResponseDto> fromCommentListToCommentResponseDtoList(List<Comment> commentList) {
        List<CommentResponseDto> commentResponseDtoList = new ArrayList<>();
        commentList.forEach(comment -> commentResponseDtoList.add(CommentResponseDto.toDto(comment)));
        return commentResponseDtoList;
    }
}
