package com.back.domain.post.postComment.controller;

import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
import com.back.domain.post.postComment.entity.PostComment;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class PostCommentController {
    private final PostService postService;


    @AllArgsConstructor
    @Getter
    public static class ModifyForm {
        @NotBlank
        @Size(min = 2, max = 100)
        private String content;
    }

    @GetMapping("/posts/{postId}/comments/{id}/modify")
    @Transactional(readOnly = true)
    public String showModify(
            @PathVariable int postId,
            @PathVariable int id,
            Model model
    ) {
        Post post = postService.findById(postId).get();
        PostComment postComment = post.findCommentById(id).get();

        model.addAttribute("post", post);
        model.addAttribute("postComment", postComment);

        return "post/postComment/modify";
    }

    @PostMapping("/posts/{postId}/comments/{id}/modify")
    @Transactional
    public String modify(
            @PathVariable int postId,
            @PathVariable int id,
            @Valid ModifyForm modifyForm
    ) {
        Post post = postService.findById(postId).get();
        PostComment postComment = post.findCommentById(id).get();

        postService.modifyComment(postComment, modifyForm.getContent());

        return "redirect:/posts/" + postId;
    }


    @AllArgsConstructor
    @Getter
    public static class WriteForm {
        @NotBlank
        @Size(min = 2, max = 100)
        private String content;
    }

    @PostMapping("/posts/{postId}/comments/write")
    @Transactional
    public String write(
            @PathVariable int postId,
            @Valid WriteForm writeForm
    ) {
        Post post = postService.findById(postId).get();

        postService.writeComment(post, writeForm.getContent());

        return "redirect:/posts/" + postId;
    }


    @GetMapping("/posts/{postId}/comments/{id}/delete")
    @Transactional
    public String delete(
            @PathVariable int postId,
            @PathVariable int id
    ) {
        Post post = postService.findById(postId).get();
        PostComment postComment = post.findCommentById(id).get();

        postService.deleteComment(post, postComment);

        return "redirect:/posts/" + postId;
    }
}