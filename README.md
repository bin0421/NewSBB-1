# NewSBB-1

스프링부트 복습용 SBB(Spring Boot Board) 단계별 구현 저장소.

- **복습하기** (`001-1`, `001-2` …) : 강사 저장소 [jhs512/p-14168-2](https://github.com/jhs512/p-14168-2)와 [강의 설명](https://www.slog.gg/p/14168)의 커밋 순서를 그대로 따라간다.
- **나아가기** (`001-A`, `001-B` …) : 참조에 없지만 개선·확장 가능한 기능을 단계마다 최대 2개까지 추가한다. ([위키독스 3장](https://wikidocs.net/book/7601), [추가 기능 예시](https://wikidocs.net/162814) 참고)
- 진행 기록·결정 사항 : [NewSBB-1.txt](NewSBB-1.txt)

## 개발 환경

| 항목 | 버전 |
|---|---|
| Java | 25 (Temurin) |
| Spring Boot | 4.1.2 |
| Gradle | 9.8.2 (Kotlin DSL) |
| DB | H2 |
| View | Thymeleaf |

## 개발 일지

### 001 프로젝트 세팅 `복습하기`

- **개선점 발견** : 아무것도 없는 상태. 스프링부트 애플리케이션이 실행될 뼈대가 필요하다.
- **추가한 기능** : Spring Initializr 구성과 같은 Gradle(Kotlin DSL) 프로젝트 생성.
  - 의존성 : Spring Web MVC, Spring Data JPA, Thymeleaf, H2 + H2 Console, Lombok, DevTools
  - 패키지 `com.back`, 메인 클래스 `BackApplication`, 테스트 `BackApplicationTests`
- **구현 설명**
  - `build.gradle.kts` : Spring Boot 4.1.2 플러그인과 Java 25 toolchain 지정. Spring Boot 4부터 `spring-boot-starter-web` 대신 `spring-boot-starter-webmvc`, H2 콘솔은 별도 모듈 `spring-boot-h2console`로 분리되었다.
  - `application.yaml` : 애플리케이션 이름(`back`)만 지정.
  - `.gitattributes` : `gradlew`는 LF, `*.bat`는 CRLF로 고정해 OS가 달라도 실행 스크립트가 깨지지 않게 한다.
  - `./gradlew build` 로 `contextLoads` 테스트 통과 확인.

### 002 샘플 데이터(글 3건) 자동 생성 `복습하기`

- **개선점 발견** : 화면을 만들기 전에 다룰 데이터가 없다. 서버를 켤 때마다 글을 직접 넣는 것은 번거롭다.
- **추가한 기능** : `Post` 엔티티/리포지터리/서비스를 만들고, 앱 시작 시 글이 없으면 샘플 글 3건을 자동 생성.
- **구현 설명**
  - `BaseEntity` (`global.jpa.entity`) : `@MappedSuperclass`로 모든 엔티티의 공통 필드(`id`, `createDate`, `modifyDate`)를 모은 부모 클래스. `@CreatedDate`/`@LastModifiedDate`는 `AuditingEntityListener`가 채워주며, 이를 위해 `BackApplication`에 `@EnableJpaAuditing`을 붙였다.
  - `Post` → `PostRepository`(`JpaRepository<Post, Integer>`) → `PostService`(`count`, `write`) 의 계층 구조. 패키지는 `domain.post.post.{entity,repository,service}` 처럼 도메인 기준으로 나눈다.
  - `BaseInitData` : `ApplicationRunner` 빈을 등록해 앱 구동 직후 실행. `count() > 0` 이면 건너뛰어 재시작해도 중복 생성되지 않는다.
  - 프로필 분리 : 기본 `dev`(파일 DB `./db_dev`), 테스트는 `@ActiveProfiles("test")`로 메모리 DB `db_test`. 둘 다 `MODE=MySQL`.
  - `application.yaml` : `ddl-auto: update`, SQL 출력/포맷/바인딩 값 TRACE 로그로 JPA가 실행하는 쿼리를 눈으로 확인할 수 있게 했다. `hikari.auto-commit: false`.
  - `.gitignore` 에 `db_dev.*.db` 파일 추가.

### 003 글 작성 폼 `복습하기`

> 003 회차는 "폼은 단순히 URL 생성기에 불과하다"는 설명만 있는 강의라 커밋이 없다.

- **개선점 발견** : 데이터는 있지만 사용자가 글을 입력할 화면이 없다.
- **추가한 기능** : `GET /posts/write` 요청에 글 작성 폼 HTML을 응답하는 `PostController`.
- **구현 설명**
  - `@Controller` + `@GetMapping("/posts/write")` + `@ResponseBody` : 템플릿 없이 메서드가 리턴한 문자열을 그대로 응답 본문으로 보낸다. 자바 텍스트 블록(`"""`)으로 HTML을 작성.
  - 폼은 "URL 생성기"일 뿐이다. 작성 버튼을 누르면 `action` 주소 뒤에 입력값이 쿼리스트링으로 붙어 `http://localhost:8080/posts/doWrite?title=안녕&content=반가워.` 로 이동한다(아직 `method` 미지정 → GET).
  - `/posts/doWrite` 를 처리하는 메서드는 아직 없으므로 전송하면 에러 페이지가 나온다. 005에서 구현한다.

### 004-1 폼 action 에서 프로토콜/도메인/포트 생략 `복습하기`

- **개선점 발견** : `action="http://localhost:8080/posts/doWrite"` 처럼 주소를 통째로 쓰면 도메인·포트가 바뀔 때마다 고쳐야 한다.
- **추가한 기능** : `action="/posts/doWrite"` 로 변경.
- **구현 설명** : `/` 로 시작하는 주소는 현재 페이지의 프로토콜·도메인·포트(`http://localhost:8080`)를 그대로 쓴다. 폼 주소와 action 주소의 앞부분이 같으면 그 부분은 생략할 수 있다.

### 004-2 폼 action 에서 겹치는 path 생략 `복습하기`

- **개선점 발견** : 폼 페이지(`/posts/write`)와 action(`/posts/doWrite`)은 `/posts/` 까지 같다.
- **추가한 기능** : `action="doWrite"` 로 변경.
- **구현 설명** : `/` 없이 시작하는 상대 경로는 현재 주소의 마지막 `/` 뒤만 바꾼다. `/posts/write` 에서 `doWrite` 는 `/posts/doWrite` 가 된다.

### 005 /posts/doWrite 액션 메서드로 글 작성 `복습하기`

- **개선점 발견** : 폼을 전송해도 받아서 처리하는 곳이 없다.
- **추가한 기능** : `GET /posts/doWrite` 를 처리해 글을 저장하고 "N번 글이 생성되었습니다." 를 응답.
- **구현 설명**
  - `@RequiredArgsConstructor` + `private final PostService` 로 생성자 주입.
  - 폼을 보여주는 메서드는 `showWrite`, 처리하는 메서드는 `write` 로 이름을 구분.
  - `@RequestParam(defaultValue = "")` : 쿼리스트링의 `title`, `content` 를 받는다. 값이 없어도 에러 대신 빈 문자열.
  - `@Transactional` 을 붙여 저장을 하나의 트랜잭션으로 처리(`auto-commit: false` 설정이므로 필요).

### 006 폼 전송은 GET 이 아니라 POST `복습하기`

- **개선점 발견** : GET으로 전송하면 입력값이 주소창에 그대로 남고, 새로고침·북마크로 글이 또 생성될 수 있다. GET은 조회용이다.
- **추가한 기능** : 폼에 `method="POST"`, 처리 메서드는 `@PostMapping("/posts/doWrite")`.
- **구현 설명** : POST는 입력값을 요청 본문(body)에 담아 보낸다. 생성·수정·삭제처럼 서버 상태를 바꾸는 요청은 POST를 쓴다. 이제 주소창에 `/posts/doWrite` 를 직접 입력(GET)하면 405 Method Not Allowed.

> 008 회차는 멱등성 설명만 있는 강의라 커밋이 없다. (GET/PUT/DELETE 는 여러 번 해도 결과가 같은 멱등, POST 는 비멱등. 실무에서 꼭 지킬 필요는 없고 참고만.)

### 007-1 title, content 필드 유효성 체크 `복습하기`

- **개선점 발견** : 제목이나 내용을 비워도 글이 저장된다.
- **추가한 기능** : 처리 메서드에서 `title.isBlank()`, `content.isBlank()` 면 안내 문구만 응답하고 저장하지 않는다.
- **구현 설명** : `isBlank()` 는 공백만 있는 문자열도 비었다고 판단한다(`isEmpty()` 와 차이). 강의 원본은 이 커밋에서 `PostController` 를 controller 패키지로 옮기지만, 이 저장소는 003부터 이미 `controller` 패키지에 두었다.

### 007-2 title 체크 실패시 메세지와 폼 함께 표시 `복습하기`

- **개선점 발견** : 에러 문구만 나오면 사용자가 뒤로가기를 눌러 다시 입력해야 한다.
- **추가한 기능** : 제목이 비었을 때 빨간 에러 메세지 + 작성 폼을 함께 응답.
- **구현 설명** : 응답 HTML 안에 `<div style="color:red;">` 메세지와 폼을 같이 넣었다.

### 007-3 content 체크 실패시 메세지와 폼 함께 표시 `복습하기`

- **개선점 발견** : 내용이 비었을 때는 여전히 문구만 나온다.
- **추가한 기능** : 내용 실패시에도 메세지 + 폼 응답.
- **구현 설명** : 007-2와 같은 방식. 결과적으로 같은 폼 HTML이 세 군데(`showWrite`, 제목 실패, 내용 실패)에 중복된다.

### 007-4 getWriteFormHtml 도입으로 폼 중복 제거 `복습하기`

- **개선점 발견** : 폼 HTML 중복 3곳. 폼을 고치려면 세 곳을 다 고쳐야 한다.
- **추가한 기능** : `getWriteFormHtml(errorMessage)` 메서드로 폼 생성 로직을 하나로 모음.
- **구현 설명** : 텍스트 블록의 `%s` 자리에 `.formatted(errorMessage)` 로 메세지를 끼워 넣는다. 인자 없는 `getWriteFormHtml()` 은 빈 메세지로 위임하는 오버로딩. 폼 기본값(`안녕`, `반가워.`)도 제거했다.

### 008-1 폼 재표시 때 기존 입력값 유지 `복습하기`

- **개선점 발견** : 내용만 비워서 실패해도 이미 입력한 제목까지 지워진다.
- **추가한 기능** : `getWriteFormHtml(errorMessage, title, content)` 로 입력값을 받아 `value="%s"`, `<textarea>%s</textarea>` 에 다시 채운다.
- **구현 설명** : 실패한 요청의 파라미터를 그대로 폼에 돌려준다. (아직 HTML 이스케이프를 하지 않으므로 입력값에 `"` 나 태그가 있으면 깨진다 → 014 타임리프 도입에서 해결)

### 008-2 autofocus 로 title 기본 포커스 `복습하기`

- **개선점 발견** : 폼을 열면 제목 칸을 직접 클릭해야 입력할 수 있다.
- **추가한 기능** : 제목 input 에 `autofocus` 속성.
- **구현 설명** : `autofocus` 는 페이지 로드시 해당 요소에 자동으로 커서를 둔다. 페이지에 하나만 두는 것이 원칙.

### 008-3 유효성 실패 필드 포커스 `복습하기`

- **개선점 발견** : 내용이 비어서 실패해도 커서는 autofocus 때문에 제목에 간다.
- **추가한 기능** : 실패한 필드 이름(`errorFieldName`)을 넘겨 자바스크립트로 그 필드에 포커스.
- **구현 설명** : `getWriteFormHtml(errorFieldName, errorMessage, title, content)`. 폼 뒤의 `<script>` 에서 `document.querySelectorAll('form')` 중 마지막 폼을 찾고 `lastForm[errorFieldName].focus()`. 폼 요소는 `form['name']` 으로 필드에 접근할 수 있다. 스크립트가 폼 뒤에 있어야 폼이 이미 만들어진 상태다.

### 009-1 제목 2~20자, 내용 2~100자 `복습하기`

- **개선점 발견** : 한 글자 제목이나 지나치게 긴 내용도 저장된다.
- **추가한 기능** : 제목 2~20자, 내용 2~100자 길이 체크를 `if` 문으로 추가.
- **구현 설명** : 체크 항목이 늘어날수록 `if` 문이 길어진다. 이 반복을 다음 커밋에서 어노테이션으로 바꾼다.

### 009-2 Validation 의존성, @Validated, @NotBlank, @Size `복습하기`

- **개선점 발견** : 손으로 쓴 `if` 유효성 체크가 6줄. 규칙을 선언만 하고 싶다.
- **추가한 기능** : `spring-boot-starter-validation` 의존성, 컨트롤러에 `@Validated`, 파라미터에 `@NotBlank`, `@Size(min, max)`.
- **구현 설명**
  - Bean Validation(jakarta.validation) 어노테이션으로 규칙을 선언하면 스프링이 메서드 호출 전에 검사한다.
  - `@RequestParam` 같은 단일 파라미터에 붙은 제약을 검사하려면 클래스에 `@Validated` 가 필요하다.
  - 실패하면 예외(`HandlerMethodValidationException`)가 발생해 에러 페이지가 뜬다. 폼 재표시는 일단 사라졌다 → 011에서 복구.

### 010 @ModelAttribute 로 입력값을 객체로 받기, @Valid `복습하기`

- **개선점 발견** : 파라미터마다 어노테이션이 3개씩 붙어 메서드 시그니처가 길다. 필드가 늘수록 더 길어진다.
- **추가한 기능** : 폼 입력값을 담는 `WriteForm` 클래스(제약 어노테이션 포함)를 만들고 `write(@Valid WriteForm form)` 으로 한 번에 받는다.
- **구현 설명**
  - 단순 타입이 아닌 객체 파라미터는 `@ModelAttribute` 가 생략된 것으로 처리되어, 요청 파라미터가 같은 이름의 필드(생성자 인자)에 바인딩된다. `@AllArgsConstructor` 의 생성자로 값이 들어간다.
  - 객체 내부 제약을 검사하려면 파라미터에 `@Valid` 를 붙인다. 이제 클래스의 `@Validated` 는 필요 없다.
  - ⚠️ **원본과 다른 점** : 강사 코드는 여기서 content 최대 길이가 20자로 바뀌지만, NewSBB 는 009의 의도대로 **100자**를 유지한다 (NewSBB-1.txt 결정 사항 9).

### 011-1 BindingResult 로 유효성 실패 예외 막기 `복습하기`

- **개선점 발견** : 유효성 실패시 예외가 던져져 400 에러 페이지가 뜬다. 실패 후 처리를 직접 하고 싶다.
- **추가한 기능** : `@Valid WriteForm form` 바로 뒤에 `BindingResult bindingResult` 파라미터. `hasErrors()` 면 빈 폼을 다시 보여준다.
- **구현 설명** : `BindingResult` 가 `@Valid` 파라미터 **바로 다음**에 있으면 스프링은 예외 대신 검사 결과를 여기에 담아 메서드를 정상 호출한다.

### 011-2 대표 에러 하나를 폼에 출력 `복습하기`

- **개선점 발견** : 실패해도 무엇이 잘못됐는지 알려주지 않고 입력값도 사라진다.
- **추가한 기능** : `bindingResult.getFieldError()` 로 대표 에러 1개를 꺼내 필드명·메세지·입력값과 함께 폼을 다시 그린다.
- **구현 설명** : `FieldError.getField()` 는 실패 필드명(`title`/`content`) → 008-3의 포커스 스크립트에 그대로 사용. `getDefaultMessage()` 는 어노테이션 기본 메세지("공백일 수 없습니다" 등).

### 011-3 유효성 어노테이션 커스텀 메세지 `복습하기`

- **개선점 발견** : 기본 메세지("크기가 2에서 20 사이여야 합니다")는 서비스 문구로 어색하다.
- **추가한 기능** : `@NotBlank(message = "제목을 입력해주세요.")`, `@Size(..., message = "...")` 처럼 메세지 지정.
- **구현 설명** : 각 제약 어노테이션의 `message` 속성이 `getDefaultMessage()` 결과가 된다. (content 는 결정 사항 9에 따라 "2자 이상, 100자 이하")

### 012-1 getFieldErrors() + 스트림으로 모든 에러 표시 `복습하기`

- **개선점 발견** : 제목·내용이 모두 틀려도 에러가 하나만 보여서, 고치고 다시 보내야 다음 에러를 알 수 있다.
- **추가한 기능** : `bindingResult.getFieldErrors()` 의 모든 메세지를 `<br>` 로 이어서 출력.
- **구현 설명** : `stream().map(FieldError::getDefaultMessage).collect(Collectors.joining("<br>"))`. 포커스 필드는 일단 `"title"` 로 고정. 단, `getFieldErrors()` 의 순서는 보장되지 않아 새로고침마다 메세지 순서가 바뀔 수 있다.

### 012-2 커스텀 메세지 앞 숫자로 순서 지정 `복습하기`

- **개선점 발견** : 에러 메세지 순서가 매번 달라진다.
- **추가한 기능** : 메세지 앞에 `01-`, `02-`, `03-`, `04-` 번호를 붙이고 `sorted()` 후 번호를 떼고 출력.
- **구현 설명** : 문자열 정렬로 순서를 고정하는 간단한 트릭. `message.split("-", 2)[1]` 로 첫 `-` 뒤만 사용(최대 2조각으로 나눠 본문의 `-` 는 보존).

### 013-1 커스텀 메세지 앞에 필드명 추가 `복습하기`

- **개선점 발견** : 여러 에러 중 첫 번째 에러 필드에 포커스를 주려면 메세지마다 어떤 필드인지 알아야 한다.
- **추가한 기능** : `fieldError.getField() + "-" + 메세지` 형태(`title-01-제목을 입력해주세요.`)로 출력.
- **구현 설명** : 다음 커밋에서 이 문자열을 `-` 로 쪼개 필드명/순번/메세지로 나눠 쓴다. (이 커밋에선 정렬이 잠시 빠짐)

### 013-2 메세지를 li 로 감싸고 data-error-field-name 추가 `복습하기`

- **개선점 발견** : `<br>` 로 이은 문자열은 프론트에서 어떤 필드의 에러인지 알아내기 어렵다.
- **추가한 기능** : 에러를 `<ul>` 안의 `<li data-error-field-name="title">메세지</li>` 로 출력. 순번은 `<!--01-->` 주석으로 앞에 둔다.
- **구현 설명** : `split("-", 3)` → `[필드명, 순번, 메세지]`. `data-*` 속성은 자바스크립트에서 `element.dataset.errorFieldName` 으로 읽을 수 있다(케밥 → 카멜 케이스).

### 013-3 정렬 후 첫 에러 필드 포커스(프론트) `복습하기`

- **개선점 발견** : 서버가 `errorFieldName` 을 따로 계산해서 넘기는 것은 중복이다. 이미 HTML 에 정보가 있다.
- **추가한 기능** : `<li>` 문자열을 `sorted()` 해(앞의 `<!--01-->` 주석 덕분에 순번대로 정렬) 출력하고, 스크립트가 첫 번째 `li` 의 `data-error-field-name` 을 읽어 포커스.
- **구현 설명** : `lastForm.previousElementSibling?.querySelector('li')?.dataset?.errorFieldName || ''`. 옵셔널 체이닝(`?.`)으로 에러가 없을 때도 안전. `getWriteFormHtml` 에서 `errorFieldName` 인자를 제거했다.

### 014-1 타임리프 도입, write.html `복습하기`

- **개선점 발견** : 자바 문자열 안에 HTML을 쓰니 에디터 지원(자동완성, 하이라이트)도 없고 `%s` 순서 맞추기도 번거롭다.
- **추가한 기능** : `templates/post/post/write.html` 템플릿을 만들고 `GET /posts/write` 는 뷰 이름 `"post/post/write"` 를 리턴.
- **구현 설명** : `@ResponseBody` 를 떼면 리턴 문자열은 응답 본문이 아니라 **뷰 이름**이 되고, 타임리프가 `classpath:/templates/{뷰이름}.html` 을 찾아 렌더링한다. 템플릿 경로는 패키지 구조(`domain/post/post`)와 맞췄다. POST 쪽은 아직 문자열 HTML.

### 014-2 getWriteFormHtml 제거, writeDone.html `복습하기`

- **개선점 발견** : POST 처리 결과도 여전히 자바 문자열 HTML.
- **추가한 기능** : `getWriteFormHtml` 삭제. 실패시 `write.html`, 성공시 `writeDone.html` 렌더링.
- **구현 설명** : 액션 메서드에 `Model model` 을 받아 `model.addAttribute("post", post)` 로 템플릿에 값 전달. 템플릿에서는 인라인 표현식 `[[${post.id}]]` 로 출력. 실패 메세지·입력값은 아직 템플릿에 연결하지 않았다.

### 014-3 th:text 는 이스케이프 `복습하기`

- **개선점 발견** : 실패해도 에러 메세지가 보이지 않는다.
- **추가한 기능** : `model.addAttribute("errorMessage", errorMessage)` + `<ul th:text="${errorMessage}">`.
- **구현 설명** : `th:text` 는 값을 HTML 이스케이프해 태그 내용으로 넣는다. 그래서 `<li ...>` 가 태그로 렌더링되지 않고 문자 그대로 화면에 보인다(XSS 방지가 기본).

### 014-4 th:utext 는 이스케이프 안 함 `복습하기`

- **개선점 발견** : `li` 태그가 글자로 보인다.
- **추가한 기능** : `th:utext="${errorMessage}"` 로 교체.
- **구현 설명** : `utext`(unescaped text)는 HTML 을 그대로 삽입해 렌더링한다. 서버가 만든 신뢰할 수 있는 HTML 에만 쓰고, 사용자 입력에는 쓰지 않는다.

### 014-5 writeForm.title/content 로 값 유지 (GET /posts/write 실패) `복습하기`

- **개선점 발견** : 실패 후 입력값이 사라진다.
- **추가한 기능** : `th:value="${writeForm.title}"`, `th:text="${writeForm.content}"`.
- **구현 설명** : `@Valid WriteForm form` 처럼 받은 객체는 **클래스명 첫 글자 소문자(`writeForm`)** 이름으로 자동으로 모델에 담긴다. 그래서 POST 실패 화면은 잘 되지만, `GET /posts/write` 에는 `writeForm` 이 없어 `null.title` 접근으로 **에러**가 난다(의도된 실패 단계).

### 014-6 writeForm?.title 사용 (GET 성공) `복습하기`

- **개선점 발견** : GET 에서 `writeForm` 이 null 이라 터진다.
- **추가한 기능** : `${writeForm?.title}`, `${writeForm?.content}`.
- **구현 설명** : SpEL 안전 탐색 연산자 `?.` 는 앞이 null 이면 null 을 반환해 예외가 나지 않는다.

### 014-7 writeForm.title 복귀, showWrite 에 @Valid WriteForm 추가 `복습하기`

- **개선점 발견** : 템플릿 곳곳에 `?.` 를 쓰는 대신, GET 에도 `writeForm` 을 넣어주면 된다.
- **추가한 기능** : `showWrite(@Valid WriteForm form)` 로 파라미터 추가, 템플릿은 다시 `writeForm.title`.
- **구현 설명** : 파라미터로 받기만 해도 모델에 `writeForm` 이 담긴다. 그러나 `@Valid` 때문에 빈 폼을 여는 순간 유효성 검사가 실패해 400 에러(BindingResult 없음) → 의도된 실패 단계.

### 014-8 @Valid 제거로 GET 성공 `복습하기`

- **개선점 발견** : 폼을 처음 보여줄 때는 검사할 필요가 없다.
- **추가한 기능** : `showWrite(WriteForm form)` — `@Valid` 제거.
- **구현 설명** : 검사 없이 바인딩만 해서 빈 `writeForm` 이 모델에 담기므로 GET 이 정상 작동. 이렇게 하면 `/posts/write?title=abc` 처럼 초기값을 쿼리로 넣을 수도 있다.

> 017 회차는 커밋이 없다.

### 015 타임리프 캐시 끄기, @ModelAttribute("form") `복습하기`

- **개선점 발견** : 템플릿을 고칠 때마다 서버를 재시작해야 반영된다. 또 `writeForm` 이라는 모델 이름이 길고 클래스명에 묶여 있다.
- **추가한 기능**
  - `application-dev.yaml` : `spring.thymeleaf.cache: false`, `prefix: file:src/main/resources/templates/`.
  - 파라미터에 `@ModelAttribute("form")` 을 붙여 모델 이름을 `form` 으로 지정.
- **구현 설명** : prefix 를 `file:` 소스 폴더로 바꾸면 빌드 결과물(classpath)이 아니라 원본 파일을 바로 읽으므로, 캐시를 끈 상태에서 새로고침만으로 수정이 반영된다(dev 프로필 한정).

### 016 @ModelAttribute("siteName") 공통 변수 `복습하기`

- **개선점 발견** : 모든 페이지에 공통으로 보일 사이트 이름을 액션 메서드마다 `model.addAttribute` 하기는 번거롭다.
- **추가한 기능** : 컨트롤러에 `@ModelAttribute("siteName")` 메서드를 두고, `write.html`/`writeDone.html` 상단에 `<h1 th:text="${siteName}">`. 템플릿의 `writeForm` 은 `form` 으로 변경.
- **구현 설명** : 메서드에 붙은 `@ModelAttribute` 는 **그 컨트롤러의 모든 액션 메서드 실행 전에** 호출되어 리턴값이 모델에 담긴다. 모든 컨트롤러에 적용하려면 `@ControllerAdvice` 클래스에 두면 된다.

### 017 th:block `복습하기`

- **개선점 발견** : `<ul th:utext>` 는 `ul` 의 내용 전체를 덮어써서, `ul` 안에 다른 요소를 함께 둘 수 없다.
- **추가한 기능** : `<ul>` 안에 `<th:block th:utext="${errorMessage}"></th:block>`.
- **구현 설명** : `th:block` 은 렌더링 후 자기 자신은 사라지고 결과(자식)만 남는 타임리프 전용 가상 태그. 반복·조건 처리에 래퍼 태그 없이 쓸 때 유용하다.

### 018 label for/id `복습하기`

- **개선점 발견** : 입력칸에 이름표가 없고, placeholder 는 입력을 시작하면 사라진다.
- **추가한 기능** : 각 필드를 `<div>` 로 감싸고 `<label for="title">제목</label>` + `id="title"` 입력칸.
- **구현 설명** : `label` 의 `for` 와 input 의 `id` 를 맞추면 라벨 클릭시 입력칸에 포커스되고 스크린리더 접근성이 좋아진다. 정석은 for/id 연결이지만, label 안에 input 을 넣는 방식도 쓰인다.

### 019-1 th:object `복습하기`

- **개선점 발견** : 필드마다 `form.` 접두어를 반복한다.
- **추가한 기능** : `<form th:object="${form}">` 안에서 `*{title}`, `*{content}` 선택 변수식 사용.
- **구현 설명** : `th:object` 로 기준 객체를 정하면 그 엘리먼트 안에서 `*{필드}` 는 `${form.필드}` 와 같다.

### 019-2 th:field `복습하기`

- **개선점 발견** : `name`, `id`, `value` 를 필드마다 직접 맞춰 써야 한다.
- **추가한 기능** : `th:field="*{title}"`, `th:field="*{content}"` 로 교체.
- **구현 설명** : `th:field` 는 `name="title" id="title" value="..."` (textarea 는 내용)를 자동 생성한다. label 의 `for="title"` 과 자동 id 가 일치한다.

### 020-1 #fields 로 에러 메세지 출력 `복습하기`

- **개선점 발견** : 컨트롤러가 에러 HTML(`<li>`)까지 만들어 넘긴다. 화면 표현은 템플릿의 몫이다.
- **추가한 기능** : 컨트롤러는 `if (bindingResult.hasErrors()) return "post/post/write";` 한 줄로 축소. 템플릿 `th:object` 안에서 `#fields.hasAnyErrors()`, `th:each="err : ${#fields.allErrors()}"` 로 출력.
- **구현 설명** : `#fields` 는 `th:object` 객체의 BindingResult 에 접근하는 타임리프 유틸리티. 에러 목록 `ul` 은 `form` 안으로 옮겼다.

### 020-2 에러 메세지 정렬, focus 스크립트 일단 제거 `복습하기`

- **개선점 발견** : 템플릿 출력도 순서가 매번 다르고 `01-` 번호가 그대로 보인다.
- **추가한 기능** : `#fields.allErrors().stream().sorted()` 로 정렬, `err.split('-', 2)[1]` 로 번호 제거.
- **구현 설명** : 타임리프 표현식(SpEL) 안에서도 자바 메서드·스트림을 호출할 수 있다. HTML 구조가 바뀌어 기존 포커스 스크립트가 맞지 않으므로 일단 제거(022에서 복원).

### 021-1 커스텀 메세지에 필드이름 추가 `복습하기`

- **개선점 발견** : 템플릿에서는 `getField()` 를 따로 꺼내기 번거롭다. 포커스를 복원하려면 메세지에 필드명이 있어야 한다.
- **추가한 기능** : 메세지를 `01-title-제목을 입력해주세요.` 형식(`순번-필드명-메세지`)으로 변경.
- **구현 설명** : 013 에서 코드로 붙이던 필드명을 메세지 자체에 넣었다. 정렬은 맨 앞 순번 기준으로 그대로 동작.

### 021-2 data-error-field-name 하드코딩 `복습하기`

- **개선점 발견** : `li` 에 어떤 필드의 에러인지 표시가 없다.
- **추가한 기능** : `data-error-field-name="title"` (일단 하드코딩), 메세지는 `err.split('-', 3)[2]`.
- **구현 설명** : 동작 위치를 먼저 확인하기 위한 중간 단계.

### 021-3 th: 없이 split (실패) `복습하기`

- **개선점 발견** : 필드명을 하드코딩이 아닌 메세지에서 꺼내야 한다.
- **추가한 기능** : `data-error-field-name="${err.split('-', 3)[1]}"`.
- **구현 설명** : 의도된 실패 단계. `th:` 접두어가 없는 일반 속성은 타임리프가 처리하지 않아 `${...}` 문자열이 그대로 출력된다.

### 021-4 th: 붙여 성공 `복습하기`

- **개선점 발견** : 021-3 에서 표현식이 평가되지 않았다.
- **추가한 기능** : `th:data-error-field-name="${err.split('-', 3)[1]}"`.
- **구현 설명** : `th:아무속성` 은 표현식 결과로 그 속성을 만든다. `data-*` 같은 커스텀 속성에도 사용 가능.

### 021-5 th:with 로 변수화 `복습하기`

- **개선점 발견** : 같은 `split` 을 두 번 호출하고 가독성이 떨어진다.
- **추가한 기능** : `th:with="errStrs = ${err.split('-', 3)}, errFieldName = ${errStrs[1]}, errMsg = ${errStrs[2]}"`.
- **구현 설명** : `th:with` 는 해당 엘리먼트 범위의 지역 변수를 선언한다. 앞에서 선언한 변수를 뒤 변수에서 바로 쓸 수 있다.

### 022-1 focus 스크립트 복원, ul 은 form 의 자식 `복습하기`

- **개선점 발견** : 020-2 에서 지운 포커스 기능이 없다.
- **추가한 기능** : 폼 뒤에 스크립트 복원. 에러 `ul` 이 이제 form 안에 있으므로 `lastForm.querySelector('ul > li')` 로 첫 에러를 찾는다.
- **구현 설명** : 013-3 의 `previousElementSibling` 대신 form 내부 탐색으로 변경.

### 022-2 ul 아래 스크립트는 실패 `복습하기`

- **개선점 발견** : 에러 목록과 스크립트를 한 덩어리로 묶어 재사용하고 싶다 → 스크립트를 `ul` 바로 아래(form 안)로 옮겨 본다.
- **추가한 기능** : 스크립트 위치 이동.
- **구현 설명** : 의도된 실패 단계. 스크립트가 실행되는 시점에는 아래쪽 input 들이 아직 파싱되지 않아 `lastForm[errorFieldName]` 이 없다.

### 022-3 setTimeout 1초 지연 `복습하기`

- **개선점 발견** : 폼이 완성되기 전에 포커스를 시도한다.
- **추가한 기능** : `setTimeout(() => lastForm[errorFieldName].focus(), 1000)`.
- **구현 설명** : 포커스 코드만 나중으로 미루면 그 사이 브라우저가 나머지 HTML 을 다 파싱한다.

### 022-4 0.001초 지연 `복습하기`

- **개선점 발견** : 1초는 사용자 눈에 보일 만큼 느리다.
- **추가한 기능** : 지연을 `1`ms 로.
- **구현 설명** : 중요한 건 시간이 아니라 "현재 실행 중인 스크립트(파싱) 이후"로 미루는 것. 이벤트 루프상 타이머 콜백은 현재 작업이 끝난 뒤 실행된다.

### 022-5 지연 시간 생략 `복습하기`

- **개선점 발견** : `1` 이라는 숫자도 불필요하다.
- **추가한 기능** : `setTimeout(() => ...)` 로 지연 인자 생략.
- **구현 설명** : 생략하면 0 으로 처리되어 가능한 빨리, 하지만 현재 작업 이후에 실행된다.

### 022-6 프래그먼트로 등록 `복습하기`

- **개선점 발견** : 에러 목록 + 포커스 스크립트는 앞으로 모든 폼에서 쓸 공통 부품이다.
- **추가한 기능** : `templates/form_errors.html` 에 `th:fragment="formErrorsFragment"` 로 등록, `write.html` 은 `<div th:replace="~{form_errors :: formErrorsFragment}">`.
- **구현 설명** : `th:replace` 는 해당 태그를 프래그먼트로 통째로 교체한다. 프래그먼트 안의 `#fields` 는 삽입된 위치(`th:object` form 안)의 문맥을 그대로 쓴다.

### 023-1 POST /posts/doWrite -> POST /posts/write `복습하기`

- **개선점 발견** : `doWrite` 의 "do" 는 POST 라는 메서드가 이미 말해 준다.
- **추가한 기능** : `@PostMapping("/posts/write")`, 폼 `action="write"`.
- **구현 설명** : URL 이 같아도 HTTP 메서드가 다르면 스프링이 다른 액션 메서드로 구분한다. `GET /posts/write` = 폼 보여주기, `POST /posts/write` = 처리.

### 023-2 현재 페이지 action 생략 `복습하기`

- **개선점 발견** : action 이 현재 페이지 주소와 같다.
- **추가한 기능** : `action` 속성 삭제.
- **구현 설명** : action 이 없으면 폼은 현재 주소로 전송된다.

### 024 meta refresh 3초 `복습하기`

- **개선점 발견** : 글 작성 후 완료 문구만 덩그러니 남는다. 이 화면에서 새로고침하면 POST 가 다시 전송돼 글이 중복 생성된다.
- **추가한 기능** : `writeDone.html` 에 `<meta http-equiv="refresh" content="3;url=/posts/write">` + "3초 후에 이동됩니다."
- **구현 설명** : 브라우저가 3초 뒤 지정 URL 로 GET 이동한다.

### 025-1 meta refresh 0초 `복습하기`

- **개선점 발견** : 기다릴 필요가 없다.
- **추가한 기능** : `content="0;url=/posts/write"` 한 줄만 남김.
- **구현 설명** : 즉시 이동하지만, 여전히 "HTML 응답 → 브라우저가 다시 요청" 의 2단계이고 HTML 을 내려줘야 한다.

### 025-2 액션메서드에서 302 응답 `복습하기`

- **개선점 발견** : 이동만 할 거라면 HTML 페이지가 필요 없다.
- **추가한 기능** : `return "redirect:/posts/write";`, `writeDone.html` 과 `Model` 파라미터 삭제.
- **구현 설명** : `redirect:` 접두어는 302 응답 + `Location` 헤더를 보낸다. POST 후 Redirect 후 GET 하는 **PRG 패턴**으로, 새로고침해도 POST 가 다시 전송되지 않는다.

### 026-1 글 상세페이지, 작성 후 상세로 이동 `복습하기`

- **개선점 발견** : 작성한 글을 볼 방법이 없다. 작성 후 다시 빈 폼으로 돌아간다.
- **추가한 기능** : `GET /posts/{id}` → `detail.html`, 작성 후 `redirect:/posts/{새 글 id}`. `PostService.findById`.
- **구현 설명** : `@PathVariable int id` 로 URL 경로의 값을 받는다. 조회 전용 메서드에는 `@Transactional(readOnly = true)` 로 변경 감지 등 불필요한 작업을 줄인다. `findById` 는 `Optional` 을 리턴하므로 일단 `.get()`.

### 026-2 white-space: pre-line `복습하기`

- **개선점 발견** : 내용에 입력한 줄바꿈이 상세페이지에서 한 줄로 붙어 보인다.
- **추가한 기능** : 내용 `div` 에 `style="white-space: pre-line;"`.
- **구현 설명** : HTML 은 줄바꿈 문자를 공백으로 취급한다. `pre-line` 은 줄바꿈은 살리고 연속 공백은 하나로 합친다. (`<br>` 로 치환해 `utext` 를 쓰는 것보다 XSS 에 안전)

### 026-3 상세에 글쓰기/목록/수정 링크 `복습하기`

- **개선점 발견** : 상세페이지에서 다른 곳으로 갈 길이 없다.
- **추가한 기능** : `<a href=".">글 목록</a>`, `<a href="write">글 쓰기</a>`, `<a th:href="|${post.id}/modify|">글 수정</a>`.
- **구현 설명** : `/posts/1` 기준 상대경로 `.` → `/posts/`, `write` → `/posts/write`. `|...|` 는 타임리프 리터럴 치환(문자열 템플릿).

### 026-4 수정 링크 타임리프스럽게 `복습하기`

- **개선점 발견** : URL 을 문자열 치환으로 만들면 인코딩·컨텍스트 경로 처리가 안 된다.
- **추가한 기능** : `th:href="@{{id}/modify(id=${post.id})}"`.
- **구현 설명** : `@{...}` 링크 표현식은 `{id}` 자리에 괄호 안 파라미터를 URL 인코딩해 넣는다. `/` 로 시작하면 컨텍스트 경로도 자동으로 붙는다.

### 026-5 글 목록 @ResponseBody List (JSON) `복습하기`

- **개선점 발견** : 상세의 "글 목록" 링크가 갈 곳이 없다.
- **추가한 기능** : `GET /posts` 가 `List<Post>` 를 `@ResponseBody` 로 리턴, `GET /posts/` 는 `/posts` 로 리다이렉트. `PostService.findAll`.
- **구현 설명** : `@ResponseBody` 메서드가 객체를 리턴하면 `HttpMessageConverter`(Jackson)가 JSON 으로 변환한다. 이 JSON 을 프론트(fetch)에서 받아 그리면 클라이언트 사이드 렌더링이 된다. 끝 `/` 유무가 다른 URL 이므로 리다이렉트로 통일.

### 027-1 필드별 getter 접근 레벨 조절 `복습하기`

- **개선점 발견** : JSON 에 어떤 필드가 나가는지 조절할 수 있을까?
- **추가한 기능** : `createDate` 에 `@Getter(PRIVATE)`, `modifyDate` 에 `@Getter(PROTECTED)`.
- **구현 설명** : Jackson 은 public 필드 또는 public getter 가 있는 속성만 직렬화한다. 그래서 두 날짜가 JSON 에서 빠진다. `protected` 는 같은 패키지+자식만, `public` 은 어디서나 접근 가능.

### 027-2 원상복구 `복습하기`

- **개선점 발견** : 확인용 실험이었고 날짜는 화면에 필요하다.
- **추가한 기능** : 027-1 의 접근 레벨 지정 제거.
- **구현 설명** : 다시 클래스 레벨 `@Getter` 로 모두 public getter.

### 028-1 list.html `복습하기`

- **개선점 발견** : 목록이 JSON 이라 사람이 보기 불편하다.
- **추가한 기능** : `GET /posts` 가 `list.html` 렌더링. `th:each` 로 글 번호·제목(상세 링크)·작성일 출력, 글쓰기 링크.
- **구현 설명** : `#temporals.format(post.createDate, 'yy.MM.dd HH:mm')` 로 `LocalDateTime` 포맷. 링크는 `@{/posts/{id}(id=${post.id})}`.

### 028-2 상세에 작성/수정 날짜 `복습하기`

- **개선점 발견** : 상세페이지에서 언제 쓰고 고쳤는지 알 수 없다.
- **추가한 기능** : 상세에 작성일, 수정일 표시.
- **구현 설명** : `BaseEntity` 의 auditing 필드(`@CreatedDate`, `@LastModifiedDate`)를 `#temporals.format` 으로 출력.

### 029-1 modify.html `복습하기`

- **개선점 발견** : 상세의 "글 수정" 링크(`/posts/{id}/modify`)가 갈 곳이 없다.
- **추가한 기능** : `ModifyForm`(WriteForm 과 같은 제약 + `@Setter`), `GET /posts/{id}/modify` → `modify.html`. 기존 제목·내용을 폼에 채워서 보여준다.
- **구현 설명** : `@ModelAttribute("form") ModifyForm form` 으로 빈 폼 객체를 모델에 올린 뒤 `form.setTitle(post.getTitle())` 처럼 값을 채운다. 템플릿은 write.html 과 같은 구조에 글 ID·작성일·수정일 표시, 폼 에러 프래그먼트 재사용. (content 최대 100자 — 결정 사항 9)

### 029-2 수정 처리 후 상세로 이동 `복습하기`

- **개선점 발견** : 수정 폼을 전송해도 처리하지 않는다.
- **추가한 기능** : `POST /posts/{id}/modify` — 유효성 실패시 수정 폼 재표시, 성공시 `Post.modify()` 후 상세로 리다이렉트.
- **구현 설명** : `postService.modify(post, title, content)` 는 엔티티 필드만 바꾸고 `save` 를 호출하지 않는다. `@Transactional` 안에서 조회한 영속 엔티티는 트랜잭션 커밋 때 **변경 감지(dirty checking)** 로 UPDATE 가 자동 실행된다. 이때 `@LastModifiedDate` 로 수정일도 갱신된다.

### 029-A 글 삭제 `나아가기`

- **개선점 발견** : 글 작성·조회·수정은 되지만 삭제가 없다. 강사 코드에는 댓글 삭제(034)만 있고 글 삭제는 끝까지 없다.
- **추가한 기능** : 상세페이지에 "글 삭제" 버튼, `POST /posts/{id}/delete` → 삭제 후 목록으로 리다이렉트.
- **구현 설명**
  - 삭제는 서버 상태를 바꾸므로 링크(GET)가 아니라 `<form method="POST">` 로 보낸다. `onsubmit="return confirm(...)"` 으로 취소하면 전송되지 않는다.
  - `th:action="@{/posts/{id}/delete(id=${post.id})}"` — 링크 표현식으로 주소를 만든다(시큐리티 도입 후 CSRF 토큰 자동 삽입에도 유리).
  - `PostService.delete(post)` → `postRepository.delete(post)`.
  - 테스트 `PostControllerTest.t1` : MockMvc 로 삭제 요청 후 리다이렉트 주소와 실제 삭제 여부 확인.

### 029-B 목록 최신순 + 페이징 `나아가기`

- **개선점 발견** : 목록이 오래된 글부터 나오고, 글이 많아지면 한 화면에 전부 출력된다. ([위키독스 3-09 페이징](https://wikidocs.net/161186) 참고)
- **추가한 기능** : `GET /posts?page=N` — 최신글(id 내림차순) 먼저, 한 페이지 10개. 목록 아래 "이전 / 페이지 번호 / 다음" 링크(현재 페이지 앞뒤 4페이지까지, 현재 페이지는 굵게).
- **구현 설명**
  - `PostService.getPage(page)` : `PageRequest.of(page - 1, 10, Sort.by(Sort.Order.desc("id")))` 로 `postRepository.findAll(pageable)` 호출. JPA 가 `LIMIT/OFFSET` 쿼리와 전체 개수 `COUNT` 쿼리를 함께 실행한다. 사용자에게 보이는 page 는 1부터, 스프링 Data 는 0부터라서 변환한다.
  - 리턴 타입 `Page<Post>` 는 `Iterable` 이라 기존 `th:each="post : ${posts}"` 를 그대로 쓴다. `totalPages`, `number`, `hasPrevious()`, `hasNext()` 로 페이지 링크를 그린다.
  - `th:with` 로 `currentPage/startPage/endPage` 계산, `T(java.lang.Math)` 로 정적 메서드 호출, `#numbers.sequence(start, end)` 로 번호 반복.
  - 테스트 `t2` : 글 25개 생성 후 1페이지 첫 글이 최신글인지, 2페이지에 해당 글과 현재 페이지 표시가 있는지 확인.

### 029-C 없는 글 404 처리 `나아가기`

- **개선점 발견** : `/posts/999` 처럼 없는 글에 접근하면 `findById(id).get()` 에서 `NoSuchElementException` 이 터져 **500 서버 에러**가 난다. 서버 잘못이 아니라 "없는 자원"이므로 404 가 맞다.
- **추가한 기능** : 전역 예외 처리기 `GlobalExceptionHandler` 가 `NoSuchElementException` 을 404 + "요청하신 데이터를 찾을 수 없습니다." 페이지(`templates/global/notFound.html`)로 바꾼다.
- **구현 설명**
  - `@ControllerAdvice` 클래스의 `@ExceptionHandler(NoSuchElementException.class)` 는 **모든 컨트롤러**에서 발생한 해당 예외를 가로챈다. `@ResponseStatus(HttpStatus.NOT_FOUND)` 로 상태 코드 지정, 리턴값은 뷰 이름.
  - 컨트롤러마다 `orElseThrow` 로 고치지 않고 한 곳에서 처리하므로, 앞으로 추가될 댓글 등 다른 `findById(...).get()` 에도 자동 적용된다.
  - 테스트 `t3` : 없는 글의 상세·수정 폼·삭제 요청이 모두 404 인지 확인.

### 030-1 PostComment 엔티티, 샘플 데이터 (실패) `복습하기`

- **개선점 발견** : 글에 의견을 남길 수 없다.
- **추가한 기능** : `PostComment` 엔티티(`@ManyToOne(fetch = LAZY) Post post`, `content`), `Post.comments`(`@OneToMany(mappedBy = "post", cascade = {PERSIST, REMOVE})`), `Post.addComment(content)`. 샘플 댓글 5개 추가.
- **구현 설명**
  - 연관관계의 주인은 FK(`POST_ID`)를 가진 `PostComment.post`. `Post.comments` 는 `mappedBy` 로 읽기 전용 거울.
  - `addComment` 는 댓글을 만들어 리스트에 넣기만 한다. `PERSIST` cascade 로 글이 영속 상태일 때 댓글도 함께 저장되고, `REMOVE` 로 글 삭제시 댓글도 삭제된다(029-A 글 삭제와도 맞물림).
  - 의도된 실패 단계 : `ApplicationRunner` 람다에는 트랜잭션이 없어 `write()` 직후 글이 준영속 상태가 되므로, 이후 `addComment` 한 댓글은 **저장되지 않는다**.
