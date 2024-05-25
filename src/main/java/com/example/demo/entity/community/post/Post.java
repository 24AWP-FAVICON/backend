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
    private Long postId;              //게시글 id

    @JoinColumn(name = "userId")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User user;              //회원 id

    @Column(nullable = false)
    private String title;               //제목

    @Lob
    @Column(nullable = false)
    private String content;             //내용

    @Column(nullable = false)
    private boolean open;               //게시글 공개 여부

    private String thumbnailImageId;    //썸네일 주소

    private LocalDateTime createdAt;

    private LocalDateTime modifiedAt;

    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Attachment> attachmentList = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    @JsonManagedReference
    private Set<View> views = new HashSet<>(); // 조회

    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    @JsonManagedReference
    private Set<PostLike> postLikes = new HashSet<>();                 //좋아요

    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    @JsonManagedReference
    private Set<Comment> comments = new HashSet<>();               //댓글

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
