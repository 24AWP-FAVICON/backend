package com.example.demo.service.community.post;


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

    @Transactional
    public PostResponseDto createPost(PostRequestDto postRequestDto, String userId) {

        User user = userRepository.findById(userId).orElseThrow(() -> new ComponentNotFoundException("USER_NOT_FOUND"));

        Post post = postRepository.save(postRequestDto.toEntity(user));

        attachmentFileService.setAttachmentsByContent(post);

        // 새 게시글 생성에 대해서 이 회원을 팔로우하고 있는 회원에게 알림 생성
        alarmService.createFollowersCreatePostAlarm(
                post.getPostId(),
                post.getUser().getUserId()
        );

        return PostResponseDto.toDto(post);
    }

    @Transactional
    public PostResponseDto updatePost(PostRequestDto postRequestDto, Long postId, String userId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ComponentNotFoundException("POST_NOT_FOUND"));

        if (!post.getUser().getUserId().equalsIgnoreCase(userId))
            throw new UnAuthorizedUserException("UNAUTHORIZED_USER");

        // 게시글 수정시 삭제된 파일을 확인하기 위해 해당 게시글에 있는 모든 이미지 경로 불러오기
        List<String> FilePathList = attachmentFileService.getAllFilePathsByPostId(postId, userId);

        // 수정된 게시글의 본문에 이미지가 존재하는지 확인하고 존재하지 않으면 s3와 mongodb에서 삭제.
        FilePathList.forEach(filePath -> {
            if (!postRequestDto.getContent().contains(filePath)) {
                s3ImageService.deleteImageFromS3(filePath);
                attachmentFileService.deleteFileData(filePath, postId, userId);
            }
        });

        List<Attachment> attachmentList = attachmentFileService.getAttachmentsByPost(post);

        post = postRepository.save(Post.of(post, postRequestDto, attachmentList));
        return PostResponseDto.toDto(post);
    }

    public List<PostResponseDto> getAllPosts() {
        return fromPostListToPostResponseDtoList(postRepository.findAll());
    }

    public PostResponseDto getPostByPostId(Long postId, String userId) {

        Post post = postRepository.findById(postId).orElseThrow(() -> new ComponentNotFoundException("POST_NOT_FOUND"));

        //작성자가 아닌 사람이 요청하는 게시글이 비공개 게시글인 경우
        if (post.isOpen() || post.getUser().getUserId().equalsIgnoreCase(userId))
            return null;

        //조회수 하나 증가
        increaseViews(postId, userId);
        return PostResponseDto.toDto(post);
    }

    public List<PostResponseDto> getPostsByUserId(String requestedUserId, String userId) {
        User user = userRepository.findById(requestedUserId).orElseThrow(() -> new ComponentNotFoundException("USER_NOT_FOUND"));
        List<Post> postList = postRepository.findByUser(user);

        //요청한 사람이 작성자 본인이면 비공개, 공개글 모두 반환
        if (requestedUserId.equalsIgnoreCase(userId))
            return fromPostListToPostResponseDtoList(postList);

        //요청한 사람이 작성자가 아니면 비공개글은 필터링.
        return fromPostListToPostResponseDtoList(postList.stream()
                .filter(Post::isOpen)
                .collect(Collectors.toList()));
    }

    //포스트 삭제
    public void deletePostByPostId(Long postId, String userId) {

        Post post = postRepository.findById(postId).orElseThrow(() -> new ComponentNotFoundException("POST_NOT_FOUND"));

        if (!post.getUser().getUserId().equalsIgnoreCase(userId))
            throw new UnAuthorizedUserException("UNAUTHORIZED_USER");

        List<String> FilePathList = attachmentFileService.getAllFilePathsByPostId(postId, userId);
        FilePathList.forEach(filePath -> {
            s3ImageService.deleteImageFromS3(filePath);
            attachmentFileService.deleteFileData(filePath, postId, userId);
        });

        postRepository.deleteById(postId);
    }

    //조회수 증가
    public Post increaseViews(Long postId, String userId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ComponentNotFoundException("POST_NOT_FOUND"));
        User user = userRepository.findById(userId).orElseThrow(() -> new ComponentNotFoundException("USER_NOT_FOUND"));

        Set<View> originalViews = post.getViews();
        originalViews.add(View.toEntity(user, post));
        post.setViews(originalViews);
        postRepository.save(post);

        return postRepository.findById(postId).get();
    }

    //게시글 좋아요 하나 증가
    public Post increaseLike(Long postId, String userId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ComponentNotFoundException("POST_NOT_FOUND"));
        User user = userRepository.findById(userId).orElseThrow(() -> new ComponentNotFoundException("USER_NOT_FOUND"));

        alarmService.createLikeAlarm(userId,
                postId,
                post.getUser().getUserId()
        );

        Set<PostLike> postLikeSet = post.getPostLikes();
        postLikeSet.add(PostLike.toEntity(user, post));
        post.setPostLikes(postLikeSet);
        postRepository.save(post);

        return postRepository.findById(post.getPostId()).orElseThrow(() -> new RuntimeException("ERROR_ON_INCREASE_LIKE"));
    }

    //게시글 좋아요 하나 감소
    public int decreaseLike(Long postId, String userId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ComponentNotFoundException("POST_NOT_FOUND"));

        if (post.getPostLikes().isEmpty())
            throw new IndexOutOfBoundsException("CAN_NOT_DECREASE_LIKE");

        postLikeRepository.deleteByPostId(postId, userId);
        return postLikeRepository.getPostLikeByPost(post).size();
    }


    public boolean isFileInPost(String path, Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ComponentNotFoundException("POST_NOT_FOUND"));
        return post.getContent().contains(path);
    }

    public List<PostResponseDto> fromPostListToPostResponseDtoList(List<Post> postList) {
        List<PostResponseDto> postResponseDtoList = new ArrayList<>();
        postList.forEach(post -> postResponseDtoList.add(PostResponseDto.toDto(post)));
        return postResponseDtoList;
    }
}
