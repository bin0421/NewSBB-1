package com.back.domain.member.member.controller;

import com.back.domain.member.member.service.MemberService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

// 041-A 나아가기 : 회원가입
@Controller
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;


    public record JoinForm(
            @NotBlank(message = "01-username-아이디를 입력해주세요.")
            @Size(min = 4, max = 20, message = "02-username-아이디는 4자 이상, 20자 이하로 입력가능합니다.")
            String username,
            @NotBlank(message = "03-password-비밀번호를 입력해주세요.")
            @Size(min = 4, max = 30, message = "04-password-비밀번호는 4자 이상, 30자 이하로 입력가능합니다.")
            String password,
            @NotBlank(message = "05-passwordConfirm-비밀번호 확인을 입력해주세요.")
            String passwordConfirm,
            @NotBlank(message = "07-email-이메일을 입력해주세요.")
            @Email(message = "08-email-이메일 형식이 올바르지 않습니다.")
            String email
    ) {
    }

    @GetMapping("/members/join")
    public String showJoin(@ModelAttribute("form") JoinForm form) {
        return "member/member/join";
    }

    @PostMapping("/members/join")
    @Transactional
    public String join(
            @ModelAttribute("form") @Valid JoinForm form,
            BindingResult bindingResult
    ) {
        // 어노테이션으로 표현하기 어려운 검사는 직접 하고, 같은 형식(순번-필드-메세지)으로 에러를 추가한다.
        if (form.password() != null && !form.password().equals(form.passwordConfirm()))
            bindingResult.rejectValue("passwordConfirm", "mismatch", "06-passwordConfirm-비밀번호 확인이 일치하지 않습니다.");

        if (memberService.existsByUsername(form.username()))
            bindingResult.rejectValue("username", "duplicated", "02-username-이미 사용중인 아이디입니다.");

        if (memberService.existsByEmail(form.email()))
            bindingResult.rejectValue("email", "duplicated", "08-email-이미 사용중인 이메일입니다.");

        if (bindingResult.hasErrors()) return "member/member/join";

        memberService.join(form.username(), form.password(), form.email());

        return "redirect:/members/login"; // 041-B : 가입 후 로그인 페이지로
    }


    // 041-B 나아가기 : 로그인 폼 (처리는 시큐리티 formLogin 이 담당)
    @GetMapping("/members/login")
    public String showLogin() {
        return "member/member/login";
    }
}
