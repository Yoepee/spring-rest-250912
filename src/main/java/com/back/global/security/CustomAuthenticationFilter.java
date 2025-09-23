package com.back.global.security;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.global.exception.ServiceException;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationFilter extends OncePerRequestFilter {
    private final Rq rq;
    private final MemberService memberService;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        logger.debug("CustomAuthenticationFilter: " + request.getRequestURI());

        try {
            work(request, response, filterChain);
        } catch (ServiceException e) {
            RsData<Void> rsData = e.getRsData();
            response.setContentType("application/json");
            response.setStatus(rsData.statusCode());
            response.getWriter().write("""
                    {
                        "resultCode": "%s",
                        "message": "%s"
                    }
                    """.formatted(rsData.resultCode(), rsData.message()));
        }
    }

    private void work(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // API 요청이 아니라면 패스
        if (!request.getRequestURI().startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 인증, 인가가 필요없는 API 요청이라면 패스
        // /api/v1/members/login, /api/v1/members/logout, /api/v1/members/join
        if (List.of(
                "/api/v1/members/login",
                "/api/v1/members/logout",
                "/api/v1/members/join"
        ).contains(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        String apiKey;
        String accessToken;

        String headerAuthorization = rq.getHeader("Authorization", "");

        if (!headerAuthorization.isBlank()) {
            if (!headerAuthorization.startsWith("Bearer ")) {
                throw new ServiceException("401-2", "인증 정보가 올바르지 않습니다.");
            }

            String[] headerAuthorizationBits = headerAuthorization.split(" ", 3);
            apiKey = headerAuthorizationBits[1].trim();
            accessToken = headerAuthorizationBits.length == 3 ? headerAuthorizationBits[2].trim() : "";
        } else {
            apiKey = rq.getCookieValue("apiKey", "");
            accessToken = rq.getCookieValue("accessToken", "");
        }
        logger.debug("apiKey: " + apiKey);
        logger.debug("accessToken: " + accessToken);

        if (apiKey.isBlank() && accessToken.isBlank()){
            filterChain.doFilter(request, response);
            return;
        }

        Member member = null;
        boolean isAccessTokenExists = !accessToken.isBlank();
        boolean isAccessTokenValid = false;
        if (isAccessTokenExists) {
            Map<String, Object> payload = memberService.payload(accessToken);

            if (payload != null) {
                long id = (Long) payload.get("id");
                String username = (String) payload.get("username");
                String nickname = (String) payload.get("nickname");
                member = new Member(id, username, nickname);
                isAccessTokenValid = true;
            }
        }

        if (member == null) {
            member = memberService.findByApiKey(apiKey)
                    .orElseThrow(() -> new ServiceException("401-3", "회원을 찾을 수 없습니다."));
        }

        if (isAccessTokenExists && !isAccessTokenValid) {
            // apiKey(refreshToken)을 이용한 accessToken 재발급
            String actorAccessToken = memberService.genAccessToken(member);
            rq.setCookie("accessToken", actorAccessToken);
            rq.setHeader("Authorization", "Bearer %s %s".formatted(apiKey, actorAccessToken));
        }

        Collection<? extends GrantedAuthority> authorities = member.isAdmin()
                ? List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                : List.of();

        UserDetails user = new SecurityUser(
                member.getId(),
                member.getUsername(),
                "",
                member.getNickname(),
                authorities
        );

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user,
                null,
                user.getAuthorities()
        );

        // 이 시점 이후부터는 시큐리티가 이 요청을 인증된 사용자의 요청으로 취급
        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}
