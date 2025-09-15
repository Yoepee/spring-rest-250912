package com.back.domain.member.member.dto;

import com.back.domain.member.member.entity.Member;

import java.time.LocalDateTime;

public record MemberDto(
        long id,
        LocalDateTime createdDate,
        LocalDateTime modifiedDate,
        String nickname
) {
    public MemberDto(Member member) {
        this(
                member.getId(),
                member.getCreatedDate(),
                member.getModifiedDate(),
                member.getNickname()
        );
    }
}