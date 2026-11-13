package com.back.domain.post.postComment.controller;

import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
import com.back.domain.post.postComment.entity.PostComment;
import com.back.global.rq.Rq;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class PostCommentController {
    private final PostService postService;
    private final Rq rq;


    public record ModifyForm(
            @NotBlank
            @Size(min = 2, max = 100)
            String content
    ) {
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
        postComment.checkActorCanModify(rq.getActor()); // 043-A 나아가기 : 작성자 확인

        model.addAttribute("post", post);
        model.addAttribute("postComment", postComment);

        return "post/postComment/modify";
    }

    @PutMapping("/posts/{postId}/comments/{id}/modify")
    @Transactional
    public String modify(
            @PathVariable int postId,
            @PathVariable int id,
            @Valid ModifyForm modifyForm
    ) {
        Post post = postService.findById(postId).get();
        PostComment postComment = post.findCommentById(id).get();
        postComment.checkActorCanModify(rq.getActor()); // 043-A 나아가기 : 작성자 확인

        postService.modifyComment(postComment, modifyForm.content);

        return "redirect:/posts/" + postId;
    }


    public record WriteForm(
            @NotBlank
            @Size(min = 2, max = 100)
            String content
    ) {
    }

    @PostMapping("/posts/{postId}/comments/write")
    @Transactional
    public String write(
            @PathVariable int postId,
            @Valid WriteForm writeForm
    ) {
        Post post = postService.findById(postId).get();

        postService.writeComment(post, rq.getActor(), writeForm.content); // 042-A : 작성자 저장

        return "redirect:/posts/" + postId;
    }


    @DeleteMapping("/posts/{postId}/comments/{id}/delete")
    @Transactional
    public String delete(
            @PathVariable int postId,
            @PathVariable int id
    ) {
        Post post = postService.findById(postId).get();
        PostComment postComment = post.findCommentById(id).get();
        postComment.checkActorCanDelete(rq.getActor()); // 043-A 나아가기 : 작성자 확인

        postService.deleteComment(post, postComment);

        return "redirect:/posts/" + postId;
    }


    // 044-B 나아가기 : 댓글 추천 / 추천 취소
    @PostMapping("/posts/{postId}/comments/{id}/vote")
    @Transactional
    public String vote(
            @PathVariable int postId,
            @PathVariable int id
    ) {
        Post post = postService.findById(postId).get();
        PostComment postComment = post.findCommentById(id).get();

        postService.toggleCommentVote(postComment, rq.getActor());

        return "redirect:/posts/" + postId;
    }
}
