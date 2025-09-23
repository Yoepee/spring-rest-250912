package com.back.domain.post.postComment.entity;

import com.back.domain.member.member.entity.Member;
import com.back.domain.post.post.entity.Post;
import com.back.global.exception.ServiceException;
import com.back.global.jpa.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class PostComment extends BaseEntity {
    @ManyToOne
    private Member author;
    @ManyToOne
    @JsonIgnore
    private Post post;
    private String content;


    public PostComment(Member author, Post post, String content) {
        this.author = author;
        this.post = post;
        this.content = content;
    }

    public void modify(String content){
        this.content = content;
    }

    public void checkActorCanModify(Member actor) {
        if (!(actor.getId() == this.author.getId())) {
            throw new ServiceException("403-1", "작성자만 댓글을 수정할 수 있습니다.");
        }
    }

    public void checkActorCanDelete(Member actor) {
        if (!(actor.getId() == this.author.getId())) {
            throw new ServiceException("403-1", "작성자만 댓글을 삭제할 수 있습니다.");
        }
    }
}
