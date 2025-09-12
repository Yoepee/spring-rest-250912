package com.back.domain.post.post.dto;

import com.back.domain.post.post.entity.Post;

import java.time.LocalDateTime;

public record PostDto(
        long id,
        LocalDateTime createdDate,
        LocalDateTime modifiedDate,
        String title,
        String content
) {
    public PostDto(Post post) {
        this(
                post.getId(),
                post.getCreatedDate(),
                post.getModifiedDate(),
                post.getTitle(),
                post.getContent()
        );
    }
}
