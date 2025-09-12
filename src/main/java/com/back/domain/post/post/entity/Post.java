package com.back.domain.post.post.entity;

import com.back.domain.post.postComment.entity.PostComment;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.CascadeType.REMOVE;

@Entity
@NoArgsConstructor
@Getter
public class Post extends BaseEntity {
    private String title;
    private String content;

    // PERSIST(지속), REMOVE - 영속성 전이 전달, 부모 저장 시 자식도 자동 저장 혹은 자동 삭제
    // orphanRemoval(고아) - 고아 객체 삭제, 부모와 연관관계가 끊어진 자식은 자동으로 DB에서 삭제함
    @OneToMany(mappedBy="post", fetch = FetchType.LAZY, cascade = {PERSIST, REMOVE}, orphanRemoval = true)
    private List<PostComment> postComments = new ArrayList<>();

    public Post(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void modify(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public PostComment addPostComment(String content) {
        PostComment postComment = new PostComment(this, content);
        postComments.add(postComment);

        return postComment;
    }

    public Optional<PostComment> findCommentById(long id) {
        return postComments.stream()
                .filter(postComment -> postComment.getId() == id)
                .findFirst();
    }

    public void modifyComment(PostComment postComment, String content) {
        postComment.modify(content);
    }

    public boolean deleteComment(PostComment postComment) {
        if (postComment == null) return false;

        return postComments.remove(postComment);
    }
}
