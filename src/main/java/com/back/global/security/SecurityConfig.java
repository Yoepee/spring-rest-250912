package com.back.global.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomAuthenticationFilter customAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("favicon.ico").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/*/posts/{id:\\d+}",
                                "/api/*/posts", "/api/*/posts/{postId:\\d+}/comments",
                                "/api/*/posts/{postId:\\d+}/comments/{id:\\d+}").permitAll()
                        .requestMatchers("/api/*/members/login", "/api/*/members/logout").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/*/members").permitAll()
                        .requestMatchers("/api/*/adm/**").hasRole("ADMIN")
                        .requestMatchers("/api/*/**").authenticated()
                        .anyRequest().permitAll()
                )
                .headers(headers -> headers
                        .frameOptions(
                                HeadersConfigurer.FrameOptionsConfig::sameOrigin
                        )
                )
                .csrf(AbstractHttpConfigurer::disable) // csrf 보호기능 비활성화
                .formLogin(AbstractHttpConfigurer::disable) // 기본 로그인 폼 비활성화
                .logout(AbstractHttpConfigurer::disable) // 로그아웃 기능 비활성화
                .httpBasic(AbstractHttpConfigurer::disable) // HTTP 헤더 Authorization: Basic 인증 비활성화
                .sessionManagement(AbstractHttpConfigurer::disable) // 세션 관리 비활성화
                .addFilterBefore(customAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json; charset=UTF-8");
                            response.setStatus(401);
                            response.getWriter().append("""
                                    {
                                        "resultCode":"401-1",
                                        "message":"로그인 후 사용해주세요."
                                    }
                                    """);
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType("application/json; charset=UTF-8");
                            response.setStatus(403);
                            response.getWriter().append("""
                                    {
                                        "resultCode":"403-1",
                                        "message":"권한이 없습니다."
                                    }
                                    """);
                        })
                );

        return http.build();
    }
}
