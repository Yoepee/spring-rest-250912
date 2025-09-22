package com.back.domain.post.post.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.post.post.dto.AdmPostCountResBody;
import com.back.domain.post.post.service.PostService;
import com.back.global.exception.ServiceException;
import com.back.global.rq.Rq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController // @Controller + @ResponseBody
@RequiredArgsConstructor
@RequestMapping("/api/v1/adm/posts")
@Tag(name = "ApiV1AdmPostController", description = "API 글 컨트롤러 - 관리자")
@SecurityRequirement(name="bearerAuth")
public class ApiV1AdmPostController {
    private final PostService postService;
    private final Rq rq;

    @GetMapping("/count")
    @Transactional
    @Operation(summary = "글 갯수 통계")
    public AdmPostCountResBody count() {
        Member actor = rq.getActor();
        if (actor.isNotAdmin()) {
            throw new ServiceException("403-1","권한이 없습니다.");
        }

        return new AdmPostCountResBody(postService.count());
    }
}
