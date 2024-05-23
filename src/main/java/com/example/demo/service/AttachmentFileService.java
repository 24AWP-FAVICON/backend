package com.example.demo.service;

import com.amazonaws.Response;
import com.example.demo.entity.Attachment;
import com.example.demo.entity.Post;
import com.example.demo.exception.ComponentNotFoundException;
import com.example.demo.exception.UnAuthorizedUserException;
import com.example.demo.repository.AttachmentFileRepository;
import com.example.demo.repository.PostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class AttachmentFileService {

    private final AttachmentFileRepository attachmentFileRepository;
    private final PostRepository postRepository;


    public void uploadImageMetadata(String profileImage, MultipartFile image, Long postId, String userId) {

        Post post = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("해당하는 글이 존재하지 않습니다."));
        if (post.getUser().getUserId().equalsIgnoreCase(userId)) {

            Attachment attachment = Attachment.builder()
                    .post(post)
                    .filePath(profileImage)
                    .originalFileName(image.getOriginalFilename())
                    .fileSize(image.getSize())
                    .fileType(image.getContentType())
                    .build();

            // mongodb에 파일 메타데이터 저장.(s3 url, size, 이름, 타입)
            attachmentFileRepository.save(attachment);
        } else {
            throw new UnAuthorizedUserException("COULD_NOT_UPLOAD_FILE");
        }
    }
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

    public List<String> getAllFilePathsByPostId(Long postId, String userId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ComponentNotFoundException("POST_NOT_FOUND"));

        if (!post.getUser().getUserId().equalsIgnoreCase(userId))
            throw new UnAuthorizedUserException("UNAUTHORIZED_USER");

        List<Attachment> attachmentFileList = attachmentFileRepository.findByPost(post);
        List<String> filePathList = new ArrayList<>();
        attachmentFileList.forEach(file -> filePathList.add(file.getFilePath()));
        return filePathList;
    }

    public List<Attachment> getAttachmentsByPost(Post post){
        return attachmentFileRepository.findByPost(post);
    }
}
