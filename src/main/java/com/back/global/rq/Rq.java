package com.back.global.rq;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.global.exception.ServiceException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
@RequiredArgsConstructor
public class Rq {
    private final MemberService memberService;
    private final HttpServletRequest req;

    public Member getActor() {
        String headerAuthorization = req.getHeader("Authorization");
        if (headerAuthorization == null || headerAuthorization.isBlank()) {
            throw new ServiceException("401-1", "Authorization 헤더가 존재하지 않습니다.");
        }
        if (!headerAuthorization.startsWith("Bearer ")) {
            throw new ServiceException("401-2", "Bearer 토큰이 존재하지 않습니다.");
        }

        String apiKey = headerAuthorization.substring("Bearer ".length()).trim();

        return memberService.findByApiKey(apiKey).orElseThrow(() -> new ServiceException("401-3", "유효하지 않은 API Key 입니다."));
    }
}
