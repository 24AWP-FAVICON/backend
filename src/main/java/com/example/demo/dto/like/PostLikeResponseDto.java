package com.example.demo.dto.like;

import com.example.demo.dto.view.ViewResponseDto;
import com.example.demo.entity.PostLike;
import com.example.demo.entity.View;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class PostLikeResponseDto {
    private String userId;

    private LocalDateTime createdAt;

    public static PostLikeResponseDto toDto(PostLike postLike) {
        return new PostLikeResponseDto(
                postLike.getUser().getUserId(),
                postLike.getCreatedAt()
        );
    }
}
