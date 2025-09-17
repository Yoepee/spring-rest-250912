package com.back.global.initData;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
import com.back.domain.post.postComment.service.PostCommentService;
import com.back.global.app.AppConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@RequiredArgsConstructor
public class BaseInitData {
    private final PostService postService;
    private final PostCommentService postCommentService;
    private final MemberService memberService;

    @Autowired
    @Lazy
    private BaseInitData self;

    @Bean
    ApplicationRunner baseInitDataApplicationRunner() {
        return args -> {
            self.work1();
            self.work2();
        };
    }

    @Transactional
    public void work1() {
        if (memberService.count() > 0) return;

        Member memberSystem =memberService.join("user1", "1234", "유저1");
        if(AppConfig.isNotProd()) memberSystem.modifyApiKey(memberSystem.getUsername());
        Member memberAdmin = memberService.join("user2", "1234", "유저2");
        if(AppConfig.isNotProd()) memberAdmin.modifyApiKey(memberAdmin.getUsername());
        Member memberUser1 = memberService.join("user3", "1234", "유저3");
        if(AppConfig.isNotProd()) memberUser1.modifyApiKey(memberUser1.getUsername());
        Member memberUser2 = memberService.join("user4", "1234", "유저4");
        if(AppConfig.isNotProd()) memberUser2.modifyApiKey(memberUser2.getUsername());
        Member memberUser3 = memberService.join("user5", "1234", "유저5");
        if(AppConfig.isNotProd()) memberUser3.modifyApiKey(memberUser3.getUsername());
    }

    @Transactional
    public void work2() {
        if (postService.count() > 0) return;

        Member member1 = memberService.findByUsername("user1").get();
        Member member2 = memberService.findByUsername("user2").get();
        Member member3 = memberService.findByUsername("user3").get();

        Post post1 = postService.create(member1,"제목 1", "내용 1");
        Post post2 =postService.create(member1, "제목 2", "내용 2");
        postService.create(member2, "제목 3", "내용 3");
        postService.create(member2, "제목 4", "내용 4");
        postService.create(member2, "제목 5", "내용 5");

        postCommentService.create(member1, post1, "댓글 1");
        postCommentService.create(member1, post1, "댓글 2");
        postCommentService.create(member2, post1, "댓글 3");
        postCommentService.create(member2, post2, "댓글 4");
        postCommentService.create(member3,post2, "댓글 5");
        postCommentService.create(member3,post2, "댓글 6");
        postCommentService.create(member3,post2, "댓글 7");
    }
}
