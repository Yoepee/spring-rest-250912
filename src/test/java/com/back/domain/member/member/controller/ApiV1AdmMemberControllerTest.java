package com.back.domain.member.member.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
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
    @DisplayName("다건조회 - 관리자")
    @WithUserDetails("admin")
    void t1() throws Exception {
        ResultActions resultActions = mvc.perform(
                get("/api/v1/adm/members")
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
    @DisplayName("단건 조회 - 관리자")
    @WithUserDetails("admin")
    void t2() throws Exception {
        long id = 1;

        ResultActions resultActions = mvc.perform(
                get("/api/v1/adm/members/%d".formatted(id))
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
    @DisplayName("다건 조회 실패 - 관리자, 403")
    @WithUserDetails("user1")
    void t3() throws Exception {
        ResultActions resultActions = mvc.perform(
                get("/api/v1/adm/members")
        ).andDo(print());

        resultActions
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.resultCode").value("403-1"))
                .andExpect(jsonPath("$.message").value("""
                        권한이 없습니다.
                        """.stripIndent().trim()));
    }

    @Test
    @DisplayName("단건 조회 실패 - 관리자, 403")
    @WithUserDetails("user1")
    void t4() throws Exception {
        long id = 1;

        ResultActions resultActions = mvc.perform(
                get("/api/v1/adm/members/%d".formatted(id))
        ).andDo(print());

        resultActions
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.resultCode").value("403-1"))
                .andExpect(jsonPath("$.message").value("""
                        권한이 없습니다.
                        """.stripIndent().trim()));
    }
}
