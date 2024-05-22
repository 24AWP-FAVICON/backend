package com.example.demo.dto.post;

import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@ToString
@RequiredArgsConstructor
public class PostCreateDto {

    private String userId;            //썸네일 id
    private String title;                       //제목
    private String content;                     //내용
    private boolean open = true;                  //게시글 공개 여부
    private String thumbnailImageId;            //썸네일 id

    @Builder
    public PostCreateDto( String title, String content, boolean open,
                         String thumbnailImageId, String userId) {
        this.title = title;
        this.content = content;
        this.open = open;
        this.thumbnailImageId = thumbnailImageId;
        this.userId = userId;
    }

    public Post toEntity(Long postId, User user) {
        //문자열로 받은 해쉬태그를 hashtag set으로 변경 후 저장
        return Post.builder()
                .title(title)
                .content(content)
                .open(open)
                .user(user)
                .thumbnailImageId(thumbnailImageId)
                .postId(postId)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build();
    }
}
