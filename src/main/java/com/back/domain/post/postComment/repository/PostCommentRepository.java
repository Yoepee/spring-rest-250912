package com.back.domain.post.postComment.repository;

import com.back.domain.post.post.entity.Post;
import com.back.domain.post.postComment.entity.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
    long countPostCommentsByPost(Post post);
}
