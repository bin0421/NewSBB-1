package com.back.global.initData;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
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
    @Autowired
    @Lazy
    private BaseInitData self;
    private final PostService postService;
    private final MemberService memberService;

    @Bean
    ApplicationRunner baseInitDataApplicationRunner() {
        return args -> {
            self.work2(); // 042-A : 글의 작성자로 쓰기 위해 회원을 먼저 만든다
            self.work1();
        };
    }

    @Transactional
    public void work1() {
        if (postService.count() > 0) return;

        Member user1 = memberService.findByUsername("user1").get();
        Member user2 = memberService.findByUsername("user2").get();

        Post post1 = postService.write(user1, "제목 1", "내용 1");
        Post post2 = postService.write(user1, "제목 2", "내용 2");
        Post post3 = postService.write(user2, "제목 3", "내용 3");

        post1.addComment(user1, "댓글 1-1");
        post1.addComment(user2, "댓글 1-2");
        post1.addComment(user2, "댓글 1-3");
        post2.addComment(user1, "댓글 2-1");
        post2.addComment(user2, "댓글 2-2");
    }

    // 041-A 나아가기 : 샘플 회원 (비밀번호 1234)
    @Transactional
    public void work2() {
        if (memberService.count() > 0) return;

        memberService.join("user1", "1234", "user1@test.com");
        memberService.join("user2", "1234", "user2@test.com");
    }
}
