package com.back.domain.home.home.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// 047-A 나아가기 : 사이트 주소(/)로 들어오면 글 목록으로
@Controller
public class HomeController {
    @GetMapping("/")
    public String home() {
        return "redirect:/posts";
    }
}
