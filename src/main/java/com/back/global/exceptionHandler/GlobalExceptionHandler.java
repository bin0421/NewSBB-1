package com.back.global.exceptionHandler;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.security.Principal;
import java.util.NoSuchElementException;

// 029-C 나아가기 : 없는 데이터 요청은 500 대신 404
// 036-B 나아가기 : 글뿐 아니라 댓글(post.findCommentById(id).get())에도 그대로 적용된다
@ControllerAdvice
public class GlobalExceptionHandler {
    // 047-A 나아가기 : 없는 주소(정적 리소스도 못 찾은 경우)도 같은 404 페이지로
    @ExceptionHandler({NoSuchElementException.class, NoResourceFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNoSuchElement(HttpServletRequest request, Model model) {
        addCurrentUsername(request, model);

        return "global/notFound";
    }

    // 043-A 나아가기 : 작성자가 아닌 회원의 수정·삭제 요청은 403
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAccessDenied(AccessDeniedException e, HttpServletRequest request, Model model) {
        addCurrentUsername(request, model);
        model.addAttribute("message", e.getMessage());

        return "global/forbidden";
    }

    // 047-A 나아가기 : @ExceptionHandler 의 모델에는 @ControllerAdvice 의 @ModelAttribute(041-B)가 적용되지 않으므로
    // 레이아웃 헤더 메뉴용 currentUsername 을 직접 넣는다
    private void addCurrentUsername(HttpServletRequest request, Model model) {
        Principal principal = request.getUserPrincipal();
        model.addAttribute("currentUsername", principal == null ? null : principal.getName());
    }
}
