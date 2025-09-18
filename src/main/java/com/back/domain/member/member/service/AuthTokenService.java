package com.back.domain.member.member.service;

import com.back.domain.member.member.entity.Member;
import com.back.standard.util.Ut;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthTokenService {
    String secret = "abcdefghijklmnopqrstuvwxyz123456789abcdefghijklmnopqrstuvwxyz123456789";

    public String genAccessToken(Member member) {
        int expireSec = 60 * 60 * 24 * 365;
        long id = member.getId();
        String username = member.getUsername();
        Map<String, Object> claims = Map.of("id", id, "username", username);

        return Ut.jwt.toString(secret, expireSec, claims);
    }

    public Map<String, Object> payload(String accessToken) {
        Map<String, Object> parsePayload = Ut.jwt.payload(secret, accessToken);
        if (parsePayload == null) return null;

        long id = ((Number) parsePayload.get("id")).longValue();
        String username = (String) parsePayload.get("username");
        return Map.of("id", id, "username", username);
    }
}
