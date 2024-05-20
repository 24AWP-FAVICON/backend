package com.example.demo.controller;

import com.example.demo.dto.post.PostCreateDto;
import com.example.demo.dto.post.PostResponseDto;
import com.example.demo.dto.post.PostUpdateDto;
import com.example.demo.entity.Post;
import com.example.demo.service.AttachmentFileService;
import com.example.demo.service.JwtCheckService;
import com.example.demo.service.PostService;
import com.example.demo.service.S3ImageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/post")
@RestController
public class PostController {
    private final PostService postService;
    private final JwtCheckService jwtCheckService;
    private final S3ImageService s3ImageService;
    private final AttachmentFileService attachmentFileService;

    //모든 게시글 불러오기
    @GetMapping("/all-posts")
    public List<PostResponseDto> getAllPosts(HttpServletRequest request,
                                             HttpServletResponse response) {
        List<PostResponseDto> responseDtos = new ArrayList<>();

        postService.getAllPosts().forEach(post -> {
            responseDtos.add(PostResponseDto.toDto(post));
        });

        return responseDtos;
    }

    //게시글 id로 게시글 조회
    @GetMapping("/{postId}")
    public PostResponseDto getPostByPostId(@PathVariable Long postId,
                                HttpServletRequest request,
                                HttpServletResponse response) {

        String userId = jwtCheckService.checkJwt(request, response);

        return PostResponseDto.toDto( postService.getPostByPostId(postId, userId));
    }

    //회원이 작성한 모든 게시글 불러오기
    @GetMapping("/user/{requestedUserId}")
    public List<PostResponseDto> getAllPostsByUserId(@PathVariable String requestedUserId,
                                          HttpServletRequest request,
                                          HttpServletResponse response) {
        String userId = jwtCheckService.checkJwt(request, response);

        List<PostResponseDto> responseList = new ArrayList<>();
        postService.getPostsByUserId(requestedUserId, userId).forEach(post -> {
            responseList.add(PostResponseDto.toDto(post));
        });

        return responseList;
    }

    //게시글 좋아요 하나 증가
    @GetMapping("/likes/increase/{postId}")
    public ResponseEntity<?> increaseLikes(@PathVariable Long postId,
                                           HttpServletRequest request,
                                           HttpServletResponse response) {
        String userId = jwtCheckService.checkJwt(request, response);
        Post post = postService.increaseLike(postId, userId);

        return ResponseEntity.ok("INCREASE_LIKE_SUCCESS_NEW_LIKES: " + post.getPostLikes().size());
    }

    //게시글 좋아요 하나 감소
    @DeleteMapping("/likes/decrease/{postId}")
    public ResponseEntity<?> decreaseLikes(@PathVariable Long postId,
                                           HttpServletRequest request,
                                           HttpServletResponse response) {
        String userId = jwtCheckService.checkJwt(request, response);
        String postLikeSize = postService.decreaseLike(postId, userId);

        return ResponseEntity.ok("DECREASE_LIKE_SUCCESS_NEW_LIKES: " + postLikeSize);
    }


    //게시글 작성. 사용자로부터 제목, 내용, 해쉬태그, 공개여부, 썸네일 이미지 주소, 회원 id, 댓글 허용여부, 시리즈 id 받아야 함.
    @PostMapping("/create-post")
    public ResponseEntity<?> createPost(@RequestBody PostCreateDto postCreateDto,
                                        HttpServletRequest request,
                                        HttpServletResponse response) {

        String userId = jwtCheckService.checkJwt(request, response);
        Post post = postService.createPost(postCreateDto, userId);
        PostResponseDto responseDto = PostResponseDto.toDto(postService.getPostByPostId(post.getPostId(), post.getUser().getUserId()));

        return ResponseEntity.ok(responseDto);
    }

    //게시글 수정. 제목, 내용, 해쉬태그, 공개여부, 썸네일, 시리즈 id 수정 가능.
    @PostMapping("/update-post/{postId}")
    public ResponseEntity<?> updatePost(@RequestBody PostUpdateDto postUpdateDto,
                                           @PathVariable Long postId,
                                           HttpServletRequest request,
                                           HttpServletResponse response) {
        String userId = jwtCheckService.checkJwt(request, response);

        PostResponseDto responseDto = PostResponseDto.toDto(postService.updatePost(postUpdateDto, postId, userId));

        return ResponseEntity.ok(responseDto);
    }

    //게시글 삭제. 게시글 id 받아야 함.
    @DeleteMapping("/delete-post/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable Long postId,
                                        HttpServletRequest request,
                                        HttpServletResponse response) {

        String userId = jwtCheckService.checkJwt(request, response);

        postService.deletePostByPostId(postId, userId);
        return ResponseEntity.ok("DELETE_SUCCESS");
    }


    //s3에 파일 업로드하고 response entity로 s3 저장 경로 반환.
    @PostMapping("/s3/upload/{postId}")
    public ResponseEntity<?> s3Upload(@RequestPart(value = "image", required = false) MultipartFile image,
                                      @PathVariable Long postId,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {
        String userId = jwtCheckService.checkJwt(request, response);

        String multipartFilePath = s3ImageService.upload(image);
        attachmentFileService.uploadImageMetadata(multipartFilePath, image, postId, userId);
        return ResponseEntity.ok(multipartFilePath);
    }

    //s3에 저장된 파일 삭제.
    @DeleteMapping("/s3/delete/{postId}")
    public ResponseEntity<?> s3delete(@RequestParam String path,
                                      @PathVariable Long postId,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {
        String userId = jwtCheckService.checkJwt(request, response);

        if (postService.isFileInPost(path,postId)) {
            attachmentFileService.deleteFileData(path, postId, userId);
            s3ImageService.deleteImageFromS3(path);
            return ResponseEntity.ok("DELETE_SUCCESS");
        }

        return ResponseEntity.badRequest().body("FILE_NOT_FOUND");
    }

}
