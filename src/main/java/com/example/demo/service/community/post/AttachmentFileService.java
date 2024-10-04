package com.example.demo.service.community.post;

import com.example.demo.entity.community.post.Attachment;
import com.example.demo.entity.community.post.Post;
import com.example.demo.exception.ComponentNotFoundException;
import com.example.demo.exception.UnAuthorizedUserException;
import com.example.demo.repository.community.post.AttachmentFileRepository;
import com.example.demo.repository.community.post.PostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Service
public class AttachmentFileService {

    private final AttachmentFileRepository attachmentFileRepository;
    private final PostRepository postRepository;

    /**
     * 주어진 게시물에서 콘텐츠를 사용하여 첨부 파일을 설정합니다.
     *
     * @param post 첨부 파일을 설정할 게시물입니다.
     */
    @Transactional
    public void setAttachmentsByContent(Post post) {

        List<Attachment> attachmentList = new ArrayList<>();

        // 정규 표현식을 사용하여 content에서 모든 첨부 파일 경로를 추출합니다.
        String content = post.getContent();
        // 정규 표현식 패턴 설정
        String patternString = "!\\[\\]\\((.*?)\\)";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(content);

        // 매칭된 문자열 추출
        while (matcher.find()) {
            String matchedString = matcher.group(1); // 괄호 안의 내용을 가져옴
            Attachment attachment = attachmentFileRepository.findByFilePath(matchedString).get();
            attachment.setPost(post);
        }
    }

    /**
     * 이미지 메타데이터를 데이터베이스에 업로드하고 MongoDB에 저장합니다.
     *
     * @param profileImage 업로드된 이미지의 URL입니다.
     * @param image       MultipartFile 형식의 이미지입니다.
     * @param postId      이미지와 연관된 게시물의 ID입니다.
     * @param userId      이미지를 업로드하는 사용자의 ID입니다.
     */
    public void uploadImageMetadata(String profileImage, MultipartFile image, Long postId, String userId) {

        Attachment attachment = Attachment.builder()
                .filePath(profileImage)
                .originalFileName(image.getOriginalFilename())
                .fileSize(image.getSize())
                .fileType(image.getContentType())
                .build();

        // mongodb에 파일 메타데이터 저장.(s3 url, size, 이름, 타입)
        attachmentFileRepository.save(attachment);
    }

    /**
     * 특정 게시물과 연관된 파일 데이터를 사용자가 권한이 있는 경우 삭제합니다.
     *
     * @param path   삭제할 첨부 파일의 경로입니다.
     * @param postId 첨부 파일과 연관된 게시물의 ID입니다.
     * @param userId 파일을 삭제하려는 사용자의 ID입니다.
     * @return 삭제 작업의 결과를 나타내는 ResponseEntity입니다.
     */
    @Transactional
    public ResponseEntity<?> deleteFileData(String path, Long postId, String userId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("해당하는 글이 존재하지 않습니다."));
        if (post.getUser().getUserId().equalsIgnoreCase(userId)) {
            attachmentFileRepository.deleteByFilePath(path);
            return ResponseEntity.ok().body("DELETE_SUCCESS");
        } else {
            throw new UnAuthorizedUserException("COULD_NOT_DELETE_FILE");
        }
    }

    /**
     * 특정 게시물과 연관된 모든 파일 경로를 사용자의 ID로 조회합니다.
     *
     * @param postId 게시물의 ID입니다.
     * @param userId 파일 경로를 요청하는 사용자의 ID입니다.
     * @return 게시물과 연관된 파일 경로의 리스트입니다.
     */
    public List<String> getAllFilePathsByPostId(Long postId, String userId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ComponentNotFoundException("POST_NOT_FOUND"));

        if (!post.getUser().getUserId().equalsIgnoreCase(userId))
            throw new UnAuthorizedUserException("UNAUTHORIZED_USER");

        List<Attachment> attachmentFileList = attachmentFileRepository.findByPost(post);
        List<String> filePathList = new ArrayList<>();
        attachmentFileList.forEach(file -> filePathList.add(file.getFilePath()));
        return filePathList;
    }

    /**
     * 특정 게시물과 연관된 모든 첨부 파일을 조회합니다.
     *
     * @param post 첨부 파일을 조회할 게시물입니다.
     * @return 게시물과 연관된 첨부 파일의 리스트입니다.
     */
    public List<Attachment> getAttachmentsByPost(Post post) {
        return attachmentFileRepository.findByPost(post);
    }
}
