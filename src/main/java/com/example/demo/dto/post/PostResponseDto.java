package com.example.demo.dto.post;

import com.example.demo.dto.like.PostLikeResponseDto;
import com.example.demo.dto.view.ViewResponseDto;
import com.example.demo.entity.Post;
import com.example.demo.entity.PostLike;
import com.example.demo.entity.View;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
public class PostResponseDto {

    private Long postId;              //게시글 id

    private String userId;              //회원 id

    private String title;               //제목

    private String content;             //내용

    private String thumbnailImageId;    //썸네일 주소

    private LocalDateTime createdAt;

    private Set<ViewResponseDto> views; // 조회

    private Set<PostLikeResponseDto> postLikes; // 조회

    public static PostResponseDto toDto(Post post) {
        Set<ViewResponseDto> viewResponseDtoSet = new HashSet<>();
        Set<PostLikeResponseDto> postLikeResponseDtoSet = new HashSet<>();

        post.getViews().forEach(view -> {
                    viewResponseDtoSet.add(ViewResponseDto.toDto(view));
                }
        );
        post.getPostLikes().forEach(postLike -> {
                    postLikeResponseDtoSet.add(PostLikeResponseDto.toDto(postLike));
                }
        );

        return new PostResponseDto(
                post.getPostId(),
                post.getUser().getUserId(),
                post.getTitle(),
                post.getContent(),
                post.getThumbnailImageId(),
                post.getCreatedAt(),
                viewResponseDtoSet,
                postLikeResponseDtoSet
        );
    }

}
