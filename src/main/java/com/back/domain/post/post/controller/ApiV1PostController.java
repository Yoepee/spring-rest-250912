package com.back.domain.post.post.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.domain.post.post.dto.PostDto;
import com.back.domain.post.post.dto.PostUpdateReqBody;
import com.back.domain.post.post.dto.PostUpdateResBody;
import com.back.domain.post.post.dto.PostWriteReqBody;
import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
import com.back.global.rsData.RsData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController // @Controller + @ResponseBody
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
public class ApiV1PostController {
    private final PostService postService;
    private final MemberService memberService;

    @GetMapping("")
    @Transactional(readOnly = true)
    public List<PostDto> getItems() {
        return postService.getList().stream()
                .map(PostDto::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public PostDto getItem(@PathVariable Long id) {
        return new PostDto(postService.findById(id));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public RsData<PostDto> delete(@PathVariable Long id) {
        Post post = postService.findById(id);
        postService.delete(post);

        return new RsData<>(
                "200-1",
                "%d번 게시글이 삭제되었습니다.".formatted(id),
                new PostDto(post)
        );
    }

    @PostMapping("")
    @Transactional
    public RsData<PostDto> write(@Valid @RequestBody PostWriteReqBody reqBody) {
        Member member = memberService.findByUsername("user1");
        Post post = postService.create(member, reqBody.title(), reqBody.content());

        return new RsData<>(
                "201-1",
                "%d번 게시글이 생성되었습니다.".formatted(post.getId()),
                new PostDto(post)
        );
    }

    @PutMapping("/{id}")
    @Transactional
    public RsData<PostUpdateResBody> update(@PathVariable Long id, @Valid @RequestBody PostUpdateReqBody reqBody) {
        Post post = postService.findById(id);
        postService.update(post, reqBody.title(), reqBody.content());

        return new RsData<>(
                "200-1",
                "%d번 게시글이 수정되었습니다.".formatted(post.getId()),
                new PostUpdateResBody(new PostDto(post))
        );
    }
}
