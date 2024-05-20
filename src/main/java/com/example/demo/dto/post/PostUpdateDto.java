package com.example.demo.dto.post;

import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class PostUpdateDto {
    private String title;               //제목
    private String content;             //내용
    private boolean open;               //게시글 공개 여부
    private String thumbnailImageId;    //썸네일 id


    @Builder
    public PostUpdateDto(String title, String content, boolean open,
                         String thumbnailImageId ) {
        this.title = title;
        this.content = content;
        this.open = open;
        this.thumbnailImageId = thumbnailImageId;
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
