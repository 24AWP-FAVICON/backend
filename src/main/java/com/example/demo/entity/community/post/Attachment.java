package com.example.demo.entity.community.post;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

/**
 * 게시물에 첨부된 파일 정보를 나타내는 엔티티 클래스.
 * 게시물과 연관된 파일의 메타데이터를 저장합니다.
 */
@Builder
@Getter @Setter
@AllArgsConstructor
@RequiredArgsConstructor
@Entity
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long fileId; // 첨부파일 ID

    @JsonBackReference // 순환 참조 방지
    @JoinColumn(name = "postId")
    @ManyToOne(fetch = FetchType.LAZY)
    private Post post; // 첨부된 게시물

    @Column(nullable = false)
    private String originalFileName; // 파일명

    @Column(nullable = false)
    private String filePath; // 파일 저장 경로 (URL)

    @Column(nullable = false)
    private Long fileSize; // 파일 크기 (바이트 단위)

    @Column(nullable = false)
    private String fileType; // 파일 타입 (예: image/png, application/pdf 등)

}
