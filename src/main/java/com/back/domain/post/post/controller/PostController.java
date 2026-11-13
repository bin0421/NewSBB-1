package com.back.domain.post.post.controller;

import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;


    @AllArgsConstructor
    @Getter
    public static class WriteForm {
        @NotBlank(message = "01-제목을 입력해주세요.")
        @Size(min = 2, max = 20, message = "02-제목은 2자 이상, 20자 이하로 입력가능합니다.")
        private String title;
        @NotBlank(message = "03-내용을 입력해주세요.")
        @Size(min = 2, max = 100, message = "04-내용은 2자 이상, 100자 이하로 입력가능합니다.")
        private String content;
    }

    @GetMapping("/posts/write")
    public String showWrite(@Valid WriteForm form) {
        return "post/post/write";
    }

    @PostMapping("/posts/doWrite")
    @Transactional
    public String write(
            @Valid WriteForm form,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult
                    .getFieldErrors()
                    .stream()
                    .map(fieldError -> (fieldError.getField() + "-" + fieldError.getDefaultMessage()).split("-", 3))
                    .map(field -> "<!--%s--><li data-error-field-name=\"%s\">%s</li>".formatted(field[1], field[0], field[2]))
                    .sorted()
                    .collect(Collectors.joining("\n"));

            model.addAttribute("errorMessage", errorMessage);

            return "post/post/write";
        }

        Post post = postService.write(form.getTitle(), form.getContent());

        model.addAttribute("post", post);

        return "post/post/writeDone";
    }
}
