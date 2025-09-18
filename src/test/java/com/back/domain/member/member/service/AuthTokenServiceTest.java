package com.back.domain.member.member.service;

import com.back.domain.member.member.entity.Member;
import com.back.standard.util.Ut;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
public class AuthTokenServiceTest {
    @Value("${custom.jwt.secretKey}")
    private String jwtSecretKey;

    @Value("${custom.accessToken.expireSeconds}")
    private int accessTokenExpireSeconds;

    @Autowired
    private AuthTokenService authTokenService;

    @Autowired
    private MemberService memberService;

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
        long expireMillis = 1000L * accessTokenExpireSeconds; // 1년
        byte[] keyBytes = jwtSecretKey.getBytes(StandardCharsets.UTF_8);
        SecretKey secretKey = Keys.hmacShaKeyFor(keyBytes);
        Date issuedAt = new Date();
        Date expiration =new Date(issuedAt.getTime() + expireMillis);

        Map<String, Object> claims = Map.of("name", "Paul", "age", 23);
        String jwt = Jwts.builder()
                .claims(claims) // 사용자 정보
                .issuedAt(issuedAt) // 생성시간
                .expiration(expiration) // 만료시간
                .signWith(secretKey) // 서명키
                .compact();

        assertThat(jwt).isNotBlank();

        System.out.println("jwt : " + jwt);

        assertThat(Ut.jwt.isValid(jwtSecretKey, jwt)).isTrue();
        assertThat(Ut.jwt.payload(jwtSecretKey, jwt)).containsAllEntriesOf(claims);
    }

    @Test
    @DisplayName("Ut.jwt.toString으로 jwt 생성, {name: \"David\", age=20}")
    void t3() {
        Map<String, Object> claims = Map.of("name", "David", "age", 20);
        String jwt = Ut.jwt.toString(jwtSecretKey, accessTokenExpireSeconds, claims);
        assertThat(jwt).isNotBlank();

        System.out.println("jwt : " + jwt);

        assertThat(Ut.jwt.isValid(jwtSecretKey, jwt)).isTrue();
        assertThat(Ut.jwt.payload(jwtSecretKey, jwt)).containsAllEntriesOf(claims);
    }

    @Test
    @DisplayName("AuthTokenService로 jwt 생성, member")
    void t4() {
        Member member = memberService.findByUsername("user1").get();
        String jwt = authTokenService.genAccessToken(member);
        assertThat(jwt).isNotBlank();

        System.out.println("jwt : " + jwt);
        assertThat(Ut.jwt.isValid(jwtSecretKey, jwt)).isTrue();

        Map<String, Object> payload = authTokenService.payload(jwt);
        System.out.println("payload : " + payload);
        assertThat(payload).containsAllEntriesOf(Map.of("id", member.getId(), "username", member.getUsername()));
    }
}
