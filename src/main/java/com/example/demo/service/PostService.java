package com.example.demo.service;


import com.example.demo.dto.post.PostCreateDto;
import com.example.demo.dto.post.PostUpdateDto;
import com.example.demo.entity.PostLike;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.entity.View;
import com.example.demo.exception.ComponentNotFoundException;
import com.example.demo.exception.UnAuthorizedUserException;
import com.example.demo.repository.PostLikeRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    public Long getMaxPostId() {
        List<Post> postList = postRepository.findAll();
        if (postList.isEmpty())
            return 1L;
        else
            return postList.stream().max(Comparator.comparingLong(Post::getPostId)).get().getPostId() + 1;
    }

    public Post createPost(PostCreateDto postCreateDto, String userId) {

        User user = userRepository.findById(userId).orElseThrow(() -> new ComponentNotFoundException("USER_NOT_FOUND"));
        Long postId = getMaxPostId();

        return postRepository.save(postCreateDto.toEntity(postId, user));
    }

    public Post updatePost(PostUpdateDto postUpdateDto, Long postId, String userId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ComponentNotFoundException("POST_NOT_FOUND"));

        if (!post.getUser().getUserId().equalsIgnoreCase(userId))
            throw new UnAuthorizedUserException("UNAUTHORIZED_USER");

        // 게시글 수정시 삭제된 파일을 확인하기 위해 해당 게시글에 있는 모든 이미지 경로 불러오기
        List<String> FilePathList = attachmentFileService.getAllFilePathsByPostId(postId, userId);

        // 수정된 게시글의 본문에 이미지가 존재하는지 확인하고 존재하지 않으면 s3와 mongodb에서 삭제.
        FilePathList.forEach(filePath -> {
            if (!postUpdateDto.getContent().contains(filePath)) {
                s3ImageService.deleteImageFromS3(filePath);
                attachmentFileService.deleteFileData(filePath, postId, userId);
            }
        });

        // id, 조회수, 좋아요, 생성일자는 이전 포스트에서 그대로 가져와야 하는 필드
        Long originalPostId = post.getPostId();
        LocalDateTime createdAt = post.getCreatedAt();
        User user = post.getUser();

        return postRepository.save(postUpdateDto.toEntity(originalPostId, createdAt, user));
    }

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public Post getPostByPostId(Long postId, String userId) {

        Post post = postRepository.findById(postId).orElseThrow(() -> new ComponentNotFoundException("POST_NOT_FOUND"));

        //작성자가 아닌 사람이 요청하는 게시글이 비공개 게시글인 경우
        if (!post.getUser().getUserId().equalsIgnoreCase(userId) && !post.isOpen())
            return null;

        //조회수 하나 증가
        increaseViews(postId, userId);
        return post;
    }

    public List<Post> getPostsByUserId(String requestUserId, String userId) {
        List<Post> postList = postRepository.findByUser(userRepository.findById(requestUserId).orElse(null));

        if (postList == null)
            return null;

        //요청한 사람이 작성자 본인이면 비공개, 공개글 모두 반환
        if (requestUserId.equalsIgnoreCase(userId))
            return postList;

        //요청한 사람이 작성자가 아니면 비공개글은 필터링.
        return postList.stream()
                .filter(Post::isOpen)
                .collect(Collectors.toList());
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
        Post post  = postRepository.findById(postId).orElseThrow( () -> new ComponentNotFoundException("POST_NOT_FOUND"));
        User user = userRepository.findById(userId).orElseThrow(() -> new ComponentNotFoundException("USER_NOT_FOUND"));

        Set<View> originalViews = post.getViews();
        originalViews.add(View.toEntity(user, post));
        post.setViews(originalViews);
        postRepository.save(post);

        return postRepository.findById(postId).get();
    }

    //게시글 좋아요 하나 증가
    public Post increaseLike(Long postId, String userId) {
        Post post  = postRepository.findById(postId).orElseThrow( () -> new ComponentNotFoundException("POST_NOT_FOUND"));
        User user = userRepository.findById(userId).orElseThrow(() -> new ComponentNotFoundException("USER_NOT_FOUND"));

        Set<PostLike> postLikeSet = post.getPostLikes();
        postLikeSet.add(PostLike.toEntity(user,post));
        post.setPostLikes(postLikeSet);
        postRepository.save(post);

        return postRepository.findById(post.getPostId()).orElse(null);
    }

    //게시글 좋아요 하나 감소
    public String decreaseLike(Long postId, String userId) {
        Post post  = postRepository.findById(postId).orElseThrow( () -> new ComponentNotFoundException("POST_NOT_FOUND"));

        if (post.getPostLikes().isEmpty())
            throw new IndexOutOfBoundsException("CAN_NOT_DECREASE_LIKE");

        postLikeRepository.deleteByPostId(postId, userId);
        return String.valueOf(postLikeRepository.getPostLikeByPost(post).size());
    }


    public boolean isFileInPost(String path, Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ComponentNotFoundException("POST_NOT_FOUND"));
        return post.getContent().contains(path);
    }

}
