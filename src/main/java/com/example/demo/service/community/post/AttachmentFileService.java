package com.example.demo.service.community.post;

import com.example.demo.dto.community.post.PostRequestDto;
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
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Service
public class AttachmentFileService {

    private final AttachmentFileRepository attachmentFileRepository;
    private final PostRepository postRepository;

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

        public void uploadImageMetadata (String profileImage, MultipartFile image, Long postId, String userId){


            Attachment attachment = Attachment.builder()
                    .filePath(profileImage)
                    .originalFileName(image.getOriginalFilename())
                    .fileSize(image.getSize())
                    .fileType(image.getContentType())
                    .build();

            // mongodb에 파일 메타데이터 저장.(s3 url, size, 이름, 타입)
            attachmentFileRepository.save(attachment);
        }
        @Transactional
        public ResponseEntity<?> deleteFileData (String path, Long postId, String userId){
            Post post = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("해당하는 글이 존재하지 않습니다."));
            if (post.getUser().getUserId().equalsIgnoreCase(userId)) {
                attachmentFileRepository.deleteByFilePath(path);
                return ResponseEntity.ok().body("DELETE_SUCCESS");
            } else {
                throw new UnAuthorizedUserException("COULD_NOT_DELETE_FILE");
            }
        }

        public List<String> getAllFilePathsByPostId (Long postId, String userId){
            Post post = postRepository.findById(postId).orElseThrow(() -> new ComponentNotFoundException("POST_NOT_FOUND"));

            if (!post.getUser().getUserId().equalsIgnoreCase(userId))
                throw new UnAuthorizedUserException("UNAUTHORIZED_USER");

            List<Attachment> attachmentFileList = attachmentFileRepository.findByPost(post);
            List<String> filePathList = new ArrayList<>();
            attachmentFileList.forEach(file -> filePathList.add(file.getFilePath()));
            return filePathList;
        }

        public List<Attachment> getAttachmentsByPost (Post post){
            return attachmentFileRepository.findByPost(post);
        }
    }
