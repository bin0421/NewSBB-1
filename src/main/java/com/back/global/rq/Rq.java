package com.back.global.rq;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

// 042-A 나아가기 : 현재 요청의 로그인 회원(actor)을 꺼내 주는 요청 단위 빈
@Component
@RequestScope
@RequiredArgsConstructor
public class Rq {
    private final MemberService memberService;
    private Member actor;
    private boolean actorLoaded;

    public Member getActor() {
        if (actorLoaded) return actor;
        actorLoaded = true;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) return null;

        actor = memberService.findByUsername(authentication.getName()).orElse(null);

        return actor;
    }

    public boolean isLogined() {
        return getActor() != null;
    }
}
