package com.back.domain.post.post.controller;

import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
import com.back.global.rq.Rq;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;
    private final Rq rq;


    public record ModifyForm(
            @NotBlank(message = "01-title-제목을 입력해주세요.")
            @Size(min = 2, max = 20, message = "02-title-제목은 2자 이상, 20자 이하로 입력가능합니다.")
            String title,
            @NotBlank(message = "03-content-내용을 입력해주세요.")
            @Size(min = 2, max = 100, message = "04-content-내용은 2자 이상, 100자 이하로 입력가능합니다.")
            String content
    ) {
    }

    @GetMapping("/posts/{id}/modify")
    @Transactional(readOnly = true)
    public String showModify(
            @PathVariable int id,
            Model model
    ) {
        Post post = postService.findById(id).get();
        post.checkActorCanModify(rq.getActor()); // 043-A 나아가기 : 작성자 확인

        model.addAttribute("post", post);
        model.addAttribute("form", new ModifyForm(post.getTitle(), post.getContent()));

        return "post/post/modify";
    }

    @PutMapping("/posts/{id}/modify")
    @Transactional
    public String modify(
            @PathVariable int id,
            @ModelAttribute("form") @Valid ModifyForm form,
            BindingResult bindingResult,
            Model model
    ) {
        Post post = postService.findById(id).get();
        post.checkActorCanModify(rq.getActor()); // 043-A 나아가기 : 작성자 확인
        model.addAttribute("post", post);

        if (bindingResult.hasErrors()) return "post/post/modify";

        postService.modify(post, form.title, form.content);

        return "redirect:/posts/" + post.getId();
    }


    public record WriteForm(
            @NotBlank(message = "01-title-제목을 입력해주세요.")
            @Size(min = 2, max = 20, message = "02-title-제목은 2자 이상, 20자 이하로 입력가능합니다.")
            String title,
            @NotBlank(message = "03-content-내용을 입력해주세요.")
            @Size(min = 2, max = 100, message = "04-content-내용은 2자 이상, 100자 이하로 입력가능합니다.")
            String content
    ) {
    }

    @GetMapping("/posts/write")
    public String showWrite(@ModelAttribute("form") WriteForm form) {
        return "post/post/write";
    }

    @PostMapping("/posts/write")
    @Transactional
    public String write(
            @ModelAttribute("form") @Valid WriteForm form,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) return "post/post/write";

        Post post = postService.write(rq.getActor(), form.title, form.content); // 042-A : 작성자 저장

        return "redirect:/posts/" + post.getId();
    }


    @GetMapping("/posts/{id}")
    @Transactional(readOnly = true)
    public String showDetail(
            @PathVariable int id,
            Model model
    ) {
        Post post = postService.findById(id).get();

        model.addAttribute("post", post);

        return "post/post/detail";
    }


    @GetMapping("/posts")
    @Transactional(readOnly = true)
    public String showList(
            Model model,
            @RequestParam(defaultValue = "1") int page // 029-B 나아가기 : 페이징
    ) {
        Page<Post> posts = postService.getPage(page);

        model.addAttribute("posts", posts);

        return "post/post/list";
    }

    @GetMapping("/posts/")
    public String redirectToList() {
        return "redirect:/posts";
    }


    // 029-A 나아가기 : 글 삭제
    @PostMapping("/posts/{id}/delete")
    @Transactional
    public String delete(@PathVariable int id) {
        Post post = postService.findById(id).get();
        post.checkActorCanDelete(rq.getActor()); // 043-A 나아가기 : 작성자 확인

        postService.delete(post);

        return "redirect:/posts";
    }


    // 044-A 나아가기 : 글 추천 / 추천 취소
    @PostMapping("/posts/{id}/vote")
    @Transactional
    public String vote(@PathVariable int id) {
        Post post = postService.findById(id).get();

        postService.toggleVote(post, rq.getActor());

        return "redirect:/posts/" + id;
    }
}
