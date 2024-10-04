package com.example.demo.entity.community.post;

import com.example.demo.dto.community.post.PostRequestDto;
import com.example.demo.entity.community.comment.Comment;
import com.example.demo.entity.users.user.User;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 게시글 정보를 나타내는 엔티티 클래스.
 * 사용자가 작성한 게시물 및 관련 정보를 저장합니다.
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@Entity
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long postId; // 게시글 ID

    @JoinColumn(name = "userId")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User user; // 작성자

    @Column(nullable = false)
    private String title; // 게시글 제목

    @Lob
    @Column(nullable = false)
    private String content; // 게시글 내용

    @Column(nullable = false)
    private boolean open; // 게시글 공개 여부

    private String thumbnailImageId; // 썸네일 이미지 ID

    private LocalDateTime createdAt; // 게시글 작성 시간

    private LocalDateTime modifiedAt; // 게시글 수정 시간

    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Attachment> attachmentList = new ArrayList<>(); // 첨부파일 리스트

    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    @JsonManagedReference
    private Set<View> views = new HashSet<>(); // 조회수 정보

    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    @JsonManagedReference
    private Set<PostLike> postLikes = new HashSet<>(); // 좋아요 정보

    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    @JsonManagedReference
    private Set<Comment> comments = new HashSet<>(); // 댓글 리스트

    /**
     * 게시글을 생성하는 정적 팩토리 메서드.
     * @param post 기존 게시글 정보
     * @param postRequestDto 게시글 요청 DTO
     * @param attachmentList 첨부파일 리스트
     * @return 새롭게 생성된 게시글 인스턴스
     */
    public static Post of(Post post, PostRequestDto postRequestDto, List<Attachment> attachmentList) {
        return Post.builder()
                .postId(post.getPostId())
                .user(post.getUser())
                .title(postRequestDto.getTitle())
                .content(postRequestDto.getContent())
                .open(postRequestDto.isOpen())
                .thumbnailImageId(postRequestDto.getThumbnailImageId())
                .createdAt(post.getCreatedAt())
                .modifiedAt(LocalDateTime.now())
                .attachmentList(attachmentList)
                .views(post.getViews())
                .postLikes(post.getPostLikes())
                .comments(post.getComments())
                .build();
    }

}
