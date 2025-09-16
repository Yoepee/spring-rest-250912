package com.back.domain.post.postComment.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
import com.back.domain.post.postComment.dto.PostCommentDto;
import com.back.domain.post.postComment.dto.PostCommentUpdateReqDto;
import com.back.domain.post.postComment.dto.PostCommentWriteReqBody;
import com.back.domain.post.postComment.entity.PostComment;
import com.back.domain.post.postComment.service.PostCommentService;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController // @Controller + @ResponseBody
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts/{postId}/comments")
@Tag(name = "ApiV1PostCommentController", description = "API 댓글 컨트롤러")
public class ApiV1PostCommentController {
    private final PostService postService;
    private final PostCommentService postCommentService;
    private final Rq rq;

    @GetMapping("")
    @Transactional(readOnly = true)
    @Operation(summary = "다건 조회")
    public List<PostCommentDto> getComments(
            @PathVariable Long postId
    ) {
        Post post = postService.findById(postId);
        return post.getPostComments().stream().map(PostCommentDto::new).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    @Operation(summary = "단건 조회")
    public PostCommentDto getComment(
            @PathVariable Long postId,
            @PathVariable Long id
    ) {
        Post post = postService.findById(postId);

        return new PostCommentDto(postCommentService.getCommentById(post, id));
    }

    @DeleteMapping("/{id}")
    @Transactional
    @Operation(summary = "삭제")
    public RsData<Void> delete(
            @PathVariable Long postId,
            @PathVariable Long id
    ) {
        Member actor = rq.getActor();
        Post post = postService.findById(postId);
        PostComment postComment = postCommentService.getCommentById(post, id);
        postComment.checkActorCanDelete(actor);

        postCommentService.delete(post, postComment);

        return new RsData<>("200-1", "%d번 댓글이 삭제되었습니다.".formatted(id));
    }

    @PostMapping("")
    @Transactional
    @Operation(summary = "작성")
    public RsData<PostCommentDto> write(
            @PathVariable Long postId,
            @Valid @RequestBody PostCommentWriteReqBody reqBody
    ) {
        Member actor = rq.getActor();
        Post post = postService.findById(postId);
        PostComment postComment = postCommentService.create(actor, post, reqBody.content());
        return new RsData<>(
                "201-1",
                "%d번 댓글이 생성되었습니다.".formatted(postComment.getId()),
                new PostCommentDto(postComment)
        );
    }

    @PutMapping("/{id}")
    @Transactional
    @Operation(summary = "수정")
    public RsData<PostCommentDto> update(
            @PathVariable Long postId,
            @PathVariable Long id,
            @Valid @RequestBody PostCommentUpdateReqDto reqBody
    ) {
        Member actor = rq.getActor();
        Post post = postService.findById(postId);
        PostComment postComment = postCommentService.getCommentById(post, id);
        postComment.checkActorCanModify(actor);

        postCommentService.update(post, postComment, reqBody.content());

        return new RsData<>(
                "200-1",
                "%d번 댓글이 수정되었습니다.".formatted(post.getId()),
                new PostCommentDto(postComment)
        );
    }
}
