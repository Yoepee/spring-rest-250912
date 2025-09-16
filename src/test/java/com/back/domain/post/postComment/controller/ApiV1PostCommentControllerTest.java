package com.back.domain.post.postComment.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
import com.back.domain.post.postComment.entity.PostComment;
import com.back.domain.post.postComment.service.PostCommentService;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test") // 테스트 환경에서는 test 프로파일을 활성화합니다.
@SpringBootTest // 스프링부트 테스트 클래스임을 나타냅니다.
@AutoConfigureMockMvc // MockMvc를 자동으로 설정합니다.
@Transactional // 각 테스트 메서드가 종료되면 롤백됩니다.
public class ApiV1PostCommentControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private PostService postService;
    @Autowired
    private PostCommentService postCommentService;
    @Autowired
    private MemberService memberService;

    @Test
    @DisplayName("댓글 단건조회")
    void t1() throws Exception {
        long postId = 1;
        long id = 1;

        ResultActions resultActions = mvc.perform(
                get("/api/v1/posts/%d/comments/%d".formatted(postId, id))
        ).andDo(print());

        Post post = postService.findById(postId);
        PostComment postComment = postCommentService.getCommentById(post, id);
        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(ApiV1PostCommentController.class))
                .andExpect(handler().methodName("getComment"))
                .andExpect(jsonPath("$.id").value(postComment.getId()))
                .andExpect(jsonPath("$.createdDate").value(Matchers.startsWith(postComment.getCreatedDate().toString().substring(0, 20))))
                .andExpect(jsonPath("$.modifiedDate").value(Matchers.startsWith(postComment.getModifiedDate().toString().substring(0, 20))))
                .andExpect(jsonPath("$.content").value(postComment.getContent()));
    }

    @Test
    @DisplayName("댓글 다건조회")
    void t2() throws Exception {
        long postId = 1;

        ResultActions resultActions = mvc.perform(
                get("/api/v1/posts/%d/comments".formatted(postId))
        ).andDo(print());

        Post post = postService.findById(postId);
        List<PostComment> postComments = post.getPostComments();
        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(ApiV1PostCommentController.class))
                .andExpect(handler().methodName("getComments"))
                .andExpect(jsonPath("$.length()").value(postComments.size()));

        for (int i = 0; i < postComments.size(); i++) {
            PostComment postComment = postComments.get(i);

            resultActions
                    .andExpect(jsonPath("$[%d].id".formatted(i)).value(postComment.getId()))
                    .andExpect(jsonPath("$[%d].createdDate".formatted(i)).value(Matchers.startsWith(postComment.getCreatedDate().toString().substring(0, 20))))
                    .andExpect(jsonPath("$[%d].modifiedDate".formatted(i)).value(Matchers.startsWith(postComment.getModifiedDate().toString().substring(0, 20))))
                    .andExpect(jsonPath("$[%d].content".formatted(i)).value(postComment.getContent()));
        }
    }

    @Test
    @DisplayName("댓글 삭제")
    void t3() throws Exception {
        long postId = 1;
        long id = 1;

        Post post = postService.findById(postId);
        PostComment postComment = postCommentService.getCommentById(post, id);
        String authorApiKey = postComment.getAuthor().getApiKey();

        ResultActions resultActions = mvc.perform(
                delete("/api/v1/posts/%d/comments/%d".formatted(postId, id))
                        .header("Authorization", "Bearer %s".formatted(authorApiKey))
        ).andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(ApiV1PostCommentController.class))
                .andExpect(handler().methodName("delete"))
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.message").value("%d번 댓글이 삭제되었습니다.".formatted(id)));
    }

    @Test
    @DisplayName("댓글 수정")
    void t4() throws Exception {
        long postId = 1;
        long id = 1;

        Post post = postService.findById(postId);
        PostComment postComment = postCommentService.getCommentById(post, id);
        String authorApiKey = postComment.getAuthor().getApiKey();

        ResultActions resultActions = mvc.perform(
                put("/api/v1/posts/%d/comments/%d".formatted(postId, id))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer %s".formatted(authorApiKey))
                        .content("""
                                {
                                  "content": "내용 update"
                                }
                                """)
        ).andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(ApiV1PostCommentController.class))
                .andExpect(handler().methodName("update"))
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.message").value("%d번 댓글이 수정되었습니다.".formatted(postComment.getId())))
                .andExpect(jsonPath("$.data.id").value(postComment.getId()))
                .andExpect(jsonPath("$.data.content").value("내용 update"));
    }

    @Test
    @DisplayName("댓글 작성")
    void t5() throws Exception {
        long postId = 1;

        Member member = memberService.findByUsername("user1").get();
        String authorApiKey = member.getApiKey();

        ResultActions resultActions = mvc.perform(
                post("/api/v1/posts/%d/comments".formatted(postId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer %s".formatted(authorApiKey))
                        .content("""
                                {
                                  "content": "내용 new"
                                }
                                """)
        ).andDo(print());

        Post post = postService.findById(postId);
        PostComment postComment = post.getPostComments().getLast();

        resultActions
                .andExpect(handler().handlerType(ApiV1PostCommentController.class))
                .andExpect(handler().methodName("write"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultCode").value("201-1"))
                .andExpect(jsonPath("$.message").value("%d번 댓글이 생성되었습니다.".formatted(postComment.getId())))
                .andExpect(jsonPath("$.data.id").value(postComment.getId()))
                .andExpect(jsonPath("$.data.createdDate").value(Matchers.startsWith(postComment.getCreatedDate().toString().substring(0, 20))))
                .andExpect(jsonPath("$.data.modifiedDate").value(Matchers.startsWith(postComment.getModifiedDate().toString().substring(0, 20))))
                .andExpect(jsonPath("$.data.content").value("내용 new"));
    }

    @Test
    @DisplayName("댓글 쓰기 - 헤더 누락 authorization, 401")
    void t6() throws Exception {
        long postId = 1;
        ResultActions resultActions = mvc.perform(
                post("/api/v1/posts/%d/comments".formatted(postId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "내용 new"
                                }
                                """)
        ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostCommentController.class))
                .andExpect(handler().methodName("write"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultCode").value("401-1"))
                .andExpect(jsonPath("$.message").value("""
                        로그인 후 사용해주세요.
                        """.stripIndent().trim()));

    }

    @Test
    @DisplayName("댓글 쓰기 - 잘못된 authorization, 401")
    void t7() throws Exception {
        long postId = 1;
        Member member = memberService.findByUsername("user1").get();
        String authorApiKey = member.getApiKey();
        ResultActions resultActions = mvc.perform(
                post("/api/v1/posts/%d/comments".formatted(postId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "%s".formatted(authorApiKey))
                        .content("""
                                {
                                  "content": "내용 new"
                                }
                                """)
        ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostCommentController.class))
                .andExpect(handler().methodName("write"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultCode").value("401-2"))
                .andExpect(jsonPath("$.message").value("""
                        인증 정보가 올바르지 않습니다.
                        """.stripIndent().trim()));

    }

    @Test
    @DisplayName("댓글 쓰기 - 유효하지 않은 authorization, 401")
    void t8() throws Exception {
        long postId = 1;
        ResultActions resultActions = mvc.perform(
                post("/api/v1/posts/%d/comments".formatted(postId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer wrong")
                        .content("""
                                {
                                  "content": "내용 new"
                                }
                                """)
        ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostCommentController.class))
                .andExpect(handler().methodName("write"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultCode").value("401-3"))
                .andExpect(jsonPath("$.message").value("""
                        회원을 찾을 수 없습니다.
                        """.stripIndent().trim()));

    }

    @Test
    @DisplayName("댓글 수정 - 다른 계정 토큰, 403")
    void t9() throws Exception {
        long postId = 1;
        long id = 1;

        Member member = memberService.findByUsername("user2").get();
        String authorApiKey = member.getApiKey();

        ResultActions resultActions = mvc.perform(
                put("/api/v1/posts/%d/comments/%d".formatted(postId, id))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer %s".formatted(authorApiKey))
                        .content("""
                                {
                                  "content": "내용 update"
                                }
                                """)
        ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostCommentController.class))
                .andExpect(handler().methodName("update"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.resultCode").value("403-1"))
                .andExpect(jsonPath("$.message").value("""
                        작성자만 댓글을 수정할 수 있습니다.
                        """.stripIndent().trim()));

    }

    @Test
    @DisplayName("댓글 삭제 - 다른 계정 토큰, 403")
    void t10() throws Exception {
        long postId = 1;
        long id = 1;

        Member member = memberService.findByUsername("user2").get();
        String authorApiKey = member.getApiKey();

        ResultActions resultActions = mvc.perform(
                delete("/api/v1/posts/%d/comments/%d".formatted(postId, id))
                        .header("Authorization", "Bearer %s".formatted(authorApiKey))
        ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostCommentController.class))
                .andExpect(handler().methodName("delete"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.resultCode").value("403-1"))
                .andExpect(jsonPath("$.message").value("""
                        작성자만 댓글을 삭제할 수 있습니다.
                        """.stripIndent().trim()));

    }
}
