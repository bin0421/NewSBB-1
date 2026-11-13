package com.back.global.exceptionHandler;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.NoSuchElementException;

// 029-C 나아가기 : 없는 데이터 요청은 500 대신 404
// 036-B 나아가기 : 글뿐 아니라 댓글(post.findCommentById(id).get())에도 그대로 적용된다
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNoSuchElement() {
        return "global/notFound";
    }

    // 043-A 나아가기 : 작성자가 아닌 회원의 수정·삭제 요청은 403
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAccessDenied(AccessDeniedException e, Model model) {
        model.addAttribute("message", e.getMessage());

        return "global/forbidden";
    }
}
