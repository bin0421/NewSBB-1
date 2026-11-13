package com.back.domain.member.member.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// 나아가기 회원 기능 검증용 테스트
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MemberControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private MemberService memberService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("041-A 회원가입 성공 : 비밀번호는 암호화되어 저장된다")
    void t1() throws Exception {
        mvc.perform(post("/members/join").with(csrf())
                        .param("username", "newUser")
                        .param("password", "abcd1234")
                        .param("passwordConfirm", "abcd1234")
                        .param("email", "newUser@test.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/members/login"));

        Member member = memberService.findByUsername("newUser").get();
        assertThat(member.getPassword()).isNotEqualTo("abcd1234");
        assertThat(passwordEncoder.matches("abcd1234", member.getPassword())).isTrue();
    }

    @Test
    @DisplayName("041-A 회원가입 실패 : 비밀번호 확인 불일치, 중복 아이디")
    void t2() throws Exception {
        mvc.perform(post("/members/join").with(csrf())
                        .param("username", "user1") // 샘플 데이터에 이미 있음
                        .param("password", "abcd1234")
                        .param("passwordConfirm", "abcd9999")
                        .param("email", "other@test.com"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("비밀번호 확인이 일치하지 않습니다.")))
                .andExpect(content().string(containsString("이미 사용중인 아이디입니다.")));

        assertThat(memberService.existsByEmail("other@test.com")).isFalse();
    }

    @Test
    @DisplayName("041-B 로그인 성공 : 샘플 회원 user1 / 1234")
    void t3() throws Exception {
        mvc.perform(formLogin("/members/login").user("user1").password("1234"))
                .andExpect(authenticated().withUsername("user1"))
                .andExpect(redirectedUrl("/posts"));
    }

    @Test
    @DisplayName("041-B 로그인 실패 : 비밀번호 틀림")
    void t4() throws Exception {
        mvc.perform(formLogin("/members/login").user("user1").password("wrong"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/members/login?error"));
    }

    @Test
    @DisplayName("041-B 헤더 메뉴 : 로그인 상태면 아이디와 로그아웃, 아니면 로그인/회원가입")
    void t5() throws Exception {
        mvc.perform(get("/posts").with(user("user1")))
                .andExpect(content().string(containsString("user1님")))
                .andExpect(content().string(containsString("로그아웃")));

        mvc.perform(get("/posts"))
                .andExpect(content().string(containsString("로그인")))
                .andExpect(content().string(containsString("회원가입")));
    }
}
