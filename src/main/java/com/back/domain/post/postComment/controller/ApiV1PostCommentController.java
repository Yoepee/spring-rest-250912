package com.back.domain.post.postComment.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
import com.back.domain.post.postComment.dto.*;
import com.back.domain.post.postComment.entity.PostComment;
import com.back.domain.post.postComment.service.PostCommentService;
import com.back.global.rsData.RsData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController // @Controller + @ResponseBody
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts/{postId}/comments")
public class ApiV1PostCommentController {
    private final PostService postService;
    private final PostCommentService postCommentService;
    private final MemberService memberService;

    @GetMapping("")
    @Transactional(readOnly = true)
    public List<PostCommentDto> getComments(@PathVariable Long postId) {
        Post post = postService.findById(postId);
        return post.getPostComments().stream().map(PostCommentDto::new).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public PostCommentDto getComment(@PathVariable Long postId, @PathVariable Long id) {
        Post post = postService.findById(postId);

        return new PostCommentDto(postCommentService.getCommentById(post, id));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public RsData<Void> delete(@PathVariable Long postId, @PathVariable Long id) {
        Post post = postService.findById(postId);
        PostComment postComment = postCommentService.getCommentById(post, id);
        postCommentService.delete(post, postComment);

        return new RsData<>("200-1", "%d번 댓글이 삭제되었습니다.".formatted(id));
    }

    @PostMapping("")
    @Transactional
    public RsData<PostCommentWriteResBody> write(@PathVariable Long postId, @Valid @RequestBody PostCommentWriteReqBody reqBody) {
        Member member = memberService.findByUsername("user1");
        Post post = postService.findById(postId);
        PostComment postComment = postCommentService.create(member, post, reqBody.content());
        return new RsData<>("201-1", "%d번 댓글이 생성되었습니다.".formatted(postComment.getId()), new PostCommentWriteResBody(postCommentService.countPostCommentsByPost(post), new PostCommentDto(postComment)));
    }

    @PutMapping("/{id}")
    @Transactional
    public RsData<PostCommentUpdateResDto> update(@PathVariable Long postId, @PathVariable Long id, @Valid @RequestBody PostCommentUpdateReqDto reqBody) {
        Post post = postService.findById(postId);
        PostComment postComment = postCommentService.getCommentById(post, id);
        postCommentService.update(post, postComment, reqBody.content());

        return new RsData<>("200-1", "%d번 댓글이 수정되었습니다.".formatted(post.getId()), new PostCommentUpdateResDto(new PostCommentDto(postComment)));
    }
}
