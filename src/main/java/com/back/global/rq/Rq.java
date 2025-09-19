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
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class Rq {
    private final MemberService memberService;
    private final HttpServletRequest req;
    private final HttpServletResponse resp;

    public Member getActor() {
        String headerAuthorization = getHeader("Authorization", "");
        String apiKey;
        String accessToken;
        if (!headerAuthorization.isBlank()) {
            if (!headerAuthorization.startsWith("Bearer ")) {
                throw new ServiceException("401-2", "인증 정보가 올바르지 않습니다.");
            }

//            apiKey = headerAuthorization.substring("Bearer ".length()).trim();
            String[] headerAuthorizationBits = headerAuthorization.split("", 3);
            apiKey = headerAuthorizationBits[1].trim();
            accessToken = headerAuthorizationBits.length == 3 ? headerAuthorizationBits[2].trim() : "";
        } else {
            apiKey = getCookieValue("apiKey", "");
            accessToken = getCookieValue("accessToken", "");
        }

        if (apiKey.isBlank()) throw new ServiceException("401-1", "로그인 후 사용해주세요.");

        Member member = null;
        if (!accessToken.isBlank()) {
            Map<String, Object> payload = memberService.payload(accessToken);

            if (payload != null){
                long id = (Long) payload.get("id");
                member = memberService.findById(id)
                        .orElseThrow(() -> new ServiceException("401-3", "존재하지 않는 회원입니다."));
            }
        }
        return member;
    }

    public void setCookie(String name, String value) {
        if (value == null) value = "";
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        if (value.isBlank()) cookie.setMaxAge(0);
        resp.addCookie(cookie);
    }

    public void removeCookie(String name) {
        setCookie(name, null);
    }

    private String getHeader(String name, String defaultValue) {
        return Optional.ofNullable(req.getHeader(name))
                .filter(header -> !header.isBlank())
                .orElse(defaultValue);
    }

    private String getCookieValue(String name, String defaultValue) {
        return Optional.ofNullable(req.getCookies())
                .flatMap(cookies ->
                        Arrays.stream(cookies)
                                .filter(cookie -> cookie.getName().equals(name))
                                .map(Cookie::getValue)
                                .findFirst()
                )
                .orElse(defaultValue);
    }
}
