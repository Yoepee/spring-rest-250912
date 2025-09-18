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
            SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            Date issuedAt = new Date();
            Date expiration = new Date(issuedAt.getTime() + 1000L * expireSec);

            return Jwts.builder()
                    .claims(claims) // 사용자 정보
                    .issuedAt(issuedAt) // 생성시간
                    .expiration(expiration) // 만료시간
                    .signWith(secretKey) // 서명키
                    .compact();
        }

        public static boolean isValid(String secret, String jwtStr){
            SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            try {
                Jwts.
                        parser()
                        .verifyWith(secretKey)
                        .build()
                        .parse(jwtStr);
            } catch (Exception e) {
                return false;
            }

            return true;
        }
    }
}
