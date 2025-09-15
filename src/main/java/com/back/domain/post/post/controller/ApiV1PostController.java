package com.back.domain.post.post.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.domain.post.post.dto.PostDto;
import com.back.domain.post.post.dto.PostUpdateReqBody;
import com.back.domain.post.post.dto.PostUpdateResBody;
import com.back.domain.post.post.dto.PostWriteReqBody;
import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
import com.back.global.exception.ServiceException;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController // @Controller + @ResponseBody
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
@Tag(name = "ApiV1PostController", description = "API 글 컨트롤러")
public class ApiV1PostController {
    private final PostService postService;
    private final MemberService memberService;

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    @Operation(summary = "다건 조회")
    public List<PostDto> getItems() {
        return postService.getList().stream()
                .map(PostDto::new)
                .collect(Collectors.toList());
    }

    @GetMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    @Operation(summary = "단건 조회")
    public PostDto getItem(@PathVariable Long id) {
        return new PostDto(postService.findById(id));
    }

    @DeleteMapping("/{id}")
    @Transactional
    @Operation(summary = "삭제")
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
    @Operation(summary = "작성")
    public RsData<PostDto> write(@Valid @RequestBody PostWriteReqBody reqBody, @NotBlank @Size(min = 2, max = 50) String apiKey) {
        Member author = memberService.findByApiKey(apiKey).orElseThrow(() -> new ServiceException("401-1", "존재하지 않는 회원입니다."));
        Post post = postService.create(author, reqBody.title(), reqBody.content());

        return new RsData<>(
                "201-1",
                "%d번 게시글이 생성되었습니다.".formatted(post.getId()),
                new PostDto(post)
        );
    }

    @PutMapping("/{id}")
    @Transactional
    @Operation(summary = "수정")
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
