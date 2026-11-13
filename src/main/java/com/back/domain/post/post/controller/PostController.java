package com.back.domain.post.post.controller;

import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    private String getWriteFormHtml() {
        return getWriteFormHtml("", "", "", "");
    }

    private String getWriteFormHtml(String errorFieldName, String errorMessage, String title, String content) {
        return """
                <div style="color: red;">%s</div>
                
                <form method="POST" action="doWrite">
                  <input type="text" name="title" placeholder="제목" value="%s" autofocus>
                  <br>
                  <textarea name="content" placeholder="내용">%s</textarea>
                  <br>
                  <input type="submit" value="작성">
                </form>
                
                <script>
                const errorFieldName = '%s';
                
                if ( errorFieldName.length > 0 )
                {
                    // 현재까지 나온 모든 폼 검색
                    const forms = document.querySelectorAll('form');
                    // 그 중에서 가장 마지막 폼 1개 찾기
                    const lastForm = forms[forms.length - 1];
                
                    lastForm[errorFieldName].focus();
                }
                </script>
                """.formatted(errorMessage, title, content, errorFieldName);
    }

    @GetMapping("/posts/write")
    @ResponseBody
    public String showWrite() {
        return getWriteFormHtml();
    }

    @PostMapping("/posts/doWrite")
    @ResponseBody
    @Transactional
    public String write(
            @RequestParam(defaultValue = "") String title,
            @RequestParam(defaultValue = "") String content
    ) {
        if (title.isBlank()) return getWriteFormHtml("title", "제목을 입력해주세요.", title, content);
        if (title.length() < 2) return getWriteFormHtml("title", "제목을 2자 이상 적어주세요.", title, content);
        if (title.length() > 20) return getWriteFormHtml("title", "제목은 최대 20자까지 입력가능합니다.", title, content);
        if (content.isBlank()) return getWriteFormHtml("content", "내용을 입력해주세요.", title, content);
        if (content.length() < 2) return getWriteFormHtml("content", "내용을 2자 이상 적어주세요.", title, content);
        if (content.length() > 100) return getWriteFormHtml("content", "내용은 최대 100자까지 입력가능합니다.", title, content);

        Post post = postService.write(title, content);

        return "%d번 글이 생성되었습니다.".formatted(post.getId());
    }
}