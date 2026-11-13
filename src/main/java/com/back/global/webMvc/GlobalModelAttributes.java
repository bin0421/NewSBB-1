package com.back.global.webMvc;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

// 041-B 나아가기 : 모든 컨트롤러의 템플릿에서 쓸 공통 변수 (016 의 @ModelAttribute 를 전역으로)
@ControllerAdvice
public class GlobalModelAttributes {
    @ModelAttribute("currentUsername")
    public String currentUsername(Principal principal) {
        return principal == null ? null : principal.getName();
    }
}
