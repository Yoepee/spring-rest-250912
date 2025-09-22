package com.back.domain.member.member.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final AuthTokenService authTokenService;
    private final PasswordEncoder passwordEncoder;

    public long count() {
        return memberRepository.count();
    }

    public Member join(String username, String password, String nickname) {
        memberRepository.findByUsername(username)
                .ifPresent(_member -> {
                    throw new ServiceException("409-1", "이미 존재하는 회원입니다.");
                });
        password = passwordEncoder.encode(password);
        Member member = new Member(username, password, nickname);

        return memberRepository.save(member);
    }

    public Optional<Member> findById(Long id) {
        return memberRepository.findById(id);
    }

    public Optional<Member> findByUsername(String user1) {
        return memberRepository.findByUsername(user1);
    }

    public Optional<Member> findByApiKey(String apiKey) {
        return memberRepository.findByApiKey(apiKey);
    }

    public String genAccessToken(Member member) {
        return authTokenService.genAccessToken(member);
    }
    public Map<String, Object> payload(String accessToken) {
        return authTokenService.payload(accessToken);
    }

    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    public void checkPassword(Member member, String password) {
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new ServiceException("401-2", "비밀번호가 일치하지 않습니다.");
        }
    }
}
