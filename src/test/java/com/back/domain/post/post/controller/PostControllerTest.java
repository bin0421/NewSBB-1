package com.back.domain.post.post.controller;

import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// 나아가기 기능 검증용 테스트
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PostControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private PostService postService;

    @Test
    @DisplayName("029-A 글 삭제 : POST /posts/{id}/delete 후 목록으로 이동하고 글이 사라진다")
    void t1() throws Exception {
        Post post = postService.write("삭제할 글", "삭제할 내용");

        mvc.perform(post("/posts/%d/delete".formatted(post.getId())))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));

        assertThat(postService.findById(post.getId())).isEmpty();
    }

    @Test
    @DisplayName("029-B 페이징 : 최신글이 먼저, 한 페이지에 10개씩")
    void t2() throws Exception {
        for (int i = 1; i <= 25; i++) postService.write("페이징 글 " + i, "페이징 내용 " + i);

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

        mvc.perform(post("/posts/999999/delete"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("036-A 목록에 댓글 수 표시")
    void t4() throws Exception {
        Post post = postService.write("댓글 수 글", "댓글 수 내용");
        postService.writeComment(post, "댓글 A");
        postService.writeComment(post, "댓글 B");
        postService.writeComment(post, "댓글 C");
        postService.writeComment(post, "댓글 D"); // 샘플 데이터(3개, 2개)와 겹치지 않게 4개

        mvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("[4]")));
    }

    @Test
    @DisplayName("036-B 없는 댓글 : 수정 폼/수정/삭제 요청 모두 404")
    void t5() throws Exception {
        Post post = postService.write("댓글 404 글", "댓글 404 내용");
        String base = "/posts/%d/comments/999999".formatted(post.getId());

        mvc.perform(get(base + "/modify"))
                .andExpect(status().isNotFound());

        mvc.perform(post(base + "/modify").param("_method", "PUT").param("content", "수정 내용"))
                .andExpect(status().isNotFound());

        mvc.perform(post(base + "/delete").param("_method", "DELETE"))
                .andExpect(status().isNotFound());
    }
}
