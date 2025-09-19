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
            String[] headerAuthorizationBits = headerAuthorization.split(" ", 3);
            apiKey = headerAuthorizationBits[1].trim();
            accessToken = headerAuthorizationBits.length == 3 ? headerAuthorizationBits[2].trim() : "";
        } else {
            apiKey = getCookieValue("apiKey", "");
            accessToken = getCookieValue("accessToken", "");
        }
        /**
         * 검증
         * 1. accessToken, apiKey(refreshToken) 가지고 있는지 검증
         */

        if (apiKey.isBlank()) throw new ServiceException("401-1", "로그인 후 사용해주세요.");

        Member member = null;
        if (!accessToken.isBlank()) {
            Map<String, Object> payload = memberService.payload(accessToken);

            if (payload == null) {
                throw new ServiceException("401-4", "유효하지 않은 토큰입니다.");
            }

            // username으로 검색은 좋은 코드가 아님 -> 1. DB 조회를 한다. 2. 조회시 인덱싱이 걸려있지 않기에 + 텍스트 조회여서 속도가 PK대비 느림
            // DB 조회를 이용한 회원 검증 - Refresh Token의 역할
            long id = (Long) payload.get("id");
            String username = (String) payload.get("username");
            member = new Member(id, username);
            // 트렌젝션 영속성 때문에 같은 데이터를 다르게 조회해도 SQL이 1번만 날아감
//            member = memberService.findById(id)
//                    .orElseThrow(() -> new ServiceException("401-3", "존재하지 않는 회원입니다."));
//            member = memberService.findByUsername(username)
//                    .orElseThrow(() -> new ServiceException("401-3", "존재하지 않는 회원입니다."));
        }

        if (member == null){
            member = memberService.findByApiKey(apiKey)
                    .orElseThrow(() -> new ServiceException("401-3", "존재하지 않는 회원입니다."));
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
