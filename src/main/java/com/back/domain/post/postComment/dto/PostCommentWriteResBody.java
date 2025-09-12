package com.back.domain.post.postComment.dto;

public record PostCommentWriteResBody(
        long totalCount,
        PostCommentDto postComment
) {
}
