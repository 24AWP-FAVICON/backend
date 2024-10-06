package com.example.demo.service.community.comment;

import com.example.demo.converter.DtoConverter;
import com.example.demo.dto.community.comment.CommentRequestDto;
import com.example.demo.dto.community.comment.CommentResponseDto;
import com.example.demo.entity.community.comment.Comment;
import com.example.demo.entity.community.post.Post;
import com.example.demo.entity.users.user.User;
import com.example.demo.exception.ComponentNotFoundException;
import com.example.demo.exception.UnAuthorizedUserException;
import com.example.demo.repository.community.comment.CommentRepository;
import com.example.demo.repository.community.post.PostRepository;
import com.example.demo.repository.users.user.UserRepository;
import com.example.demo.service.users.alarm.AlarmService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * CommentService는 게시글에 달린 댓글과 관련된 로직을 처리하는 서비스 클래스입니다.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final AlarmService alarmService;

    /**
     * 모든 댓글을 조회하여 반환합니다.
     *
     * @return 모든 댓글의 리스트를 CommentResponseDto로 변환하여 반환
     */
    @Transactional(readOnly = true)
    public List<CommentResponseDto> getAllComments() {
        return DtoConverter.convertEntityListToDtoList(commentRepository.findAll(),CommentResponseDto::toDto);
    }

    /**
     * 특정 게시글에 달린 댓글들을 조회하여 반환합니다.
     * 댓글은 생성 시간 순서로 정렬됩니다.
     *
     * @param postId 댓글이 달린 게시글의 ID
     * @return 게시글에 달린 댓글 리스트를 CommentResponseDto로 변환하여 반환
     */
    @Transactional(readOnly = true)
    public List<CommentResponseDto> getCommentsByPostId(Long postId) {

        // 생성 시간 순서로 정렬된 댓글 리스트 조회
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
                // TODO: 대댓글이 달린 댓글을 나중에 삭제할 경우 고려해야 함
                if (parentDto != null) {
                    parentDto.getChildComments().add(commentResponseDto);
                }
            }
        }
        return returnList;
    }

    /**
     * 특정 사용자가 작성한 댓글들을 조회하여 반환합니다.
     *
     * @param userId 조회하려는 사용자의 ID
     * @return 사용자가 작성한 댓글 리스트를 CommentResponseDto로 변환하여 반환
     */
    @Transactional(readOnly = true)
    public List<CommentResponseDto> getCommentsByUserId(String userId) {
        return DtoConverter.convertEntityListToDtoList(commentRepository.findByUserId(userId),CommentResponseDto::toDto);
    }

    /**
     * 새로운 댓글을 작성하여 저장합니다.
     *
     * @param commentRequestDto 작성할 댓글의 정보가 담긴 DTO
     * @param userId            댓글을 작성한 사용자의 ID
     * @param postId            댓글이 달린 게시글의 ID
     * @return 저장된 댓글을 CommentResponseDto로 변환하여 반환
     */
    @Transactional
    public CommentResponseDto createComment(CommentRequestDto commentRequestDto, String userId, Long postId) {

        try {
            Post post = postRepository.findById(postId).orElseThrow(() -> new ComponentNotFoundException("POST_NOT_FOUND"));
            User user = userRepository.findById(userId).orElseThrow(() -> new ComponentNotFoundException("USER_NOT_FOUND"));

            Comment comment = CommentRequestDto.toEntity(post,
                    user,
                    commentRequestDto.getContent(),
                    commentRequestDto.getParentCommentId()
            );

            alarmService.createCommentAlarm(
                    userId,
                    post.getPostId(),
                    post.getUser().getUserId()
            );

            commentRepository.save(comment);

            return CommentResponseDto.toDto(comment);
        } catch (EntityNotFoundException e) {
            log.warn("COMMENT_SAVE_FAILED - {}", e.getLocalizedMessage());
            throw new EntityNotFoundException("COMMENT_SAVE_FAIL_COULD_NOT_FOUND_COMMENT_INFO");
        }
    }

    /**
     * 특정 댓글을 수정합니다.
     *
     * @param commentRequestDto 수정할 댓글 정보가 담긴 DTO
     * @param commentId         수정하려는 댓글의 ID
     * @param userId            댓글을 수정하려는 사용자의 ID
     * @return 수정된 댓글을 CommentResponseDto로 변환하여 반환
     */
    @Transactional
    public CommentResponseDto updateComment(CommentRequestDto commentRequestDto, Long commentId, String userId) {
        Comment comment = commentRepository.findByCommentId(commentId).orElseThrow(() -> new ComponentNotFoundException("COMMENT_NOT_FOUND"));

        // 댓글 수정 요청한 사용자가 댓글 작성자가 아니면 권한 없음 예외 발생
        if (!comment.getUser().getUserId().equalsIgnoreCase(userId))
            throw new UnAuthorizedUserException("UNAUTHORIZED_USER");

        comment.setContent(commentRequestDto.getContent());

        commentRepository.deleteByCommentId(commentId);
        commentRepository.save(comment);
        return CommentResponseDto.toDto(comment);
    }

    /**
     * 특정 댓글을 삭제합니다.
     *
     * @param commentId 삭제하려는 댓글의 ID
     * @param userId    댓글을 삭제하려는 사용자의 ID
     */
    public void deleteArticleComment(Long commentId, String userId) {
        Comment comment = commentRepository.findByCommentId(commentId).orElseThrow(() -> new ComponentNotFoundException("COMMENT_NOT_FOUND"));

        // 댓글 삭제 요청한 사용자가 댓글 작성자가 아니면 권한 없음 예외 발생
        if (!comment.getUser().getUserId().equalsIgnoreCase(userId))
            throw new UnAuthorizedUserException("UNAUTHORIZED_USER");

        commentRepository.deleteByCommentId(commentId);
    }
}
