package com.back.domain.member.member.service;

import com.back.domain.member.member.entity.Member;
import com.back.standard.util.Ut;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthTokenService {
    public String genAccessToken(Member member) {
        String secret = "abcdefghijklmnopqrstuvwxyz123456789abcdefghijklmnopqrstuvwxyz123456789";
        int expireSec = 60 * 60 * 24 * 365;
        long id = member.getId();
        String username = member.getUsername();
        Map<String, Object> claims = Map.of("id", id, "username", username);

        return Ut.jwt.toString(secret, expireSec, claims);
    }
}
