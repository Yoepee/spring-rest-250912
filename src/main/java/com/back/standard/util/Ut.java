package com.back.standard.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

public class Ut {
    public static class jwt {
        public static  String toString(String secret, int expireSec, Map<String, Object> claims) {
            byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
            SecretKey secretKey = Keys.hmacShaKeyFor(keyBytes);
            Date issedAt = new Date();
            Date expiration = new Date(issedAt.getTime() + 1000L * expireSec);

            return Jwts.builder()
                    .claims(claims) // 사용자 정보
                    .issuedAt(issedAt) // 생성시간
                    .expiration(expiration) // 만료시간
                    .signWith(secretKey) // 서명키
                    .compact();
        }
    }
}
