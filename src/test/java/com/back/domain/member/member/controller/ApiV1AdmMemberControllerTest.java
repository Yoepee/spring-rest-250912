package com.back.domain.member.member.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import jakarta.servlet.http.Cookie;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ApiV1AdmMemberControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private MemberService memberService;

    @Test
    @DisplayName("관리자용 맴버 리스트 조회")
    void t1() throws Exception {
        Member admin = memberService.findByUsername("admin").get();
        String authorApiKey = admin.getApiKey();
        String accessToken = memberService.genAccessToken(admin);

        ResultActions resultActions = mvc.perform(
                get("/api/v1/adm/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer %s %s".formatted(authorApiKey, accessToken))
                        .cookie(new Cookie("apiKey", authorApiKey))
                        .cookie(new Cookie("accessToken", accessToken))
        ).andDo(print());

        List<Member> members = memberService.findAll();

        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(ApiV1AdmMemberController.class))
                .andExpect(handler().methodName("getItems"))
                .andExpect(jsonPath("$.length()").value(members.size()));

        for (int i = 0; i < members.size(); i++) {
            Member member = members.get(i);

            resultActions
                    .andExpect(jsonPath("$[%d].id".formatted(i)).value(member.getId()))
                    .andExpect(jsonPath("$[%d].createdDate".formatted(i)).value(Matchers.startsWith(member.getCreatedDate().toString().substring(0, 20))))
                    .andExpect(jsonPath("$[%d].modifiedDate".formatted(i)).value(Matchers.startsWith(member.getModifiedDate().toString().substring(0, 20))))
                    .andExpect(jsonPath("$[%d].nickname".formatted(i)).value(member.getNickname()))
                    .andExpect(jsonPath("$[%d].username".formatted(i)).value(member.getUsername()));
        }
    }

    @Test
    @DisplayName("단건 조회")
    void t2() throws Exception {
        long id = 1;

        Member admin = memberService.findByUsername("admin").get();
        String authorApiKey = admin.getApiKey();
        String accessToken = memberService.genAccessToken(admin);
        ResultActions resultActions = mvc.perform(
                get("/api/v1/adm/members/%d".formatted(id))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer %s %s".formatted(authorApiKey, accessToken))
                        .cookie(new Cookie("apiKey", authorApiKey))
                        .cookie(new Cookie("accessToken", accessToken))
        ).andDo(print());

        Member member = memberService.findById(id).get();

        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(ApiV1AdmMemberController.class))
                .andExpect(handler().methodName("getItem"))
                .andExpect(jsonPath("$.id").value(member.getId()))
                .andExpect(jsonPath("$.createdDate").value(Matchers.startsWith(member.getCreatedDate().toString().substring(0, 20))))
                .andExpect(jsonPath("$.modifiedDate").value(Matchers.startsWith(member.getModifiedDate().toString().substring(0, 20))))
                .andExpect(jsonPath("$.nickname").value(member.getNickname()))
                .andExpect(jsonPath("$.username").value(member.getUsername()));
    }

    @Test
    @DisplayName("관리자용 맴버 리스트 조회 실패, 403")
    void t3() throws Exception {
        Member user = memberService.findByUsername("user1").get();
        String authorApiKey = user.getApiKey();
        String accessToken = memberService.genAccessToken(user);

        ResultActions resultActions = mvc.perform(
                get("/api/v1/adm/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer %s %s".formatted(authorApiKey, accessToken))
                        .cookie(new Cookie("apiKey", authorApiKey))
                        .cookie(new Cookie("accessToken", accessToken))
        ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1AdmMemberController.class))
                .andExpect(handler().methodName("getItems"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.resultCode").value("403-1"))
                .andExpect(jsonPath("$.message").value("""
                        권한이 없습니다.
                        """.stripIndent().trim()));
    }
}
