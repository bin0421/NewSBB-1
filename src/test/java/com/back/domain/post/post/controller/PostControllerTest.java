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
}
