package com.back.global.markdown;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Component;

// 046-A 나아가기 : 마크다운 -> HTML. 템플릿에서 ${@markdown.toHtml(...)} 로 사용
@Component("markdown")
public class MarkdownService {
    private final Parser parser = Parser.builder().build();
    private final HtmlRenderer renderer = HtmlRenderer.builder()
            .escapeHtml(true)    // 입력에 섞인 <script> 같은 HTML 태그는 글자로 출력 (XSS 방지)
            .sanitizeUrls(true)  // javascript: 같은 위험한 링크 주소 제거
            .softbreak("<br>")   // 엔터 한 번도 줄바꿈으로 보이게
            .build();

    public String toHtml(String markdown) {
        if (markdown == null) return "";

        Node document = parser.parse(markdown);

        return renderer.render(document);
    }
}
