package com.example.demo.entity.community.post;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter @Setter
@AllArgsConstructor
@RequiredArgsConstructor
@Entity
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long fileId;

    @JsonBackReference
    @JoinColumn(name = "postId")
    @ManyToOne(fetch = FetchType.LAZY)
    private Post post;

    @Column(nullable = false)
    private String originalFileName;        //파일명

    @Column(nullable = false)
    private String filePath;            //파일 저장 경로 (url)

    @Column(nullable = false)
    private Long fileSize;              //파일 크기

    @Column(nullable = false)
    private String fileType;              //파일 타입

}
