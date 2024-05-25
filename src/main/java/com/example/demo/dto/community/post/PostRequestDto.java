package com.example.demo.dto;

import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@ToString
@RequiredArgsConstructor
public class PostRequestDto {

    private String title;                       //제목
    private String content;                     //내용
    private boolean open = true;                  //게시글 공개 여부
    private String thumbnailImageId;            //썸네일 id

    @Builder
    public PostRequestDto(String title, String content, boolean open,
                          String thumbnailImageId ) {
        this.title = title;
        this.content = content;
        this.open = open;
        this.thumbnailImageId = thumbnailImageId;
    }

    public Post toEntity(User user) {
        //문자열로 받은 해쉬태그를 hashtag set으로 변경 후 저장
        return Post.builder()
                .title(title)
                .content(content)
                .open(open)
                .user(user)
                .thumbnailImageId(thumbnailImageId)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build();
    }

    public Post toEntity(Long postId, LocalDateTime createdAt, User user) {

        return Post.builder()
                .title(title)
                .content(content)
                .open(open)
                .thumbnailImageId(thumbnailImageId)
                .user(user)
                .postId(postId)
                .modifiedAt(LocalDateTime.now())
                .createdAt(createdAt)
                .build();
    }
}
