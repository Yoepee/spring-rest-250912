package com.back.domain.member.member.service;

import com.back.standard.util.Ut;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthTokenService {
    public String genToken(String originSecretKey, long expireMillis, Map<String, Object> body) {
        return Ut.jwt.toString(originSecretKey, expireMillis, body);
    }
}
