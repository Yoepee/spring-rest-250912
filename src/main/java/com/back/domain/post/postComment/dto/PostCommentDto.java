package com.back.domain.post.postComment.dto;

import com.back.domain.post.postComment.entity.PostComment;

import java.time.LocalDateTime;

public record PostCommentDto(
        long id,
        LocalDateTime createdDate,
        LocalDateTime modifiedDate,
        String authorName,
        String content
) {
    public PostCommentDto(PostComment postComment) {
        this(
                postComment.getId(),
                postComment.getCreatedDate(),
                postComment.getModifiedDate(),
                postComment.getAuthor().getNickname(),
                postComment.getContent()
        );
    }
}
