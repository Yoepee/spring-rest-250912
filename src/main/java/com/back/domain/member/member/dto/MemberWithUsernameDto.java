package com.back.domain.member.member.dto;

import com.back.domain.member.member.entity.Member;

import java.time.LocalDateTime;

public record MemberWithUsernameDto(
        long id,
        LocalDateTime createdDate,
        LocalDateTime modifiedDate,
        String nickname,
        String username
) {
    public MemberWithUsernameDto(Member member) {
        this(
                member.getId(),
                member.getCreatedDate(),
                member.getModifiedDate(),
                member.getNickname(),
                member.getUsername()
        );
    }
}