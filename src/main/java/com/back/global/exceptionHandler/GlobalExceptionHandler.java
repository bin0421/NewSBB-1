package com.back.global.exceptionHandler;

import org.springframework.http.HttpStatus;
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
}
