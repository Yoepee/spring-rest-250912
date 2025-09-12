package com.back.domain.post.postComment.service;

import com.back.domain.post.post.entity.Post;
import com.back.domain.post.postComment.entity.PostComment;
import com.back.domain.post.postComment.repository.PostCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostCommentService {
    private final PostCommentRepository postCommentRepository;

    public long countPostCommentsByPost(Post post) {
        return postCommentRepository.countPostCommentsByPost(post);
    }

    public PostComment create(Post post, String content) {
        PostComment postComment = post.addPostComment(content);
        return postCommentRepository.save(postComment);
    }

    public boolean delete(Post post, PostComment postComment) {
        return post.deleteComment(postComment);
    }

    public void update(Post post, PostComment postComment, String content) {
        post.modifyComment(postComment, content);
    }

    public PostComment getCommentById(Post post, long id) {
        return post.findCommentById(id).orElseThrow(() -> new RuntimeException("댓글이 존재하지 않습니다."));
    }
}
