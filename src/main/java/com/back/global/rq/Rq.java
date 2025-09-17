package com.back.global.rq;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.global.exception.ServiceException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class Rq {
    private final MemberService memberService;
    private final HttpServletRequest req;
    private final HttpServletResponse resp;

    public Member getActor() {
        String headerAuthorization = req.getHeader("Authorization");
        String apiKey;
        if (headerAuthorization != null && !headerAuthorization.isBlank()) {
            if (!headerAuthorization.startsWith("Bearer ")) {
                throw new ServiceException("401-2", "인증 정보가 올바르지 않습니다.");
            }

            apiKey = headerAuthorization.substring("Bearer ".length()).trim();
        } else {
            apiKey = req.getCookies() == null
                    ? null
                    : Arrays.stream(req.getCookies())
                    .filter(cookie -> "apiKey".equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }

        if (apiKey.isBlank()) {
            throw new ServiceException("401-1", "로그인 후 사용해주세요.");
        }

        return memberService.findByApiKey(apiKey).orElseThrow(() -> new ServiceException("401-3", "회원을 찾을 수 없습니다."));
    }

    public void setCookie(String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        resp.addCookie(cookie);
    }
}
