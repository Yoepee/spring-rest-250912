package com.back.domain.member.member.service;

import com.back.domain.member.member.entity.Member;
import com.back.standard.util.Ut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
class AuthTokenService {
    @Value("${custom.jwt.secretKey}")
    private String jwtSecretKey;

    @Value("${custom.accessToken.expireSeconds}")
    private int accessTokenExpireSeconds;

    String genAccessToken(Member member) {
        long id = member.getId();
        String username = member.getUsername();
        Map<String, Object> claims = Map.of("id", id, "username", username);

        return Ut.jwt.toString(jwtSecretKey, accessTokenExpireSeconds, claims);
    }

    Map<String, Object> payload(String accessToken) {
        Map<String, Object> parsePayload = Ut.jwt.payload(jwtSecretKey, accessToken);
        if (parsePayload == null) return null;

        long id = (Integer) parsePayload.get("id");
        String username = (String) parsePayload.get("username");
        return Map.of("id", id, "username", username);
    }
}
