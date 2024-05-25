package com.example.demo.service;

import com.example.demo.dto.community.post.PostRequestDto;
import com.example.demo.dto.community.post.PostResponseDto;
import com.example.demo.entity.community.post.Post;
import com.example.demo.entity.users.user.User;
import com.example.demo.exception.ComponentNotFoundException;
import com.example.demo.exception.UnAuthorizedUserException;
import com.example.demo.repository.community.post.PostRepository;
import com.example.demo.repository.users.user.UserRepository;
import com.example.demo.service.community.post.AttachmentFileService;
import com.example.demo.service.community.post.PostService;
import com.example.demo.service.community.post.S3ImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AttachmentFileService attachmentFileService;

    @Mock
    private S3ImageService s3ImageService;

    @InjectMocks
    private PostService postService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @DisplayName("포스트 생성 성공 테스트")
    @Test
    void createPost_Success() {
        String userId = "user123";
        PostRequestDto postRequestDto = new PostRequestDto();
        User user = new User();
        user.setUserId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(postRepository.save(any(Post.class))).thenReturn(new Post());

        PostResponseDto result = postService.createPost(postRequestDto, userId);

        assertNotNull(result);
        verify(userRepository).findById(userId);
        verify(postRepository).save(any(Post.class));
    }

    @DisplayName("회원을 못 찾아서 포스트 생성 실패하는 테스트")
    @Test
    void createPost_UserNotFound() {
        String userId = "user123";
        PostRequestDto postRequestDto = new PostRequestDto();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ComponentNotFoundException.class, () -> postService.createPost(postRequestDto, userId));
        verify(userRepository).findById(userId);
    }

    @DisplayName("포스트 업데이트 성공 테스트")
    @Test
    void updatePost_Success() {
        String userId = "user123";
        Long postId = 1L;
        PostRequestDto postRequestDto = new PostRequestDto();
        User user = new User();
        user.setUserId(userId);
        Post post = new Post();
        post.setPostId(postId);
        post.setUser(user);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenReturn(post);

        PostResponseDto result = postService.updatePost(postRequestDto, postId, userId);

        assertNotNull(result);
        verify(postRepository).findById(postId);
        verify(postRepository).save(any(Post.class));
    }

    @DisplayName("권한이 없는 회원이 요청한 포스트 업데이트 실패 테스트")
    @Test
    void updatePost_UnAuthorizedUser() {
        String userId = "user123";
        Long postId = 1L;
        PostRequestDto postRequestDto = new PostRequestDto();
        User user = new User();
        user.setUserId("differentUser");
        Post post = new Post();
        post.setPostId(postId);
        post.setUser(user);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        assertThrows(UnAuthorizedUserException.class, () -> postService.updatePost(postRequestDto, postId, userId));
        verify(postRepository).findById(postId);
    }

    @DisplayName("조회수 증가")
    @Test
    void increaseView_Success() {
        String userId = "user123";
        Long postId = 1L;

        User user = new User();
        user.setUserId(userId);

        Post post = new Post();
        post.setPostId(postId);
        post.setUser(user);
        post.setViews(new HashSet<>());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenReturn(post);

        int originalViewCount = post.getViews().size();

        postService.increaseViews(postId, userId);

        verify(userRepository).findById(userId);
        verify(postRepository, times(2)).findById(postId);
        verify(postRepository).save(any(Post.class));

        int newViewCount = post.getViews().size();

        assertEquals(originalViewCount + 1, newViewCount);
    }
}
