package com.example.demo.service.community.post;

import com.example.demo.converter.DtoConverter;
import com.example.demo.dto.community.post.PostRequestDto;
import com.example.demo.dto.community.post.PostResponseDto;
import com.example.demo.entity.community.post.Attachment;
import com.example.demo.entity.community.post.Post;
import com.example.demo.entity.community.post.PostLike;
import com.example.demo.entity.community.post.View;
import com.example.demo.entity.users.user.User;
import com.example.demo.exception.ComponentNotFoundException;
import com.example.demo.exception.UnAuthorizedUserException;
import com.example.demo.repository.community.post.PostLikeRepository;
import com.example.demo.repository.community.post.PostRepository;
import com.example.demo.repository.users.user.UserRepository;
import com.example.demo.service.users.alarm.AlarmService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostLikeRepository postLikeRepository;
    private final AttachmentFileService attachmentFileService;
    private final S3ImageService s3ImageService;
    private final AlarmService alarmService;

    /**
     * 새 게시글을 생성합니다.
     *
     * @param postRequestDto 게시글 생성 요청 데이터
     * @param userId         게시글 작성자의 사용자 ID
     * @return 생성된 게시글의 응답 데이터
     * @throws ComponentNotFoundException 사용자 ID가 존재하지 않을 경우
     */
    @Transactional
    public PostResponseDto createPost(PostRequestDto postRequestDto, String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ComponentNotFoundException("USER_NOT_FOUND"));
        Post post = postRepository.save(postRequestDto.toEntity(user));
        attachmentFileService.setAttachmentsByContent(post);

        // 새 게시글 생성에 대해 이 사용자를 팔로우하는 사용자에게 알림 생성
        alarmService.createFollowersCreatePostAlarm(post.getPostId(), post.getUser().getUserId());

        return PostResponseDto.toDto(post);
    }

    /**
     * 기존 게시글을 수정합니다.
     *
     * @param postRequestDto 수정할 게시글의 데이터
     * @param postId         수정할 게시글의 ID
     * @param userId         게시글 작성자의 사용자 ID
     * @return 수정된 게시글의 응답 데이터
     * @throws ComponentNotFoundException 게시글 ID가 존재하지 않을 경우
     * @throws UnAuthorizedUserException  사용자가 게시글의 작성자가 아닐 경우
     */
    @Transactional
    public PostResponseDto updatePost(PostRequestDto postRequestDto, Long postId, String userId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ComponentNotFoundException("POST_NOT_FOUND"));
        if (!post.getUser().getUserId().equalsIgnoreCase(userId))
            throw new UnAuthorizedUserException("UNAUTHORIZED_USER");

        // 게시글 수정 시 삭제된 파일을 확인하기 위해 해당 게시글에 있는 모든 이미지 경로 불러오기
        List<String> filePathList = attachmentFileService.getAllFilePathsByPostId(postId, userId);

        // 수정된 게시글의 본문에 이미지가 존재하는지 확인하고 존재하지 않으면 S3와 MongoDB에서 삭제
        filePathList.forEach(filePath -> {
            if (!postRequestDto.getContent().contains(filePath)) {
                s3ImageService.deleteImageFromS3(filePath);
                attachmentFileService.deleteFileData(filePath, postId, userId);
            }
        });

        List<Attachment> attachmentList = attachmentFileService.getAttachmentsByPost(post);
        post = postRepository.save(Post.of(post, postRequestDto, attachmentList));
        return PostResponseDto.toDto(post);
    }

    /**
     * 모든 게시글을 조회합니다.
     *
     * @return 모든 게시글의 응답 데이터 리스트
     */
    public List<PostResponseDto> getAllPosts() {
        return DtoConverter.convertEntityListToDtoList(postRepository.findAll(),PostResponseDto::toDto);
    }

    /**
     * 특정 게시글을 ID로 조회합니다.
     *
     * @param postId 게시글의 ID
     * @param userId 조회하는 사용자의 ID
     * @return 조회된 게시글의 응답 데이터
     * @throws ComponentNotFoundException 게시글 ID가 존재하지 않을 경우
     */
    public PostResponseDto getPostByPostId(Long postId, String userId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ComponentNotFoundException("POST_NOT_FOUND"));

        // 작성자가 아닌 사람이 요청하는 게시글이 비공개 게시글인 경우
        if (post.isOpen() || post.getUser().getUserId().equalsIgnoreCase(userId))
            return null;

        // 조회수 하나 증가
        increaseViews(postId, userId);
        return PostResponseDto.toDto(post);
    }

    /**
     * 특정 사용자가 작성한 게시글을 조회합니다.
     *
     * @param requestedUserId 요청하는 사용자의 ID
     * @param userId          게시글 작성자의 ID
     * @return 요청된 사용자가 작성한 게시글의 응답 데이터 리스트
     * @throws ComponentNotFoundException 사용자 ID가 존재하지 않을 경우
     */
    public List<PostResponseDto> getPostsByUserId(String requestedUserId, String userId) {
        User user = userRepository.findById(requestedUserId).orElseThrow(() -> new ComponentNotFoundException("USER_NOT_FOUND"));
        List<Post> postList = postRepository.findByUser(user);

        // 요청한 사람이 작성자 본인이면 비공개, 공개글 모두 반환
        if (requestedUserId.equalsIgnoreCase(userId))
            return DtoConverter.convertEntityListToDtoList(postList, PostResponseDto::toDto);

        // 요청한 사람이 작성자가 아니면 비공개글은 필터링
        return DtoConverter.convertEntityListToDtoList(postList.stream()
                .filter(Post::isOpen)
                .collect(Collectors.toList()), PostResponseDto::toDto);
    }

    /**
     * 특정 게시글을 삭제합니다.
     *
     * @param postId 게시글의 ID
     * @param userId 게시글 작성자의 사용자 ID
     * @throws ComponentNotFoundException 게시글 ID가 존재하지 않을 경우
     * @throws UnAuthorizedUserException  사용자가 게시글의 작성자가 아닐 경우
     */
    public void deletePostByPostId(Long postId, String userId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ComponentNotFoundException("POST_NOT_FOUND"));
        if (!post.getUser().getUserId().equalsIgnoreCase(userId))
            throw new UnAuthorizedUserException("UNAUTHORIZED_USER");

        List<String> filePathList = attachmentFileService.getAllFilePathsByPostId(postId, userId);
        filePathList.forEach(filePath -> {
            s3ImageService.deleteImageFromS3(filePath);
            attachmentFileService.deleteFileData(filePath, postId, userId);
        });

        postRepository.deleteById(postId);
    }

    /**
     * 특정 게시글의 조회수를 증가시킵니다.
     *
     * @param postId 게시글의 ID
     * @param userId 조회를 수행하는 사용자의 ID
     * @return 조회수가 증가한 게시글 객체
     * @throws ComponentNotFoundException 게시글 ID가 존재하지 않을 경우
     */
    public Post increaseViews(Long postId, String userId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ComponentNotFoundException("POST_NOT_FOUND"));
        User user = userRepository.findById(userId).orElseThrow(() -> new ComponentNotFoundException("USER_NOT_FOUND"));

        Set<View> originalViews = post.getViews();
        originalViews.add(View.toEntity(user, post));
        post.setViews(originalViews);
        postRepository.save(post);

        return postRepository.findById(postId).get();
    }

    /**
     * 특정 게시글에 좋아요를 추가합니다.
     *
     * @param postId 게시글의 ID
     * @param userId 좋아요를 누르는 사용자의 ID
     * @return 좋아요가 추가된 게시글 객체
     * @throws ComponentNotFoundException 게시글 ID가 존재하지 않을 경우
     */
    public Post increaseLike(Long postId, String userId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ComponentNotFoundException("POST_NOT_FOUND"));
        User user = userRepository.findById(userId).orElseThrow(() -> new ComponentNotFoundException("USER_NOT_FOUND"));

        alarmService.createLikeAlarm(userId, postId, post.getUser().getUserId());

        Set<PostLike> postLikeSet = post.getPostLikes();
        postLikeSet.add(PostLike.toEntity(user, post));
        post.setPostLikes(postLikeSet);
        postRepository.save(post);

        return postRepository.findById(post.getPostId()).orElseThrow(() -> new RuntimeException("ERROR_ON_INCREASE_LIKE"));
    }

    /**
     * 특정 게시글의 좋아요를 감소시킵니다.
     *
     * @param postId 게시글의 ID
     * @param userId 좋아요를 제거하는 사용자의 ID
     * @return 현재 게시글에 남아 있는 좋아요 수
     * @throws ComponentNotFoundException 게시글 ID가 존재하지 않을 경우
     */
    public int decreaseLike(Long postId, String userId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ComponentNotFoundException("POST_NOT_FOUND"));

        Set<PostLike> postLikeSet = post.getPostLikes();
        postLikeSet.removeIf(postLike -> postLike.getUser().getUserId().equalsIgnoreCase(userId));
        post.setPostLikes(postLikeSet);
        postRepository.save(post);

        return postLikeSet.size();
    }

    /**
     * 파일이 게시글에 첨부되어 있는지 확인하는 메서드입니다.
     *
     * @param path   파일 경로
     * @param postId 게시글의 ID
     * @return 파일이 게시글에 첨부되어 있는지 여부
     */
    public boolean isFileInPost(String path, Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ComponentNotFoundException("POST_NOT_FOUND"));
        return post.getContent().contains(path);
    }
}
