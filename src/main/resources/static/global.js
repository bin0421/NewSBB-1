document.addEventListener("DOMContentLoaded", () => {
    // value 속성이 있는 select 요소들을 찾아서 해당 값을 설정
    document.querySelectorAll("select[value]").forEach((select) => {
        const value = select.getAttribute("value");
        if (value) {
            select.value = value;
        }
    });

    // a 태그에 method와 onclick 속성이 같이 있는 경우 onclick 속성을 data-onclick으로 이동
    document.querySelectorAll("a[method][onclick]").forEach((anchor) => {
        const onclick = anchor.getAttribute("onclick");
        if (onclick) {
            anchor.removeAttribute("onclick");
            anchor.setAttribute("data-onclick", onclick);
        }
    });

    // method 속성이 DELETE, POST, PUT인 a 태그에 클릭 이벤트 등록
    document.querySelectorAll("a[method]").forEach((anchor) => {
        anchor.addEventListener("click", (e) => {
            e.preventDefault(); // 기본 링크 이동 방지

            // data-onclick 실행 (기존 onclick 대체)
            const onclickAfterCode = anchor.getAttribute("data-onclick");
            if (onclickAfterCode) {
                let onclickAfter = new Function(onclickAfterCode); // 문자열을 함수로 변환
                if (!onclickAfter()) return false; // false면 중단
            }

            // href에서 URL과 쿼리 파라미터 분리
            const href = anchor.getAttribute("href");
            const url = new URL(href, window.location.origin); // 절대경로로 변환
            const action = url.pathname; // 실제 form action으로 쓸 경로
            const method = anchor.getAttribute("method");

            const form = document.createElement("form");
            form.action = action;
            form.method = "POST";

            // CSRF 토큰
            const csrfToken =
                document.querySelector('meta[name="_csrf"]')?.getAttribute("content") ||
                "";

            const csrfInput = document.createElement("input");
            csrfInput.type = "hidden";
            csrfInput.name = "_csrf";
            csrfInput.value = csrfToken;
            form.appendChild(csrfInput);

            // HTTP method override
            const methodInput = document.createElement("input");
            methodInput.type = "hidden";
            methodInput.name = "_method";
            methodInput.value = method;
            form.appendChild(methodInput);

            // 쿼리 파라미터 → hidden input 변환
            for (const [key, value] of url.searchParams.entries()) {
                const paramInput = document.createElement("input");
                paramInput.type = "hidden";
                paramInput.name = key;
                paramInput.value = value;
                form.appendChild(paramInput);
            }

            document.body.appendChild(form);
            form.submit();

            // 잠시 뒤 폼 삭제
            setTimeout(() => {
                form.remove();
            });
        });
    });
});