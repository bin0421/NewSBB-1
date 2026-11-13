package com.back.domain.post.post.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
import com.back.domain.post.postComment.entity.PostComment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// 나아가기 기능 검증용 테스트
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "user1") // 041-1 시큐리티 도입 후 : 인증된 사용자 요청 / 042-A : 샘플 회원 user1 로
class PostControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private PostService postService;

    @Autowired
    private MemberService memberService;

    private Member user1() {
        return memberService.findByUsername("user1").get();
    }

    @Test
    @DisplayName("029-A 글 삭제 : POST /posts/{id}/delete 후 목록으로 이동하고 글이 사라진다")
    void t1() throws Exception {
        Post post = postService.write(user1(), "삭제할 글", "삭제할 내용");

        mvc.perform(post("/posts/%d/delete".formatted(post.getId())).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));

        assertThat(postService.findById(post.getId())).isEmpty();
    }

    @Test
    @DisplayName("029-B 페이징 : 최신글이 먼저, 한 페이지에 10개씩")
    void t2() throws Exception {
        for (int i = 1; i <= 25; i++) postService.write(user1(), "페이징 글 " + i, "페이징 내용 " + i);

        Page<Post> page1 = postService.getPage(1);
        assertThat(page1.getContent()).hasSize(PostService.PAGE_SIZE);
        assertThat(page1.getContent().get(0).getTitle()).isEqualTo("페이징 글 25");

        mvc.perform(get("/posts").param("page", "2"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("페이징 글 15")))
                .andExpect(content().string(containsString("<strong>2</strong>")));
    }

    @Test
    @DisplayName("029-C 없는 글 : 상세/수정 폼/삭제 요청 모두 404")
    void t3() throws Exception {
        mvc.perform(get("/posts/999999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("요청하신 데이터를 찾을 수 없습니다.")));

        mvc.perform(get("/posts/999999/modify"))
                .andExpect(status().isNotFound());

        mvc.perform(post("/posts/999999/delete").with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("036-A 목록에 댓글 수 표시")
    void t4() throws Exception {
        Post post = postService.write(user1(), "댓글 수 글", "댓글 수 내용");
        postService.writeComment(post, user1(), "댓글 A");
        postService.writeComment(post, user1(), "댓글 B");
        postService.writeComment(post, user1(), "댓글 C");
        postService.writeComment(post, user1(), "댓글 D"); // 샘플 데이터(3개, 2개)와 겹치지 않게 4개

        mvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("[4]")));
    }

    @Test
    @DisplayName("036-B 없는 댓글 : 수정 폼/수정/삭제 요청 모두 404")
    void t5() throws Exception {
        Post post = postService.write(user1(), "댓글 404 글", "댓글 404 내용");
        String base = "/posts/%d/comments/999999".formatted(post.getId());

        mvc.perform(get(base + "/modify"))
                .andExpect(status().isNotFound());

        mvc.perform(post(base + "/modify").param("_method", "PUT").param("content", "수정 내용").with(csrf()))
                .andExpect(status().isNotFound());

        mvc.perform(post(base + "/delete").param("_method", "DELETE").with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("042-A 작성자 : 로그인 회원이 쓴 글·댓글에 작성자가 저장되고 상세페이지에 보인다")
    void t6() throws Exception {
        mvc.perform(post("/posts/write").with(csrf())
                        .param("title", "작성자 글")
                        .param("content", "작성자 내용"))
                .andExpect(status().is3xxRedirection());

        Post post = postService.getPage(1).getContent().get(0);
        assertThat(post.getTitle()).isEqualTo("작성자 글");
        assertThat(post.getAuthor().getUsername()).isEqualTo("user1");

        mvc.perform(post("/posts/%d/comments/write".formatted(post.getId())).with(csrf())
                        .param("content", "작성자 댓글"))
                .andExpect(status().is3xxRedirection());

        assertThat(post.getComments().getLast().getAuthor().getUsername()).isEqualTo("user1");

        mvc.perform(get("/posts/%d".formatted(post.getId())))
                .andExpect(content().string(containsString("작성자 : <span>user1</span>")));
    }

    @Test
    @WithAnonymousUser
    @DisplayName("042-B 비로그인 : 글쓰기·댓글쓰기는 로그인 페이지로, 상세페이지에는 로그인 안내")
    void t7() throws Exception {
        mvc.perform(get("/posts/write"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", endsWith("/members/login")));

        mvc.perform(post("/posts/1/comments/write").with(csrf()).param("content", "비로그인 댓글"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", endsWith("/members/login")));

        mvc.perform(get("/posts/1"))
                .andExpect(content().string(containsString("후 댓글을 작성할 수 있습니다.")));
    }

    @Test
    @DisplayName("043-A 권한 : 남의 글·댓글은 수정 폼/수정/삭제 모두 403 (요청자 user1, 작성자 user2)")
    void t8() throws Exception {
        Member user2 = memberService.findByUsername("user2").get();
        Post post = postService.write(user2, "user2 의 글", "user2 의 내용");
        int commentId = postService.writeComment(post, user2, "user2 의 댓글").getId();
        String postUrl = "/posts/%d".formatted(post.getId());
        String commentUrl = postUrl + "/comments/%d".formatted(commentId);

        mvc.perform(get(postUrl + "/modify")).andExpect(status().isForbidden());
        mvc.perform(post(postUrl + "/modify").param("_method", "PUT")
                        .param("title", "남의 글 수정").param("content", "남의 글 수정").with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("작성자만 글을 수정할 수 있습니다.")));
        mvc.perform(post(postUrl + "/delete").with(csrf())).andExpect(status().isForbidden());

        mvc.perform(get(commentUrl + "/modify")).andExpect(status().isForbidden());
        mvc.perform(post(commentUrl + "/modify").param("_method", "PUT")
                        .param("content", "남의 댓글 수정").with(csrf()))
                .andExpect(status().isForbidden());
        mvc.perform(post(commentUrl + "/delete").param("_method", "DELETE").with(csrf()))
                .andExpect(status().isForbidden());

        assertThat(post.getTitle()).isEqualTo("user2 의 글");
        assertThat(postService.findById(post.getId())).isPresent();
    }

    @Test
    @DisplayName("043-B 버튼 : 작성자 본인에게만 글·댓글 수정/삭제 버튼이 보인다")
    void t9() throws Exception {
        Member user2 = memberService.findByUsername("user2").get();

        Post myPost = postService.write(user1(), "내 글", "내 내용");
        postService.writeComment(myPost, user2, "남의 댓글");

        mvc.perform(get("/posts/%d".formatted(myPost.getId())))
                .andExpect(content().string(containsString(">글 수정</a>")))
                .andExpect(content().string(containsString(">글 삭제</button>")))
                .andExpect(content().string(not(containsString("comments/%d/modify".formatted(myPost.getComments().getFirst().getId())))));

        Post otherPost = postService.write(user2, "남의 글", "남의 내용");
        postService.writeComment(otherPost, user1(), "내 댓글");

        mvc.perform(get("/posts/%d".formatted(otherPost.getId())))
                .andExpect(content().string(not(containsString(">글 수정</a>"))))
                .andExpect(content().string(not(containsString(">글 삭제</button>"))))
                .andExpect(content().string(containsString("comments/%d/modify".formatted(otherPost.getComments().getFirst().getId()))));
    }

    @Test
    @DisplayName("044-A 글 추천 : 누르면 추천, 다시 누르면 추천 취소")
    void t10() throws Exception {
        Post post = postService.write(user1(), "추천 글", "추천 내용");
        String url = "/posts/%d".formatted(post.getId());

        mvc.perform(post(url + "/vote").with(csrf())).andExpect(redirectedUrl(url));
        assertThat(post.getVoters()).hasSize(1);
        assertThat(post.isVotedBy(user1())).isTrue();

        mvc.perform(get(url)).andExpect(content().string(containsString(">추천 취소</a>")));

        mvc.perform(post(url + "/vote").with(csrf())).andExpect(redirectedUrl(url));
        assertThat(post.getVoters()).isEmpty();
    }

    @Test
    @DisplayName("044-B 댓글 추천 : 누르면 추천, 다시 누르면 추천 취소")
    void t11() throws Exception {
        Post post = postService.write(user1(), "댓글 추천 글", "댓글 추천 내용");
        PostComment postComment = postService.writeComment(post, user1(), "추천할 댓글");
        String url = "/posts/%d/comments/%d/vote".formatted(post.getId(), postComment.getId());

        mvc.perform(post(url).with(csrf())).andExpect(redirectedUrl("/posts/" + post.getId()));
        assertThat(postComment.isVotedBy(user1())).isTrue();

        mvc.perform(post(url).with(csrf()));
        assertThat(postComment.getVoters()).isEmpty();
    }
}
