package com.back.domain.member.member.service;

import com.back.standard.util.Ut;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
public class AuthTokenServiceTest {
    @Autowired
    private AuthTokenService authTokenService;
    String originSecretKey = "abcdefghijklmnopqrstuvwxyz123456789abcdefghijklmnopqrstuvwxyz123456789";
    long expireMillis = 1000L * 60 * 60 * 24 * 365;

    @Test
    @DisplayName("authTokenService가 존재한다.")
    void t1() {
        assertThat(authTokenService).isNotNull();
    }

    @Test
    @DisplayName("jjwt 최신 방식으로 jwt 생성, {name: \"Paul\", age=23}")
    void t2() {
//        서명키 (SecretKey)
//        서명 알고리즘 (SignatureAlgorithm)
//        클레임 (Claims)
//          토큰에 담을 데이터
//          예) {name="Paul", age=20}
//        생성시간 (Issued At : iat)
//        만료시간 (Expiration Time : exp)
        long expireMillis = 1000L * 60 * 60 * 24 * 365; // 1년
        String originSecretKey = "abcdefghijklmnopqrstuvwxyz123456789abcdefghijklmnopqrstuvwxyz123456789"; // 32byte 이상
        byte[] keyBytes = originSecretKey.getBytes(StandardCharsets.UTF_8);
        SecretKey secretKey = Keys.hmacShaKeyFor(keyBytes);
        Date issedAt = new Date();
        Date expiration =new Date(issedAt.getTime() + expireMillis);

        Map<String, Object> claims = Map.of("name", "Paul", "age", 23);
        String jwt = Jwts.builder()
                .claims(claims) // 사용자 정보
                .issuedAt(issedAt) // 생성시간
                .expiration(expiration) // 만료시간
                .signWith(secretKey) // 서명키
                .compact();

        assertThat(jwt).isNotBlank();

        System.out.println("jwt : " + jwt);
    }

    @Test
    @DisplayName("Ut.jwt.toString으로 jwt 생성, {name: \"David\", age=20}")
    void t3() {
        String jwt = Ut.jwt.toString(originSecretKey, expireMillis, Map.of("name", "David", "age", 20));
        assertThat(jwt).isNotBlank();

        System.out.println("jwt : " + jwt);
    }
}
