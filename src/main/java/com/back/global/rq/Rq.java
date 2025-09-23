package com.back.global.rq;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.global.security.SecurityUser;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class Rq {
    private final MemberService memberService;
    private final HttpServletRequest req;
    private final HttpServletResponse resp;

    public Member getActor() {
        return Optional.ofNullable(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        )
                .map(Authentication::getPrincipal)
                .filter(principal-> principal instanceof SecurityUser)
                .map(principal -> (SecurityUser) principal)
                .map(securityUser -> new Member(
                        securityUser.getId(),
                        securityUser.getUsername(),
                        securityUser.getNickname()
                ))
                .orElse(null);
    }

    public void setCookie(String name, String value) {
        if (value == null) value = "";
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/"); // 쿠키를 도메인 전체에서 쓰겠다.
        cookie.setHttpOnly(true); // 쿠키를 스크립트로 접근 못하게 (XSS 공격 방어)
        cookie.setDomain("localhost"); // 쿠키가 적용될 도메인 지정
        cookie.setSecure(true); // HTTPS에서만 쿠키 전송
        cookie.setAttribute("sameSite", "Strict"); // 크로스 사이트 요청 위조(CSRF) 공격 방어

        // 값이 빈 문자열이면 쿠키 즉시 삭제
        if (value.isBlank()) {
            cookie.setMaxAge(0);
        } else {
            cookie.setMaxAge(60 * 60 * 24 * 365); // 1년
        }

        resp.addCookie(cookie);
    }

    public void removeCookie(String name) {
        setCookie(name, null);
    }

    public String getHeader(String name, String defaultValue) {
        return Optional.ofNullable(req.getHeader(name))
                .filter(header -> !header.isBlank())
                .orElse(defaultValue);
    }

    public String getCookieValue(String name, String defaultValue) {
        return Optional.ofNullable(req.getCookies())
                .flatMap(cookies ->
                        Arrays.stream(cookies)
                                .filter(cookie -> cookie.getName().equals(name))
                                .map(Cookie::getValue)
                                .findFirst()
                )
                .orElse(defaultValue);
    }

    public void setHeader(String name, String value) {
        resp.setHeader(name, value);
    }
}
