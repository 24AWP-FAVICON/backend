package com.example.demo.controller.community;

import com.example.demo.dto.community.post.PostRequestDto;
import com.example.demo.dto.community.post.PostResponseDto;
import com.example.demo.entity.community.post.Post;
import com.example.demo.entity.users.user.User;
import com.example.demo.service.community.post.AttachmentFileService;
import com.example.demo.service.community.post.PostService;
import com.example.demo.service.community.post.S3ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * PostController는 커뮤니티 내 게시글 관련 기능을 제공하는 컨트롤러입니다.
 * 게시글의 조회, 생성, 수정, 삭제 및 파일 업로드 기능을 포함합니다.
 */
@RequiredArgsConstructor
@RequestMapping("/community")
@RestController
public class PostController {

    private final PostService postService;
    private final S3ImageService s3ImageService;
    private final AttachmentFileService attachmentFileService;

    /**
     * 모든 게시글을 조회합니다.
     *
     * @return 모든 게시글 리스트를 반환
     */
    @GetMapping("/posts")
    public ResponseEntity<List<PostResponseDto>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    /**
     * 게시글 ID를 기반으로 게시글을 조회합니다.
     *
     * @param postId   게시글 ID
     * @param authentication 인증된 사용자 정보
     * @return 특정 게시글 정보를 반환
     */
    @GetMapping("/post/{postId}")
    public ResponseEntity<PostResponseDto> getPostByPostId(@PathVariable Long postId,
                                                           Authentication authentication) {

        String userId = ((User) authentication.getPrincipal()).getUserId();
        return ResponseEntity.ok(postService.getPostByPostId(postId, userId));
    }

    /**
     * 특정 사용자가 작성한 모든 게시글을 조회합니다.
     *
     * @param requestedUserId 요청된 사용자 ID
     * @param authentication  인증된 사용자 정보
     * @return 해당 사용자가 작성한 게시글 리스트를 반환
     */
    @GetMapping("/posts/user/{requestedUserId}")
    public ResponseEntity<List<PostResponseDto>> getAllPostsByUserId(@PathVariable String requestedUserId,
                                                                     Authentication authentication) {
        String userId = ((User) authentication.getPrincipal()).getUserId();

        return ResponseEntity.ok(postService.getPostsByUserId(requestedUserId, userId));
    }

    /**
     * 게시글에 좋아요를 추가합니다.
     *
     * @param postId   게시글 ID
     * @param authentication 인증된 사용자 정보
     * @return 좋아요 수 증가 후 성공 메시지를 반환
     */
    @GetMapping("/post/{postId}/like")
    public ResponseEntity<String> increaseLikes(@PathVariable Long postId,
                                                Authentication authentication) {
        String userId = ((User) authentication.getPrincipal()).getUserId();
        Post post = postService.increaseLike(postId, userId);

        return ResponseEntity.ok("INCREASE_LIKE_SUCCESS_NEW_LIKES: " + post.getPostLikes().size());
    }

    /**
     * 게시글에 좋아요를 취소합니다.
     *
     * @param postId   게시글 ID
     * @param authentication 인증된 사용자 정보
     * @return 좋아요 수 감소 후 성공 메시지를 반환
     */
    @DeleteMapping("/post/{postId}/like")
    public ResponseEntity<String> decreaseLikes(@PathVariable Long postId,
                                                Authentication authentication) {
        String userId = ((User) authentication.getPrincipal()).getUserId();
        int postLikeSize = postService.decreaseLike(postId, userId);

        return ResponseEntity.ok("DECREASE_LIKE_SUCCESS_NEW_LIKES: " + postLikeSize);
    }

    /**
     * 새로운 게시글을 작성합니다.
     *
     * @param postRequestDto 게시글 요청 정보 (제목, 내용 등)
     * @param authentication 인증된 사용자 정보
     * @return 생성된 게시글 정보를 반환
     */
    @PostMapping("/posts")
    public ResponseEntity<PostResponseDto> createPost(@RequestBody PostRequestDto postRequestDto,
                                                      Authentication authentication) {

        String userId = ((User) authentication.getPrincipal()).getUserId();
        PostResponseDto postResponseDto = postService.createPost(postRequestDto, userId);
        return ResponseEntity.ok(postResponseDto);
    }

    /**
     * 기존 게시글을 수정합니다.
     *
     * @param postRequestDto 수정할 게시글 정보
     * @param postId         수정할 게시글 ID
     * @param authentication 인증된 사용자 정보
     * @return 수정된 게시글 정보를 반환
     */
    @PutMapping("/post/{postId}")
    public ResponseEntity<PostResponseDto> updatePost(@RequestBody PostRequestDto postRequestDto,
                                                      @PathVariable Long postId,
                                                      Authentication authentication) {
        String userId = ((User) authentication.getPrincipal()).getUserId();

        PostResponseDto postResponseDto = postService.updatePost(postRequestDto, postId, userId);
        return ResponseEntity.ok(postResponseDto);
    }

    /**
     * 게시글을 삭제합니다.
     *
     * @param postId   삭제할 게시글 ID
     * @param authentication 인증된 사용자 정보
     * @return 게시글 삭제 성공 메시지를 반환
     */
    @DeleteMapping("/post/{postId}")
    public ResponseEntity<String> deletePost(@PathVariable Long postId,
                                             Authentication authentication) {

        String userId = ((User) authentication.getPrincipal()).getUserId();
        postService.deletePostByPostId(postId, userId);
        return ResponseEntity.ok("DELETE_SUCCESS");
    }

    /**
     * S3에 이미지를 업로드하고 저장된 파일 경로를 반환합니다.
     *
     * @param image   업로드할 이미지 파일
     * @param postId  이미지가 첨부될 게시글 ID
     * @param authentication 인증된 사용자 정보
     * @return S3에 업로드된 이미지 파일 경로를 반환
     */
    @PostMapping("/post/{postId}/s3/upload")
    public ResponseEntity<String> s3Upload(@RequestPart(value = "image", required = false) MultipartFile image,
                                           @PathVariable Long postId,
                                           Authentication authentication) {
        String userId = ((User) authentication.getPrincipal()).getUserId();

        String multipartFilePath = s3ImageService.upload(image);
        attachmentFileService.uploadImageMetadata(multipartFilePath, image, postId, userId);
        return ResponseEntity.ok("![]("+multipartFilePath+")");
    }

    /**
     * S3에 저장된 이미지를 삭제합니다.
     *
     * @param path     삭제할 이미지 파일 경로
     * @param postId   이미지가 첨부된 게시글 ID
     * @param authentication 인증된 사용자 정보
     * @return 이미지 삭제 성공 메시지를 반환
     */
    @DeleteMapping("/post/{postId}/s3/delete")
    public ResponseEntity<String> s3delete(@RequestParam String path,
                                           @PathVariable Long postId,
                                           Authentication authentication) {
        String userId = ((User) authentication.getPrincipal()).getUserId();

        if (postService.isFileInPost(path, postId)) {
            attachmentFileService.deleteFileData(path, postId, userId);
            s3ImageService.deleteImageFromS3(path);
            return ResponseEntity.ok("DELETE_SUCCESS");
        }

        return ResponseEntity.badRequest().body("FILE_NOT_FOUND");
    }
}
