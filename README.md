# NewSBB-1

스프링부트 복습용 SBB(Spring Boot Board) 단계별 구현 저장소.

- **복습하기** (`001-1`, `001-2` …) : 강사 저장소 [jhs512/p-14168-2](https://github.com/jhs512/p-14168-2)와 [강의 설명](https://www.slog.gg/p/14168)의 커밋 순서를 그대로 따라간다.
- **나아가기** (`001-A`, `001-B` …) : 참조에 없지만 개선·확장 가능한 기능을 단계마다 최대 2개까지 추가한다. ([위키독스 3장](https://wikidocs.net/book/7601), [추가 기능 예시](https://wikidocs.net/162814) 참고)
- 단계 번호 : 커밋이 있는 회차만 001 부터 빈 번호 없이 매겼다.
- 제작 과정 소개 영상 : [YouTube 에서 보기](https://youtu.be/_X-YeBUJCsc)
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

각 단계는 다음 형식으로 적는다.

- **링크** : 단어 "링크"를 누르면 해당 커밋 페이지로 이동해 직전 단계와의 코드 변경(diff)을 볼 수 있다
- **원인** : 이 단계가 필요한 이유(발견한 개선점)
- **작업** : 추가·수정·삭제한 기능, 객체, 클래스, 메서드
- **설명** : 작업의 구현 방식과 동작 원리 (작업에 적은 내용은 반복하지 않는다)

### 001 프로젝트 생성 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/3579b432d1a324a180e9afaccb856fe6256ab513)**
- **원인** : 아무것도 없는 상태. 스프링부트 애플리케이션이 실행될 뼈대가 필요하다.
- **작업**
  - 추가 : Gradle 프로젝트(`build.gradle.kts`, `settings.gradle.kts`, Gradle Wrapper), `BackApplication`, `BackApplicationTests`, `application.yaml`, `.gitignore`, `.gitattributes`
- **설명**
  - `build.gradle.kts` : Spring Boot 4.1.2 플러그인과 Java 25 toolchain 지정. Spring Boot 4부터 `spring-boot-starter-web` 대신 `spring-boot-starter-webmvc`, H2 콘솔은 별도 모듈 `spring-boot-h2console`로 분리되었다.
  - `application.yaml` : 애플리케이션 이름(`back`)만 지정.
  - `.gitattributes` : `gradlew`는 LF, `*.bat`는 CRLF로 고정해 OS가 달라도 실행 스크립트가 깨지지 않게 한다.
  - `./gradlew build` 로 `contextLoads` 테스트 통과 확인.

### 002 샘플 글 자동 생성 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/98e86841f77ac50d4d8729f4a119e5dd2a348d71)**
- **원인** : 화면을 만들기 전에 다룰 데이터가 없다. 서버를 켤 때마다 글을 직접 넣는 것은 번거롭다.
- **작업**
  - 추가 : `BaseEntity`, `Post` 엔티티, `PostRepository`, `PostService.count()`·`write()`, `BaseInitData`, `application-dev.yaml`, `application-test.yaml`
  - 수정 : `BackApplication`
    - 이전 : `@SpringBootApplication`
    - 이후 : `@SpringBootApplication` + `@EnableJpaAuditing`
  - 수정 : `application.yaml`
    - 이전 : 애플리케이션 이름만 설정
    - 이후 : `dev` 프로필, JPA(`ddl-auto: update`, SQL 출력), 로그 레벨 설정
  - 수정 : `BackApplicationTests`
    - 이전 : `@SpringBootTest`
    - 이후 : `@SpringBootTest` + `@ActiveProfiles("test")`
- **설명**
  - `BaseEntity` (`global.jpa.entity`) : `@MappedSuperclass`로 모든 엔티티의 공통 필드(`id`, `createDate`, `modifyDate`)를 모은 부모 클래스. `@CreatedDate`/`@LastModifiedDate`는 `AuditingEntityListener`가 채워주며, 이 리스너는 `@EnableJpaAuditing` 이 있어야 동작한다.
  - `Post` → `PostRepository`(`JpaRepository<Post, Integer>`) → `PostService`(`count`, `write`) 의 계층 구조. 패키지는 `domain.post.post.{entity,repository,service}` 처럼 도메인 기준으로 나눈다.
  - `BaseInitData` : `ApplicationRunner` 빈을 등록해 앱 구동 직후 실행. `count() > 0` 이면 건너뛰어 재시작해도 중복 생성되지 않는다.
  - 프로필 분리 : `dev` 는 파일 DB(`./db_dev`)라 재시작해도 데이터가 남고, `test` 는 메모리 DB(`db_test`)라 테스트마다 깨끗하게 시작한다. 둘 다 `MODE=MySQL`.
  - `ddl-auto: update` 는 엔티티 구조에 맞춰 테이블을 만들고 고친다. SQL 과 바인딩 값을 TRACE 로그로 남겨 JPA 가 실행하는 쿼리를 확인할 수 있다. `hikari.auto-commit: false` 이므로 저장은 트랜잭션 안에서 해야 반영된다.
  - 파일 DB(`db_dev.*.db`)는 `.gitignore` 로 저장소에서 뺀다.

### 003 글 작성 폼 추가 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/ccc58ca6ae2a6efd0ac0a5c39eba02ba425a96be)**
- **원인** : 데이터는 있지만 사용자가 글을 입력할 화면이 없다.
- **작업**
  - 추가 : `PostController.write()` — `GET /posts/write` 에 글 작성 폼 HTML 응답
- **설명**
  - `@Controller` + `@GetMapping("/posts/write")` + `@ResponseBody` : 템플릿 없이 메서드가 리턴한 문자열을 그대로 응답 본문으로 보낸다. 자바 텍스트 블록(`"""`)으로 HTML을 작성.
  - 폼은 "URL 생성기"일 뿐이다. 작성 버튼을 누르면 `action` 주소 뒤에 입력값이 쿼리스트링으로 붙어 `http://localhost:8080/posts/doWrite?title=안녕&content=반가워.` 로 이동한다(아직 `method` 미지정 → GET).
  - `/posts/doWrite` 를 처리하는 메서드는 아직 없으므로 전송하면 에러 페이지가 나온다. 005에서 구현한다.

### 004-1 폼 주소의 프로토콜·도메인·포트 생략 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/abdff8e1fea07380b1216d1575f2a9924c8db95e)**
- **원인** : `action="http://localhost:8080/posts/doWrite"` 처럼 주소를 통째로 쓰면 도메인·포트가 바뀔 때마다 고쳐야 한다.
- **작업**
  - 수정 : 글 작성 폼 action
    - 이전 : `action="http://localhost:8080/posts/doWrite"`
    - 이후 : `action="/posts/doWrite"`
- **설명**
  - `/` 로 시작하는 주소는 현재 페이지의 프로토콜·도메인·포트(`http://localhost:8080`)를 그대로 쓴다. 폼 주소와 action 주소의 앞부분이 같으면 그 부분은 생략할 수 있다.

### 004-2 폼 주소의 겹치는 경로 생략 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/41eef19e89847fbe531a259cfbcdee4256f1d110)**
- **원인** : 폼 페이지(`/posts/write`)와 action(`/posts/doWrite`)은 `/posts/` 까지 같다.
- **작업**
  - 수정 : 글 작성 폼 action
    - 이전 : `action="/posts/doWrite"`
    - 이후 : `action="doWrite"`
- **설명**
  - `/` 없이 시작하는 상대 경로는 현재 주소의 마지막 `/` 뒤만 바꾼다. `/posts/write` 에서 `doWrite` 는 `/posts/doWrite` 가 된다.

### 005 글 작성 처리 추가 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/578ac3b1f1f846af7937c0978ff10d53f5bd120b)**
- **원인** : 폼을 전송해도 받아서 처리하는 곳이 없다.
- **작업**
  - 추가 : `PostController.write(title, content)` — `GET /posts/doWrite` 글 저장, `PostService` 주입
  - 수정 : 폼을 보여주는 메서드 이름
    - 이전 : `write()`
    - 이후 : `showWrite()`
- **설명**
  - `@RequiredArgsConstructor` + `private final PostService` 로 생성자 주입.
  - `show` 로 시작하는 이름은 화면을 보여주는 메서드, 동사만 쓴 이름은 처리하는 메서드라는 이름 규칙을 따른다.
  - `@RequestParam(defaultValue = "")` : 쿼리스트링의 `title`, `content` 를 받는다. 값이 없어도 에러 대신 빈 문자열.
  - `@Transactional` 을 붙여 저장을 하나의 트랜잭션으로 처리(`auto-commit: false` 설정이므로 필요).

### 006 글 작성 요청을 POST 로 변경 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/fff2346ac2ae4430a59f1415e8d49692db96f81b)**
- **원인** : GET으로 전송하면 입력값이 주소창에 그대로 남고, 새로고침·북마크로 글이 또 생성될 수 있다. GET은 조회용이다.
- **작업**
  - 수정 : 폼 전송 방식
    - 이전 : `<form action="doWrite">`, `@GetMapping("/posts/doWrite")`
    - 이후 : `<form method="POST" action="doWrite">`, `@PostMapping("/posts/doWrite")`
- **설명**
  - POST는 입력값을 요청 본문(body)에 담아 보낸다. 생성·수정·삭제처럼 서버 상태를 바꾸는 요청은 POST를 쓴다. 이제 주소창에 `/posts/doWrite` 를 직접 입력(GET)하면 405 Method Not Allowed.

### 007-1 글 입력값 빈 값 검사 추가 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/0baf1e54516214817d8a8303990b3d584e65459e)**
- **원인** : 제목이나 내용을 비워도 글이 저장된다.
- **작업**
  - 추가 : `write()` 의 제목·내용 빈 값 검사(`isBlank()`) — 실패시 안내 문구 응답
- **설명**
  - `isBlank()` 는 공백만 있는 문자열도 비었다고 판단한다(`isEmpty()` 와 차이). 강의 원본은 이 커밋에서 `PostController` 를 controller 패키지로 옮기지만, 이 저장소는 003부터 이미 `controller` 패키지에 두었다.

### 007-2 제목 검사 실패시 폼 재표시 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/050d72d4564828b13b320311cbdaf82996e07aab)**
- **원인** : 에러 문구만 나오면 사용자가 뒤로가기를 눌러 다시 입력해야 한다.
- **작업**
  - 수정 : 제목 검사 실패시 응답
    - 이전 : `"제목을 입력해주세요."` 문구만
    - 이후 : 빨간 에러 메세지 + 작성 폼 HTML
- **설명**
  - 응답 HTML 안에 `<div style="color:red;">` 메세지와 폼을 같이 넣었다.

### 007-3 내용 검사 실패시 폼 재표시 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/e706dbe76653b4651a596da9013df3eb33f3c318)**
- **원인** : 내용이 비었을 때는 여전히 문구만 나온다.
- **작업**
  - 수정 : 내용 검사 실패시 응답
    - 이전 : `"내용을 입력해주세요."` 문구만
    - 이후 : 빨간 에러 메세지 + 작성 폼 HTML
- **설명**
  - 007-2와 같은 방식. 결과적으로 같은 폼 HTML이 세 군데(`showWrite`, 제목 실패, 내용 실패)에 중복된다.

### 007-4 폼 HTML 중복 제거 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/5225f511f4579cd0f4600e85a71fd003cedfa8f0)**
- **원인** : 폼 HTML 중복 3곳. 폼을 고치려면 세 곳을 다 고쳐야 한다.
- **작업**
  - 추가 : `getWriteFormHtml(errorMessage)`, `getWriteFormHtml()`
  - 수정 : 폼 HTML 생성 위치
    - 이전 : `showWrite()`, 제목 실패, 내용 실패 3곳에 같은 HTML
    - 이후 : 세 곳 모두 `getWriteFormHtml(...)` 호출
  - 삭제 : 폼 기본값(`안녕`, `반가워.`)
- **설명**
  - 텍스트 블록의 `%s` 자리에 `.formatted(errorMessage)` 로 메세지를 끼워 넣는다. 인자 없는 `getWriteFormHtml()` 은 빈 메세지로 위임하는 오버로딩.

### 008-1 검사 실패시 입력값 유지 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/4a95ae1ab88d2c19e41071a328db42c92e870aff)**
- **원인** : 내용만 비워서 실패해도 이미 입력한 제목까지 지워진다.
- **작업**
  - 수정 : `getWriteFormHtml` 시그니처
    - 이전 : `getWriteFormHtml(errorMessage)`
    - 이후 : `getWriteFormHtml(errorMessage, title, content)`
- **설명**
  - 실패한 요청의 파라미터를 그대로 폼에 돌려준다. (아직 HTML 이스케이프를 하지 않으므로 입력값에 `"` 나 태그가 있으면 깨진다 → 014 타임리프 도입에서 해결)

### 008-2 제목 입력칸 자동 포커스 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/68794fa52b4065d9bce0d507bb1e1cbd095efb31)**
- **원인** : 폼을 열면 제목 칸을 직접 클릭해야 입력할 수 있다.
- **작업**
  - 수정 : 제목 input
    - 이전 : `<input type="text" name="title" ...>`
    - 이후 : `<input type="text" name="title" ... autofocus>`
- **설명**
  - `autofocus` 는 페이지 로드시 해당 요소에 자동으로 커서를 둔다. 페이지에 하나만 두는 것이 원칙.

### 008-3 검사 실패 필드 포커스 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/322de9b5dfaef26e26e67d2f784ced2edb45d804)**
- **원인** : 내용이 비어서 실패해도 커서는 autofocus 때문에 제목에 간다.
- **작업**
  - 추가 : 실패 필드 포커스 스크립트
  - 수정 : `getWriteFormHtml` 시그니처
    - 이전 : `getWriteFormHtml(errorMessage, title, content)`
    - 이후 : `getWriteFormHtml(errorFieldName, errorMessage, title, content)`
- **설명**
  - 폼 뒤의 `<script>` 에서 `document.querySelectorAll('form')` 중 마지막 폼을 찾고 `lastForm[errorFieldName].focus()`. 폼 요소는 `form['name']` 으로 필드에 접근할 수 있다. 스크립트가 폼 뒤에 있어야 폼이 이미 만들어진 상태다.

### 009-1 제목·내용 길이 제한 추가 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/0749bfd21730a48748dd220f4911b4eabc8542bd)**
- **원인** : 한 글자 제목이나 지나치게 긴 내용도 저장된다.
- **작업**
  - 추가 : 제목 2자 이상 20자 이하, 내용 2자 이상 100자 이하 길이 검사(`if` 4개)
- **설명**
  - 체크 항목이 늘어날수록 `if` 문이 길어진다. 이 반복을 다음 커밋에서 어노테이션으로 바꾼다.

### 009-2 어노테이션 기반 유효성 검사로 변경 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/cd5f81a5d51cd0bf9754842aee0c241072708e80)**
- **원인** : 손으로 쓴 `if` 유효성 체크가 6줄. 규칙을 선언만 하고 싶다.
- **작업**
  - 추가 : `spring-boot-starter-validation`(·`-test`) 의존성, `PostController` 의 `@Validated`
  - 수정 : `write()` 파라미터
    - 이전 : `@RequestParam String title, @RequestParam String content`
    - 이후 : `@NotBlank @Size(min = 2, max = 20) ... title`, `@NotBlank @Size(min = 2, max = 100) ... content`
  - 삭제 : `if` 유효성 검사 6줄
- **설명**
  - Bean Validation(jakarta.validation) 어노테이션으로 규칙을 선언하면 스프링이 메서드 호출 전에 검사한다.
  - `@RequestParam` 같은 단일 파라미터에 붙은 제약을 검사하려면 클래스에 `@Validated` 가 필요하다.
  - 실패하면 예외(`HandlerMethodValidationException`)가 발생해 에러 페이지가 뜬다. 폼 재표시는 일단 사라졌다 → 011에서 복구.

### 010 폼 입력값의 객체 바인딩 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/a38bdfb9d80a4ff9f52f22f9d46114ff1a46a1bd)**
- **원인** : 파라미터마다 어노테이션이 3개씩 붙어 메서드 시그니처가 길다. 필드가 늘수록 더 길어진다.
- **작업**
  - 추가 : `PostController.WriteForm`(title, content + 제약 어노테이션)
  - 수정 : `write()` 파라미터
    - 이전 : 제약 어노테이션이 붙은 `@RequestParam` 2개
    - 이후 : `@Valid WriteForm form`
  - 삭제 : 클래스의 `@Validated`
- **설명**
  - 단순 타입이 아닌 객체 파라미터는 `@ModelAttribute` 가 생략된 것으로 처리되어, 요청 파라미터가 같은 이름의 필드(생성자 인자)에 바인딩된다. `@AllArgsConstructor` 의 생성자로 값이 들어간다.
  - 객체 내부 제약을 검사하려면 파라미터에 `@Valid` 를 붙인다. 이제 클래스의 `@Validated` 는 필요 없다.

### 011-1 유효성 검사 실패 예외 방지 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/fbe829adc640dbe39afd659d3f93fb4ca394c4a4)**
- **원인** : 유효성 실패시 예외가 던져져 400 에러 페이지가 뜬다. 실패 후 처리를 직접 하고 싶다.
- **작업**
  - 추가 : `write()` 의 `BindingResult bindingResult` 파라미터
  - 수정 : 유효성 실패시 동작
    - 이전 : 예외 발생(400 에러 페이지)
    - 이후 : `bindingResult.hasErrors()` 면 빈 작성 폼 응답
- **설명**
  - `BindingResult` 가 `@Valid` 파라미터 **바로 다음**에 있으면 스프링은 예외 대신 검사 결과를 여기에 담아 메서드를 정상 호출한다.

### 011-2 대표 에러 메세지 출력 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/54252aa249dcd218f81f5bf438da4f4d137112e5)**
- **원인** : 실패해도 무엇이 잘못됐는지 알려주지 않고 입력값도 사라진다.
- **작업**
  - 수정 : 유효성 실패시 응답
    - 이전 : 빈 작성 폼
    - 이후 : `bindingResult.getFieldError()` 의 필드명·메세지와 입력값을 담은 작성 폼
- **설명**
  - `FieldError.getField()` 는 실패 필드명(`title`/`content`) → 008-3의 포커스 스크립트에 그대로 사용. `getDefaultMessage()` 는 어노테이션 기본 메세지("공백일 수 없습니다" 등).

### 011-3 에러 메세지 문구 지정 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/313bbac2ada918a97a2a86c78401ded0d1d5b241)**
- **원인** : 기본 메세지("크기가 2에서 20 사이여야 합니다")는 서비스 문구로 어색하다.
- **작업**
  - 수정 : `WriteForm` 제약 어노테이션
    - 이전 : `@NotBlank`, `@Size(...)`
    - 이후 : `@NotBlank(message = "제목을 입력해주세요.")`, `@Size(..., message = "...")`
- **설명**
  - 각 제약 어노테이션의 `message` 속성이 `getDefaultMessage()` 결과가 된다. (content 는 결정 사항 9에 따라 "2자 이상, 100자 이하")

### 012-1 전체 에러 메세지 출력 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/7ce72e0f05e2bd8c0fb282aaec8c0cea1f9c689b)**
- **원인** : 제목·내용이 모두 틀려도 에러가 하나만 보여서, 고치고 다시 보내야 다음 에러를 알 수 있다.
- **작업**
  - 수정 : 에러 메세지 생성
    - 이전 : `getFieldError()` 대표 에러 1개
    - 이후 : `getFieldErrors()` 스트림으로 전체 메세지를 `<br>` 로 연결, 포커스 필드는 `"title"` 고정
- **설명**
  - `stream().map(FieldError::getDefaultMessage).collect(Collectors.joining("<br>"))`. 단, `getFieldErrors()` 의 순서는 보장되지 않아 새로고침마다 메세지 순서가 바뀔 수 있다.

### 012-2 에러 메세지 순서 고정 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/d4bdd9dd2cece6d4b865e776a88d9233dbd9bd08)**
- **원인** : 에러 메세지 순서가 매번 달라진다.
- **작업**
  - 수정 : 에러 메세지
    - 이전 : `"제목을 입력해주세요."`
    - 이후 : `"01-제목을 입력해주세요."` — `sorted()` 후 순번을 떼고 출력
- **설명**
  - 문자열 정렬로 순서를 고정하는 간단한 트릭. `message.split("-", 2)[1]` 로 첫 `-` 뒤만 사용(최대 2조각으로 나눠 본문의 `-` 는 보존).

### 013-1 에러 메세지에 필드명 추가 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/8cb82e55ca3cc2d63aff42fb545def07667ce7c0)**
- **원인** : 여러 에러 중 첫 번째 에러 필드에 포커스를 주려면 메세지마다 어떤 필드인지 알아야 한다.
- **작업**
  - 수정 : 에러 문자열
    - 이전 : `메세지`(정렬 후 순번 제거)
    - 이후 : `필드명-메세지` (예: `title-01-제목을 입력해주세요.`)
- **설명**
  - 다음 커밋에서 이 문자열을 `-` 로 쪼개 필드명/순번/메세지로 나눠 쓴다. (이 커밋에선 정렬이 잠시 빠짐)

### 013-2 에러 목록의 HTML 목록화 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/5d8d70e6fb6c500119fb575bb59bf0d9fa4b4462)**
- **원인** : `<br>` 로 이은 문자열은 프론트에서 어떤 필드의 에러인지 알아내기 어렵다.
- **작업**
  - 수정 : 에러 출력 HTML
    - 이전 : `<div>` 안에 `<br>` 로 연결
    - 이후 : `<ul>` 안에 `<!--순번--><li data-error-field-name="필드명">메세지</li>`
- **설명**
  - `split("-", 3)` → `[필드명, 순번, 메세지]`. `data-*` 속성은 자바스크립트에서 `element.dataset.errorFieldName` 으로 읽을 수 있다(케밥 → 카멜 케이스).

### 013-3 첫 에러 필드 포커스의 프론트 처리 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/2413359235bec261eb9a150c8f7ddfc8b664bfbc)**
- **원인** : 서버가 `errorFieldName` 을 따로 계산해서 넘기는 것은 중복이다. 이미 HTML 에 정보가 있다.
- **작업**
  - 수정 : 포커스할 필드 결정
    - 이전 : 서버가 `errorFieldName` 을 계산해 스크립트에 전달
    - 이후 : `<li>` 를 정렬해 출력하고, 스크립트가 첫 `li` 의 `data-error-field-name` 을 읽음
  - 삭제 : `getWriteFormHtml` 의 `errorFieldName` 인자
- **설명**
  - `lastForm.previousElementSibling?.querySelector('li')?.dataset?.errorFieldName || ''`. 옵셔널 체이닝(`?.`)으로 에러가 없을 때도 안전.

### 014-1 타임리프 글 작성 템플릿 도입 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/4a5f9f5b04ce22fea39e9479522000731dd2c8b2)**
- **원인** : 자바 문자열 안에 HTML을 쓰니 에디터 지원(자동완성, 하이라이트)도 없고 `%s` 순서 맞추기도 번거롭다.
- **작업**
  - 추가 : `templates/post/post/write.html`
  - 수정 : `showWrite()`
    - 이전 : `@ResponseBody` + `getWriteFormHtml()` 문자열 리턴
    - 이후 : 뷰 이름 `"post/post/write"` 리턴
  - 삭제 : 인자 없는 `getWriteFormHtml()`
- **설명**
  - `@ResponseBody` 를 떼면 리턴 문자열은 응답 본문이 아니라 **뷰 이름**이 되고, 타임리프가 `classpath:/templates/{뷰이름}.html` 을 찾아 렌더링한다. 템플릿 경로는 패키지 구조(`domain/post/post`)와 맞췄다. POST 쪽은 아직 문자열 HTML.

### 014-2 글 작성 완료 템플릿 도입 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/a4043d5b237fabc395dffea5ecbf202f9de8a70f)**
- **원인** : POST 처리 결과도 여전히 자바 문자열 HTML.
- **작업**
  - 추가 : `templates/post/post/writeDone.html`, `write()` 의 `Model` 파라미터
  - 수정 : `write()` 응답
    - 이전 : `@ResponseBody` 문자열(폼 HTML / 완료 문구)
    - 이후 : 실패시 `"post/post/write"`, 성공시 `"post/post/writeDone"` 템플릿
  - 삭제 : `getWriteFormHtml()`
- **설명**
  - 액션 메서드에 `Model model` 을 받아 `model.addAttribute("post", post)` 로 템플릿에 값 전달. 템플릿에서는 인라인 표현식 `[[${post.id}]]` 로 출력. 실패 메세지·입력값은 아직 템플릿에 연결하지 않았다.

### 014-3 에러 메세지 이스케이프 출력 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/61a5b551127cd1cdeb1a5585f4254d8cf7d9afb2)**
- **원인** : 실패해도 에러 메세지가 보이지 않는다.
- **작업**
  - 추가 : 모델 속성 `errorMessage`
  - 수정 : `write.html` 에러 영역
    - 이전 : `<ul style="color: red;">`
    - 이후 : `<ul style="color: red;" th:text="${errorMessage}">`
- **설명**
  - `th:text` 는 값을 HTML 이스케이프해 태그 내용으로 넣는다. 그래서 `<li ...>` 가 태그로 렌더링되지 않고 문자 그대로 화면에 보인다(XSS 방지가 기본).

### 014-4 에러 메세지 HTML 출력 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/0032732ef1bf6198d3b93c00cc7f8b6f37963b16)**
- **원인** : `li` 태그가 글자로 보인다.
- **작업**
  - 수정 : 에러 출력 속성
    - 이전 : `th:text="${errorMessage}"`
    - 이후 : `th:utext="${errorMessage}"`
- **설명**
  - `utext`(unescaped text)는 HTML 을 그대로 삽입해 렌더링한다. 서버가 만든 신뢰할 수 있는 HTML 에만 쓰고, 사용자 입력에는 쓰지 않는다.

### 014-5 템플릿에서 입력값 유지 (작성 폼 오류) `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/18fe75f047f0e0ed90ea72fb2b0bcd8beb748205)**
- **원인** : 실패 후 입력값이 사라진다.
- **작업**
  - 수정 : 작성 폼 입력값
    - 이전 : `value=""`, 빈 textarea
    - 이후 : `th:value="${writeForm.title}"`, `th:text="${writeForm.content}"`
- **설명**
  - `@Valid WriteForm form` 처럼 받은 객체는 **클래스명 첫 글자 소문자(`writeForm`)** 이름으로 자동으로 모델에 담긴다. 그래서 POST 실패 화면은 잘 되지만, `GET /posts/write` 에는 `writeForm` 이 없어 `null.title` 접근으로 **에러**가 난다(의도된 실패 단계).

### 014-6 안전 탐색 연산자로 작성 폼 오류 해결 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/7ed9507c45d016e3ae6d5fa8b49e836de78246ff)**
- **원인** : GET 에서 `writeForm` 이 null 이라 터진다.
- **작업**
  - 수정 : 작성 폼 입력값 표현식
    - 이전 : `${writeForm.title}`
    - 이후 : `${writeForm?.title}`
- **설명**
  - SpEL 안전 탐색 연산자 `?.` 는 앞이 null 이면 null 을 반환해 예외가 나지 않는다.

### 014-7 작성 폼에 폼 객체 전달 (검사 오류) `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/2a8b5088228aa43f39b76b46c7b390936b41e37b)**
- **원인** : 템플릿 곳곳에 `?.` 를 쓰는 대신, GET 에도 `writeForm` 을 넣어주면 된다.
- **작업**
  - 수정 : `showWrite()` 파라미터
    - 이전 : 없음
    - 이후 : `@Valid WriteForm form`
  - 수정 : 작성 폼 입력값 표현식
    - 이전 : `${writeForm?.title}`
    - 이후 : `${writeForm.title}`
- **설명**
  - 파라미터로 받기만 해도 모델에 `writeForm` 이 담긴다. 그러나 `@Valid` 때문에 빈 폼을 여는 순간 유효성 검사가 실패해 400 에러(BindingResult 없음) → 의도된 실패 단계.

### 014-8 작성 폼의 유효성 검사 제거 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/1a800ce1975a873078060c8d4a7b2b0e6b73c42b)**
- **원인** : 폼을 처음 보여줄 때는 검사할 필요가 없다.
- **작업**
  - 수정 : `showWrite()` 파라미터
    - 이전 : `@Valid WriteForm form`
    - 이후 : `WriteForm form`
- **설명**
  - 검사 없이 바인딩만 해서 빈 `writeForm` 이 모델에 담기므로 GET 이 정상 작동. 이렇게 하면 `/posts/write?title=abc` 처럼 초기값을 쿼리로 넣을 수도 있다.

### 015 템플릿 캐시 해제와 폼 모델 이름 지정 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/1a0d4f1543c084e90e69e86161b1516a57ec3a95)**
- **원인** : 템플릿을 고칠 때마다 서버를 재시작해야 반영된다. 또 `writeForm` 이라는 모델 이름이 길고 클래스명에 묶여 있다.
- **작업**
  - 추가 : `application-dev.yaml` 의 `spring.thymeleaf.cache: false`, `prefix: file:src/main/resources/templates/`
  - 수정 : `showWrite()`·`write()` 의 폼 파라미터
    - 이전 : `WriteForm form` (모델 이름 `writeForm`)
    - 이후 : `@ModelAttribute("form") WriteForm form` (모델 이름 `form`)
- **설명**
  - prefix 를 `file:` 소스 폴더로 바꾸면 빌드 결과물(classpath)이 아니라 원본 파일을 바로 읽으므로, 캐시를 끈 상태에서 새로고침만으로 수정이 반영된다(dev 프로필 한정).

### 016 사이트 이름 공통 모델 변수 추가 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/1cba5369190160e9a52ca1d843fb08e02167e8f8)**
- **원인** : 모든 페이지에 공통으로 보일 사이트 이름을 액션 메서드마다 `model.addAttribute` 하기는 번거롭다.
- **작업**
  - 추가 : `PostController.siteName()`(`@ModelAttribute("siteName")`), 템플릿 상단 `<h1 th:text="${siteName}">`
  - 수정 : 작성 폼 입력값 표현식
    - 이전 : `${writeForm.title}`
    - 이후 : `${form.title}`
- **설명**
  - 메서드에 붙은 `@ModelAttribute` 는 **그 컨트롤러의 모든 액션 메서드 실행 전에** 호출되어 리턴값이 모델에 담긴다. 모든 컨트롤러에 적용하려면 `@ControllerAdvice` 클래스에 두면 된다.

### 017 에러 메세지 출력 래퍼 태그 제거 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/86341bb39295df65d4a0f875f92636af99dfa4ae)**
- **원인** : `<ul th:utext>` 는 `ul` 의 내용 전체를 덮어써서, `ul` 안에 다른 요소를 함께 둘 수 없다.
- **작업**
  - 수정 : 에러 출력 위치
    - 이전 : `<ul th:utext="${errorMessage}">`
    - 이후 : `<ul>` 안의 `<th:block th:utext="${errorMessage}">`
- **설명**
  - `th:block` 은 렌더링 후 자기 자신은 사라지고 결과(자식)만 남는 타임리프 전용 가상 태그. 반복·조건 처리에 래퍼 태그 없이 쓸 때 유용하다.

### 018 입력칸 라벨 추가 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/ed47981a9e874591b62048a79b97b487d0c529ea)**
- **원인** : 입력칸에 이름표가 없고, placeholder 는 입력을 시작하면 사라진다.
- **작업**
  - 추가 : 필드별 `<div>`, `<label for="...">`, input·textarea 의 `id`
  - 삭제 : 필드 사이 `<br>`
- **설명**
  - `label` 의 `for` 와 input 의 `id` 를 맞추면 라벨 클릭시 입력칸에 포커스되고 스크린리더 접근성이 좋아진다. 정석은 for/id 연결이지만, label 안에 input 을 넣는 방식도 쓰인다.

### 019-1 폼 객체 기준 필드 표현식 적용 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/88cba4d5d0d483ec1809f4dad186fca1a0f88d23)**
- **원인** : 필드마다 `form.` 접두어를 반복한다.
- **작업**
  - 추가 : `<form th:object="${form}">`
  - 수정 : 입력값 표현식
    - 이전 : `${form.title}`
    - 이후 : `*{title}`
- **설명**
  - `th:object` 로 기준 객체를 정하면 그 엘리먼트 안에서 `*{필드}` 는 `${form.필드}` 와 같다.

### 019-2 폼 필드 속성 자동 생성 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/1b6def72ed3d2c64319b5b4104467fff8e796065)**
- **원인** : `name`, `id`, `value` 를 필드마다 직접 맞춰 써야 한다.
- **작업**
  - 수정 : 입력칸 속성
    - 이전 : `name="title" id="title" th:value="*{title}"`
    - 이후 : `th:field="*{title}"`
- **설명**
  - `th:field` 는 `name="title" id="title" value="..."` (textarea 는 내용)를 자동 생성한다. label 의 `for="title"` 과 자동 id 가 일치한다.

### 020-1 템플릿에서 에러 메세지 출력 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/e9bf4750b794c31dd73a18ca2b3ae0ffd94f4c09)**
- **원인** : 컨트롤러가 에러 HTML(`<li>`)까지 만들어 넘긴다. 화면 표현은 템플릿의 몫이다.
- **작업**
  - 추가 : `write.html` 의 `#fields.hasAnyErrors()`, `#fields.allErrors()` 에러 목록
  - 수정 : `write()` 의 실패 처리
    - 이전 : 에러 HTML 문자열을 만들어 `errorMessage` 로 모델에 추가
    - 이후 : `if (bindingResult.hasErrors()) return "post/post/write";`
- **설명**
  - `#fields` 는 `th:object` 객체의 BindingResult 에 접근하는 타임리프 유틸리티. 에러 목록 `ul` 은 `form` 안으로 옮겼다.

### 020-2 템플릿 에러 메세지 정렬 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/d3b6cefbb0af6ee7e13014ff000030b4be81f0f3)**
- **원인** : 템플릿 출력도 순서가 매번 다르고 `01-` 번호가 그대로 보인다.
- **작업**
  - 수정 : 에러 목록 반복
    - 이전 : `${#fields.allErrors()}`, `th:text="${err}"`
    - 이후 : `${#fields.allErrors().stream().sorted()}`, `th:text="${err.split('-', 2)[1]}"`
  - 삭제 : 포커스 스크립트
- **설명**
  - 타임리프 표현식(SpEL) 안에서도 자바 메서드·스트림을 호출할 수 있다. HTML 구조가 바뀌어 기존 포커스 스크립트가 맞지 않으므로 일단 제거(022에서 복원).

### 021-1 에러 메세지에 필드명 포함 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/22e8bf76ea5cc3149561129606cb0e8e6da6a076)**
- **원인** : 템플릿에서는 `getField()` 를 따로 꺼내기 번거롭다. 포커스를 복원하려면 메세지에 필드명이 있어야 한다.
- **작업**
  - 수정 : 에러 메세지 형식
    - 이전 : `"01-제목을 입력해주세요."`
    - 이후 : `"01-title-제목을 입력해주세요."`
- **설명**
  - 013 에서 코드로 붙이던 필드명을 메세지 자체에 넣었다. 정렬은 맨 앞 순번 기준으로 그대로 동작.

### 021-2 에러 필드명 속성 추가 (고정값) `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/b662772007444e458b4ab7e39f01a080cfe205c3)**
- **원인** : `li` 에 어떤 필드의 에러인지 표시가 없다.
- **작업**
  - 추가 : `<li>` 의 `data-error-field-name="title"`
  - 수정 : 메세지 추출
    - 이전 : `err.split('-', 2)[1]`
    - 이후 : `err.split('-', 3)[2]`
- **설명**
  - 동작 위치를 먼저 확인하기 위한 중간 단계.

### 021-3 에러 필드명 속성 동적 지정 (실패) `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/9c6cd87c722827162df511d323424488ee03c7c6)**
- **원인** : 필드명을 하드코딩이 아닌 메세지에서 꺼내야 한다.
- **작업**
  - 수정 : 필드명 속성
    - 이전 : `data-error-field-name="title"`
    - 이후 : `data-error-field-name="${err.split('-', 3)[1]}"`
- **설명**
  - 의도된 실패 단계. `th:` 접두어가 없는 일반 속성은 타임리프가 처리하지 않아 `${...}` 문자열이 그대로 출력된다.

### 021-4 에러 필드명 속성 동적 지정 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/9c184cd85ea19843eb57aeeef42a81d38ec1e18a)**
- **원인** : 021-3 에서 표현식이 평가되지 않았다.
- **작업**
  - 수정 : 필드명 속성
    - 이전 : `data-error-field-name="${...}"`
    - 이후 : `th:data-error-field-name="${...}"`
- **설명**
  - `th:아무속성` 은 표현식 결과로 그 속성을 만든다. `data-*` 같은 커스텀 속성에도 사용 가능.

### 021-5 에러 메세지 분해 결과 변수화 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/08675799aec631d2709b754cfd9030af9ca6d461)**
- **원인** : 같은 `split` 을 두 번 호출하고 가독성이 떨어진다.
- **작업**
  - 추가 : `th:with="errStrs = ..., errFieldName = ..., errMsg = ..."`
  - 수정 : 필드명·메세지 표현식
    - 이전 : `${err.split('-', 3)[1]}`, `${err.split('-', 3)[2]}`
    - 이후 : `${errFieldName}`, `${errMsg}`
- **설명**
  - `th:with` 는 해당 엘리먼트 범위의 지역 변수를 선언한다. 앞에서 선언한 변수를 뒤 변수에서 바로 쓸 수 있다.

### 022-1 에러 필드 포커스 스크립트 복원 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/4e47c23252bcb6d1fec09e5d3e6cfbd328f5075d)**
- **원인** : 020-2 에서 지운 포커스 기능이 없다.
- **작업**
  - 추가 : 폼 뒤 포커스 스크립트(`lastForm.querySelector('ul > li')`)
- **설명**
  - 013-3 의 `previousElementSibling` 대신 form 내부 탐색으로 변경.

### 022-2 포커스 스크립트 위치 이동 (실패) `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/ee4042200499245ea5a0a4b305b3b0856efa43fc)**
- **원인** : 에러 목록과 스크립트를 한 덩어리로 묶어 재사용하고 싶다 → 스크립트를 `ul` 바로 아래(form 안)로 옮겨 본다.
- **작업**
  - 수정 : 포커스 스크립트 위치
    - 이전 : `</form>` 뒤
    - 이후 : form 안, 에러 `ul` 바로 아래
- **설명**
  - 의도된 실패 단계. 스크립트가 실행되는 시점에는 아래쪽 input 들이 아직 파싱되지 않아 `lastForm[errorFieldName]` 이 없다.

### 022-3 포커스 지연 실행 (1초) `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/2efe9c21cb0ab6e225ec9365c3e4860560e1818e)**
- **원인** : 폼이 완성되기 전에 포커스를 시도한다.
- **작업**
  - 수정 : 포커스 호출
    - 이전 : `lastForm[errorFieldName].focus()`
    - 이후 : `setTimeout(() => lastForm[errorFieldName].focus(), 1000)`
- **설명**
  - 포커스 코드만 나중으로 미루면 그 사이 브라우저가 나머지 HTML 을 다 파싱한다.

### 022-4 포커스 지연 시간 단축 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/aca43a733b700696e9d8ea48e599edd3df512156)**
- **원인** : 1초는 사용자 눈에 보일 만큼 느리다.
- **작업**
  - 수정 : 지연 시간
    - 이전 : `1000`
    - 이후 : `1`
- **설명**
  - 중요한 건 시간이 아니라 "현재 실행 중인 스크립트(파싱) 이후"로 미루는 것. 이벤트 루프상 타이머 콜백은 현재 작업이 끝난 뒤 실행된다.

### 022-5 포커스 지연 시간 생략 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/8c02df91c41afa524ea6c2a1f21332ab7df8b1e2)**
- **원인** : `1` 이라는 숫자도 불필요하다.
- **작업**
  - 수정 : 지연 시간
    - 이전 : `setTimeout(..., 1)`
    - 이후 : `setTimeout(...)`
- **설명**
  - 생략하면 0 으로 처리되어 가능한 빨리, 하지만 현재 작업 이후에 실행된다.

### 022-6 폼 에러 프래그먼트 분리 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/0bf7ea2884489edfde93bbe7d429e6060eb029f3)**
- **원인** : 에러 목록 + 포커스 스크립트는 앞으로 모든 폼에서 쓸 공통 부품이다.
- **작업**
  - 추가 : `templates/form_errors.html`(`th:fragment="formErrorsFragment"`)
  - 수정 : `write.html` 에러 영역
    - 이전 : 에러 `ul` + 포커스 스크립트
    - 이후 : `<div th:replace="~{form_errors :: formErrorsFragment}">`
- **설명**
  - `th:replace` 는 해당 태그를 프래그먼트로 통째로 교체한다. 프래그먼트 안의 `#fields` 는 삽입된 위치(`th:object` form 안)의 문맥을 그대로 쓴다.

### 023-1 글 작성 처리 주소 변경 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/c6e863ad9c57281b87f5f77d412543ffea5d2095)**
- **원인** : `doWrite` 의 "do" 는 POST 라는 메서드가 이미 말해 준다.
- **작업**
  - 수정 : 글 작성 처리 주소
    - 이전 : `@PostMapping("/posts/doWrite")`, `action="doWrite"`
    - 이후 : `@PostMapping("/posts/write")`, `action="write"`
- **설명**
  - URL 이 같아도 HTTP 메서드가 다르면 스프링이 다른 액션 메서드로 구분한다. `GET /posts/write` = 폼 보여주기, `POST /posts/write` = 처리.

### 023-2 폼 action 생략 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/4abe19be924eb11d0f2973f01725d16583e54b09)**
- **원인** : action 이 현재 페이지 주소와 같다.
- **작업**
  - 삭제 : 작성 폼의 `action="write"`
- **설명**
  - action 이 없으면 폼은 현재 주소로 전송된다.

### 024 작성 완료 후 자동 이동 (3초) `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/649a643bfd1afbd536dd4a68b52f34c0f4a30540)**
- **원인** : 글 작성 후 완료 문구만 덩그러니 남는다. 이 화면에서 새로고침하면 POST 가 다시 전송돼 글이 중복 생성된다.
- **작업**
  - 수정 : `writeDone.html`
    - 이전 : `[[${post.id}]]번 글이 생성되었습니다.`
    - 이후 : 같은 문구 + `<meta http-equiv="refresh" content="3;url=/posts/write">` + "3초 후에 이동됩니다."
- **설명**
  - 브라우저가 3초 뒤 지정 URL 로 GET 이동한다.

### 025-1 작성 완료 후 즉시 이동 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/e833ba34486ff3f65390506cbcf187be7a7550b0)**
- **원인** : 기다릴 필요가 없다.
- **작업**
  - 수정 : `writeDone.html`
    - 이전 : 사이트 이름, 완료 문구, 3초 meta refresh
    - 이후 : `<meta http-equiv="refresh" content="0;url=/posts/write">` 한 줄
- **설명**
  - 즉시 이동하지만, 여전히 "HTML 응답 → 브라우저가 다시 요청" 의 2단계이고 HTML 을 내려줘야 한다.

### 025-2 작성 완료 후 리다이렉트 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/c304a724bc6c7b00a6c2fbf2f3d370ef8ac93ac9)**
- **원인** : 이동만 할 거라면 HTML 페이지가 필요 없다.
- **작업**
  - 수정 : `write()` 성공시 응답
    - 이전 : `"post/post/writeDone"` 템플릿
    - 이후 : `"redirect:/posts/write"`
  - 삭제 : `writeDone.html`, `write()` 의 `Model` 파라미터
- **설명**
  - `redirect:` 접두어는 302 응답 + `Location` 헤더를 보낸다. POST 후 Redirect 후 GET 하는 **PRG 패턴**으로, 새로고침해도 POST 가 다시 전송되지 않는다.

### 026-1 글 상세페이지 추가 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/f747d292ab6ef5557b6ee975ef6dc2f30f37c797)**
- **원인** : 작성한 글을 볼 방법이 없다. 작성 후 다시 빈 폼으로 돌아간다.
- **작업**
  - 추가 : `PostController.showDetail()`(`GET /posts/{id}`), `PostService.findById()`, `detail.html`
  - 수정 : `write()` 성공시 이동
    - 이전 : `"redirect:/posts/write"`
    - 이후 : `"redirect:/posts/" + post.getId()`
- **설명**
  - `@PathVariable int id` 로 URL 경로의 값을 받는다. 조회 전용 메서드에는 `@Transactional(readOnly = true)` 로 변경 감지 등 불필요한 작업을 줄인다. `findById` 는 `Optional` 을 리턴하므로 일단 `.get()`.

### 026-2 본문 줄바꿈 표시 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/253385b0c48bab9bb4f7f40b4f2447bfa3f21499)**
- **원인** : 내용에 입력한 줄바꿈이 상세페이지에서 한 줄로 붙어 보인다.
- **작업**
  - 수정 : 본문 div
    - 이전 : `<div>[[${post.content}]]</div>`
    - 이후 : `<div style="white-space: pre-line;">[[${post.content}]]</div>`
- **설명**
  - HTML 은 줄바꿈 문자를 공백으로 취급한다. `pre-line` 은 줄바꿈은 살리고 연속 공백은 하나로 합친다. (`<br>` 로 치환해 `utext` 를 쓰는 것보다 XSS 에 안전)

### 026-3 상세페이지 이동 링크 추가 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/3043305fd9c3ee9aa640cec1d4cf8f04652e5ac5)**
- **원인** : 상세페이지에서 다른 곳으로 갈 길이 없다.
- **작업**
  - 추가 : 상세페이지의 글 목록·글 쓰기·글 수정 링크
- **설명**
  - `/posts/1` 기준 상대경로 `.` → `/posts/`, `write` → `/posts/write`. `|...|` 는 타임리프 리터럴 치환(문자열 템플릿).

### 026-4 글 수정 링크 표현식 변경 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/6deaf57a8d82a584cb9d995ac4a90c0e86e8ce42)**
- **원인** : URL 을 문자열 치환으로 만들면 인코딩·컨텍스트 경로 처리가 안 된다.
- **작업**
  - 수정 : 글 수정 링크
    - 이전 : `th:href="|${post.id}/modify|"`
    - 이후 : `th:href="@{{id}/modify(id=${post.id})}"`
- **설명**
  - `@{...}` 링크 표현식은 `{id}` 자리에 괄호 안 파라미터를 URL 인코딩해 넣는다. `/` 로 시작하면 컨텍스트 경로도 자동으로 붙는다.

### 026-5 글 목록 JSON 응답 추가 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/907188c2475f958f3f727a9b88a0cebcba3edbb9)**
- **원인** : 상세의 "글 목록" 링크가 갈 곳이 없다.
- **작업**
  - 추가 : `PostController.showList()`(`GET /posts`, `@ResponseBody List<Post>`), `redirectToList()`(`GET /posts/`), `PostService.findAll()`
- **설명**
  - `@ResponseBody` 메서드가 객체를 리턴하면 `HttpMessageConverter`(Jackson)가 JSON 으로 변환한다. 이 JSON 을 프론트(fetch)에서 받아 그리면 클라이언트 사이드 렌더링이 된다. 끝 `/` 유무가 다른 URL 이므로 리다이렉트로 통일.

### 027-1 JSON 출력 필드 제한 실험 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/6e1201ac85a3115f5b1e0dddf332024ca687aecd)**
- **원인** : JSON 에 어떤 필드가 나가는지 조절할 수 있을까?
- **작업**
  - 수정 : `BaseEntity` 날짜 필드 getter
    - 이전 : 클래스 `@Getter`(public)
    - 이후 : `createDate` 는 `@Getter(PRIVATE)`, `modifyDate` 는 `@Getter(PROTECTED)`
- **설명**
  - Jackson 은 public 필드 또는 public getter 가 있는 속성만 직렬화한다. 그래서 두 날짜가 JSON 에서 빠진다. `protected` 는 같은 패키지+자식만, `public` 은 어디서나 접근 가능.

### 027-2 JSON 출력 필드 제한 원복 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/028c18c36a4a3cd00fa8ce28b33487921e43546d)**
- **원인** : 확인용 실험이었고 날짜는 화면에 필요하다.
- **작업**
  - 수정 : `BaseEntity` 날짜 필드 getter
    - 이전 : `@Getter(PRIVATE)`, `@Getter(PROTECTED)`
    - 이후 : 클래스 `@Getter`(public)
- **설명**
  - 날짜가 다시 JSON 과 화면에 모두 노출된다.

### 028-1 글 목록 페이지 추가 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/a4b4af5cef5edd0b00489f06ee97e347e5ae1c69)**
- **원인** : 목록이 JSON 이라 사람이 보기 불편하다.
- **작업**
  - 추가 : `templates/post/post/list.html`
  - 수정 : `showList()`
    - 이전 : `@ResponseBody` + `List<Post>` 리턴(JSON)
    - 이후 : `Model` 에 `posts` 를 담아 `"post/post/list"` 리턴
- **설명**
  - `#temporals.format(post.createDate, 'yy.MM.dd HH:mm')` 로 `LocalDateTime` 포맷. 링크는 `@{/posts/{id}(id=${post.id})}`.

### 028-2 상세페이지 작성일·수정일 표시 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/15dadde0ac1d17ebb7c1713f44fcfcea12dfed59)**
- **원인** : 상세페이지에서 언제 쓰고 고쳤는지 알 수 없다.
- **작업**
  - 추가 : 상세페이지 작성일·수정일
- **설명**
  - `BaseEntity` 의 auditing 필드(`@CreatedDate`, `@LastModifiedDate`)를 `#temporals.format` 으로 출력.

### 029-1 글 수정 폼 추가 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/140f076c79c682de78e8917da71e56806dd24da7)**
- **원인** : 상세의 "글 수정" 링크(`/posts/{id}/modify`)가 갈 곳이 없다.
- **작업**
  - 추가 : `PostController.ModifyForm`, `showModify()`(`GET /posts/{id}/modify`), `modify.html`
- **설명**
  - `@ModelAttribute("form") ModifyForm form` 으로 빈 폼 객체를 모델에 올린 뒤 `form.setTitle(post.getTitle())` 처럼 값을 채운다. 템플릿은 write.html 과 같은 구조에 글 ID·작성일·수정일 표시, 폼 에러 프래그먼트 재사용. (content 최대 100자 — 결정 사항 9)

### 029-2 글 수정 처리 추가 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/cd803534fc4b00d1136ebd82e3348b5faaed2c67)**
- **원인** : 수정 폼을 전송해도 처리하지 않는다.
- **작업**
  - 추가 : `PostController.modify()`(`POST /posts/{id}/modify`), `PostService.modify()`, `Post.modify()`
- **설명**
  - `postService.modify(post, title, content)` 는 엔티티 필드만 바꾸고 `save` 를 호출하지 않는다. `@Transactional` 안에서 조회한 영속 엔티티는 트랜잭션 커밋 때 **변경 감지(dirty checking)** 로 UPDATE 가 자동 실행된다. 이때 `@LastModifiedDate` 로 수정일도 갱신된다.

### 029-A 글 삭제 `나아가기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/e373eebcb4f21affa7074737e34b1d7501ce2b5f)**
- **원인** : 글 작성·조회·수정은 되지만 삭제가 없다.
- **작업**
  - 추가 : `PostController.delete()`(`POST /posts/{id}/delete`), `PostService.delete()`, 상세페이지 글 삭제 버튼, `PostControllerTest`
- **설명**
  - 삭제는 서버 상태를 바꾸므로 링크(GET)가 아니라 `<form method="POST">` 로 보낸다. `onsubmit="return confirm(...)"` 으로 취소하면 전송되지 않는다.
  - `th:action="@{/posts/{id}/delete(id=${post.id})}"` — 링크 표현식으로 주소를 만든다(시큐리티 도입 후 CSRF 토큰 자동 삽입에도 유리).
  - `PostService.delete(post)` → `postRepository.delete(post)`.
  - 테스트 `PostControllerTest.t1` : MockMvc 로 삭제 요청 후 리다이렉트 주소와 실제 삭제 여부 확인.

### 029-B 목록 최신순 정렬과 페이징 `나아가기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/903149a813ca4399e87cf506168c266ea2d3f5f0)**
- **원인** : 목록이 오래된 글부터 나오고, 글이 많아지면 한 화면에 전부 출력된다.
- **작업**
  - 추가 : `PostService.getPage(page)`, `PAGE_SIZE`, 목록 페이지 번호 링크
  - 수정 : `showList()`
    - 이전 : `showList(Model)` — `findAll()` 전체 목록
    - 이후 : `showList(Model, page)` — `getPage(page)` 최신순 10개씩
- **설명**
  - `PostService.getPage(page)` : `PageRequest.of(page - 1, 10, Sort.by(Sort.Order.desc("id")))` 로 `postRepository.findAll(pageable)` 호출. JPA 가 `LIMIT/OFFSET` 쿼리와 전체 개수 `COUNT` 쿼리를 함께 실행한다. 사용자에게 보이는 page 는 1부터, 스프링 Data 는 0부터라서 변환한다.
  - 리턴 타입 `Page<Post>` 는 `Iterable` 이라 기존 `th:each="post : ${posts}"` 를 그대로 쓴다. `totalPages`, `number`, `hasPrevious()`, `hasNext()` 로 페이지 링크를 그린다.
  - `th:with` 로 `currentPage/startPage/endPage` 계산, `T(java.lang.Math)` 로 정적 메서드 호출, `#numbers.sequence(start, end)` 로 번호 반복.
  - 테스트 `t2` : 글 25개 생성 후 1페이지 첫 글이 최신글인지, 2페이지에 해당 글과 현재 페이지 표시가 있는지 확인.

### 029-C 없는 글 404 처리 `나아가기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/9c8117de505f15231dd8c3c6d73302092beda39d)**
- **원인** : `/posts/999` 처럼 없는 글에 접근하면 `findById(id).get()` 에서 `NoSuchElementException` 이 터져 **500 서버 에러**가 난다. 서버 잘못이 아니라 "없는 자원"이므로 404 가 맞다.
- **작업**
  - 추가 : `GlobalExceptionHandler`(`NoSuchElementException` → 404), `templates/global/notFound.html`
- **설명**
  - `@ControllerAdvice` 클래스의 `@ExceptionHandler(NoSuchElementException.class)` 는 **모든 컨트롤러**에서 발생한 해당 예외를 가로챈다. `@ResponseStatus(HttpStatus.NOT_FOUND)` 로 상태 코드 지정, 리턴값은 뷰 이름.
  - 컨트롤러마다 `orElseThrow` 로 고치지 않고 한 곳에서 처리하므로, 앞으로 추가될 댓글 등 다른 `findById(...).get()` 에도 자동 적용된다.
  - 테스트 `t3` : 없는 글의 상세·수정 폼·삭제 요청이 모두 404 인지 확인.

### 030-1 댓글 엔티티와 샘플 댓글 추가 (저장 실패) `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/53844ec7b68e6b542e3adb2ee515f57e2d4b33dd)**
- **원인** : 글에 의견을 남길 수 없다.
- **작업**
  - 추가 : `PostComment` 엔티티, `Post.comments`(`@OneToMany`), `Post.addComment()`, 샘플 댓글 5개
- **설명**
  - 연관관계의 주인은 FK(`POST_ID`)를 가진 `PostComment.post`. `Post.comments` 는 `mappedBy` 로 읽기 전용 거울.
  - `addComment` 는 댓글을 만들어 리스트에 넣기만 한다. `PERSIST` cascade 로 글이 영속 상태일 때 댓글도 함께 저장되고, `REMOVE` 로 글 삭제시 댓글도 삭제된다(029-A 글 삭제와도 맞물림).
  - 의도된 실패 단계 : `ApplicationRunner` 람다에는 트랜잭션이 없어 `write()` 직후 글이 준영속 상태가 되므로, 이후 `addComment` 한 댓글은 **저장되지 않는다**.

### 030-2 샘플 데이터 트랜잭션 적용 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/634ed180487a17518f975dbe64cd36d0b0f803ff)**
- **원인** : 샘플 댓글이 DB 에 들어가지 않는다.
- **작업**
  - 추가 : `BaseInitData.self`(`@Autowired @Lazy`), `BaseInitData.work1()`(`@Transactional`)
  - 수정 : 샘플 데이터 생성 위치
    - 이전 : `ApplicationRunner` 람다 안
    - 이후 : `self.work1()` 호출
- **설명**
  - `@Transactional` 은 스프링 **프록시**를 거쳐 호출될 때만 적용된다. 같은 객체 안에서 `this.work1()` 로 부르면 프록시를 우회하므로, 자기 자신의 프록시(`self`)를 주입받아 호출한다. `@Lazy` 는 자기 참조 순환 의존을 지연 주입으로 풀어준다. 하나의 트랜잭션 안에서 글이 영속 상태로 유지되어 커밋 때 댓글까지 저장된다.

### 031 댓글 목록 표시 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/0288f44cd399c1d7bde265b0cb36f23bbb2577da)**
- **원인** : 댓글이 저장돼도 화면에 보이지 않는다.
- **작업**
  - 추가 : 상세페이지 댓글 목록
- **설명**
  - `th:each="postComment : ${post.comments.reversed}"` — 최신 댓글이 위로. `reversed` 는 Java 21 `List.reversed()`(SequencedCollection). `comments` 는 LAZY 컬렉션이지만 `showDetail` 이 `@Transactional(readOnly = true)` 이고 OSIV(open-in-view) 기본 활성이라 뷰 렌더링 중 조회된다.

### 032-1 댓글 작성 폼 추가 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/7eb3a4d767f018ac634816b857e68f52e5cbb71c)**
- **원인** : 댓글을 입력할 곳이 없다.
- **작업**
  - 추가 : 상세페이지 댓글 작성 폼(`POST /posts/{id}/comments/write`)
- **설명**
  - 처리 메서드는 033 에서 만든다. 지금은 화면만.

### 032-2 댓글 폼 기본 전송 차단 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/f7088251f3e3a253eb86d76f8afba1e3ae2f332d)**
- **원인** : 서버에 보내기 전에 프론트에서 먼저 검사하고 싶다. 그러려면 기본 전송을 막아야 한다.
- **작업**
  - 추가 : 댓글 폼 `onsubmit="return false;"`
- **설명**
  - `onsubmit` 핸들러가 `false` 를 리턴하면 브라우저 기본 동작(전송)이 취소된다. 지금은 아무리 눌러도 전송되지 않는다.

### 032-3 댓글 폼 전송 함수 추가 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/ff4b006233d57eeedbd66b62e41e7f0b3da0ef1a)**
- **원인** : 전송을 막았으니 대신 실행될 함수가 필요하다.
- **작업**
  - 추가 : `PostComment__submitWriteForm(form)`(앞뒤 공백 제거)
  - 수정 : 댓글 폼 onsubmit
    - 이전 : `onsubmit="return false;"`
    - 이후 : `onsubmit="PostComment__submitWriteForm(this); return false;"`
- **설명**
  - `this` 는 폼 엘리먼트. `form.content` 처럼 name 으로 필드에 접근한다. 함수 이름의 `도메인__동작` 접두어는 전역 함수 이름 충돌을 피하는 관례.

### 032-4 댓글 프론트 유효성 검사 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/1fe410af4f8bfefbda4baa2257b4084d3050a6ef)**
- **원인** : 함수가 검사만 하고 전송은 하지 않는다.
- **작업**
  - 추가 : 댓글 빈 값·2자 미만 검사, `form.submit()`, textarea `maxlength="100"`
- **설명**
  - `form.submit()` 은 `onsubmit` 을 다시 발생시키지 않고 바로 전송한다. 최대 길이는 `maxlength` 속성이 입력 자체를 막는다. 프론트 검사는 UX 용이고 서버 검사도 반드시 있어야 한다(033).

### 033 댓글 작성 처리 추가 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/50788d9d0bf5a5cf3218c9770844dd7a38b6a4c0)**
- **원인** : 댓글 폼을 전송해도 받을 곳이 없다.
- **작업**
  - 추가 : `PostCommentController.write()`(`POST /posts/{postId}/comments/write`), 댓글 `WriteForm`, `PostService.writeComment()`
- **설명**
  - 댓글은 글(Post)을 통해 추가한다(`post.addComment`). 트랜잭션 안에서 조회한 영속 상태의 글이므로 `PERSIST` cascade 로 커밋 때 댓글이 INSERT 된다. 프론트에서 이미 검사했으므로 서버는 `@Valid` 만 두고 실패시 400(비정상 요청으로 간주).

### 034-1 댓글 삭제 추가 (삭제 실패) `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/0b61934d17e79d17fb14e33d7ceeb802de1b0ba8)**
- **원인** : 잘못 쓴 댓글을 지울 수 없다.
- **작업**
  - 추가 : `PostCommentController.delete()`(`GET .../comments/{id}/delete`), `Post.findCommentById()`, `Post.deleteComment()`, `PostService.deleteComment()`, 댓글 수정·삭제 링크
- **설명**
  - 댓글은 글을 통해 다룬다 — 글의 `comments` 리스트에서 찾아 `remove` 한다. 의도된 실패 단계 : 컬렉션에서 빼기만 해서는 JPA 가 DELETE 를 실행하지 않는다(연관관계 주인은 `PostComment.post` 쪽이므로).

### 034-2 고아 객체 제거로 댓글 삭제 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/4b5b6275bc7673d20c210520477e5c50d9acbe80)**
- **원인** : 리스트에서 뺀 댓글이 DB 에 그대로 남는다.
- **작업**
  - 수정 : `Post.comments` 매핑
    - 이전 : `@OneToMany(mappedBy = "post", cascade = {PERSIST, REMOVE})`
    - 이후 : `@OneToMany(..., orphanRemoval = true)`
- **설명**
  - 부모 컬렉션에서 제거되어 고아가 된 자식 엔티티를 JPA 가 자동으로 DELETE 한다. 객체 세계의 "리스트에서 제거" 가 DB 의 "삭제" 와 일치하게 된다.

### 034-3 댓글 삭제 링크 동작 차단 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/8fe818eec5684899b53939d5b53e52eb617c1f32)**
- **원인** : 삭제 링크를 실수로 누르면 바로 지워진다.
- **작업**
  - 추가 : 댓글 삭제 링크 `onclick="return false;"`
- **설명**
  - 링크의 click 핸들러가 `false` 를 리턴하면 페이지 이동(기본 동작)이 취소된다.

### 034-4 댓글 삭제 확인창 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/d88d7117b468c2e04cb6edbb61f6745f969883f2)**
- **원인** : 막기만 하면 삭제를 할 수 없다.
- **작업**
  - 수정 : 댓글 삭제 링크 onclick
    - 이전 : `return false;`
    - 이후 : `return confirm('정말 삭제하시겠습니까?');`
- **설명**
  - `confirm()` 은 확인이면 `true`, 취소면 `false` 를 리턴하므로 확인을 누른 경우에만 이동한다.

### 034-5 엔티티 동등성 비교 기준 변경 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/27e3a7a60bc80a59a0a18cf46037dd97f6aeba92)**
- **원인** : `comments.remove(postComment)` 는 `equals` 로 대상을 찾는다. 기본 `equals` 는 같은 인스턴스만 같다고 보므로, 다른 경로로 조회한 같은 댓글은 지워지지 않을 수 있다.
- **작업**
  - 추가 : `BaseEntity.equals()`(클래스·id 비교), `BaseEntity.hashCode()`
- **설명**
  - 엔티티의 동일성 기준은 식별자(id). `equals` 를 바꾸면 `hashCode` 도 함께 바꿔야 `HashSet`/`HashMap` 에서 일관되게 동작한다.

### 035 엔티티 동일성 빠른 비교 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/985086c0ca3c695f517ef95f1eb2b118678168a3)**
- **원인** : 같은 인스턴스끼리 비교할 때도 클래스·id 비교를 거친다.
- **작업**
  - 수정 : `BaseEntity.equals()`
    - 이전 : 클래스·id 비교
    - 이후 : `o == this` 이면 바로 `true`, 아니면 클래스·id 비교
- **설명**
  - 참조가 같으면(동일성) 당연히 동등하므로 바로 리턴하는 빠른 경로.

### 036-1 댓글 수정 추가 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/545e2de9aae77560c19230228869648fd0419321)**
- **원인** : 댓글의 "수정" 링크가 갈 곳이 없다.
- **작업**
  - 추가 : 댓글 `ModifyForm`, `PostCommentController.showModify()`·`modify()`(`/posts/{postId}/comments/{id}/modify`), `PostComment.modify()`, `PostService.modifyComment()`, `post/postComment/modify.html`
- **설명**
  - 글 수정과 같은 흐름. 수정은 변경 감지로 UPDATE 되고, 처리 후 글 상세로 리다이렉트.

### 036-2 댓글 수정 프론트 유효성 검사 `복습하기`

> 이 일지는 036-2 커밋에서 누락되어 036-A 커밋에 함께 추가했다.

- **[링크](https://github.com/bin0421/NewSBB-1/commit/2a80bc077f3159ca2a31886f04602a187fc91253)**
- **원인** : 댓글 수정 폼에는 작성 폼(032-4) 같은 프론트 검사가 없다.
- **작업**
  - 추가 : `PostComment__submitModifyForm(form)`(빈 값·2자 미만 검사)
  - 수정 : 댓글 수정 폼
    - 이전 : `<form method="POST">`
    - 이후 : `<form method="POST" onsubmit="PostComment__submitModifyForm(this); return false;">`
- **설명**
  - 032 의 작성 폼과 동일한 패턴(`onsubmit="...; return false;"`).

### 036-A 목록 댓글 수 표시 `나아가기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/3c9de80941646d8458bb46c1f02baa617a30f233)**
- **원인** : 목록에서는 어떤 글에 댓글이 달렸는지 알 수 없다.
- **작업**
  - 추가 : 목록 제목 옆 댓글 수, `hibernate.default_batch_fetch_size: 100`, 테스트 `t4`·`t5`
- **설명**
  - `post.comments.size()` 로 개수를 세고, 댓글이 없으면 `th:unless` 로 숨긴다.
  - `comments` 는 LAZY 컬렉션이라 글마다 댓글 조회 쿼리가 따로 나가는 **N+1 문제**가 생긴다. `hibernate.default_batch_fetch_size: 100` 을 설정해 한 페이지 글들의 댓글을 `IN (...)` 쿼리 한 번으로 모아서 가져오게 했다.
  - 테스트 `t4` : 댓글 4개 단 글이 목록에 `[4]` 로 보이는지 확인.

### 036-B 없는 댓글 404 확인 `나아가기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/c2d7f6f63e225df0bcbe0fd76b6305c49ae8ae1e)**
- **원인** : 029-C 의 전역 404 처리가 댓글에도 적용되는지 보장되지 않았고, 404 페이지 문구는 "글"만 언급한다.
- **작업**
  - 수정 : 404 안내 문구
    - 이전 : "삭제되었거나 존재하지 않는 글입니다."
    - 이후 : "삭제되었거나 존재하지 않는 글 또는 댓글입니다."
- **설명**
  - 댓글 컨트롤러도 `post.findCommentById(id).get()` 을 쓰므로 `NoSuchElementException` 이 발생 → `GlobalExceptionHandler` 가 404 로 바꾼다. 전역 처리 덕분에 댓글 쪽 코드는 고치지 않았다.
  - 수정 요청은 `_method=PUT` 파라미터를 함께 보내 038 이후(HiddenHttpMethodFilter)에도 그대로 통과하게 했다.
  - ⚠️ 테스트 `t5` 코드는 실수로 036-A 커밋에 먼저 포함되었다.

### 037 폼 클래스의 record 전환 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/223c162f8619ca28ec759cb5dec0496324618fe7)**
- **원인** : 폼 클래스마다 `@AllArgsConstructor`, `@Getter`, `@Setter`, private 필드 선언이 반복된다.
- **작업**
  - 수정 : 글·댓글의 `WriteForm`, `ModifyForm`
    - 이전 : `@AllArgsConstructor @Getter (@Setter) public static class`, `form.getTitle()`
    - 이후 : `public record`, `form.title`
  - 수정 : `showModify()` 의 폼 초기값
    - 이전 : `@ModelAttribute("form") ModifyForm form` + `form.setTitle(...)`
    - 이후 : `model.addAttribute("form", new ModifyForm(post.getTitle(), post.getContent()))`
- **설명**
  - `record` 는 불변 데이터 클래스로, 생성자·접근자(`title()`)·`equals/hashCode/toString` 을 자동 생성한다. 스프링은 record 를 생성자 바인딩으로 채우고, 제약 어노테이션은 컴포넌트에 붙인다. 같은 클래스(컨트롤러) 안의 중첩 record 라 private 필드 `form.title` 에도 직접 접근한다. 불변이므로 `@Setter` 대신 생성자로 값을 채운다.

### 038 수정 요청의 PUT 전환 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/b460d765bc54a09ad8ed09ce5b715f4009dd0153)**
- **원인** : 수정 처리가 `POST` 다. 의미상 수정은 `PUT` 이지만 HTML 폼은 GET/POST 만 보낼 수 있다.
- **작업**
  - 추가 : `spring.mvc.hiddenmethod.filter.enabled: true`, 수정 폼의 `<input type="hidden" name="_method" value="PUT">`
  - 수정 : 글·댓글 수정 처리 매핑
    - 이전 : `@PostMapping(".../modify")`
    - 이후 : `@PutMapping(".../modify")`
- **설명**
  - `HiddenHttpMethodFilter` 가 POST 요청의 `_method` 파라미터를 보고 요청 메서드를 PUT/DELETE 등으로 바꿔 디스패치한다. 같은 URL 을 `GET`(폼) / `PUT`(처리)으로 구분한다.

### 039-1 공통 레이아웃과 공통 스크립트 도입 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/3fe68b9d68a82eb832f614091798b011878e3d55)**
- **원인** : 모든 템플릿에 `<h1 th:text="${siteName}">` 를 반복하고, `<html>/<head>` 같은 공통 뼈대가 없다. 공통 스크립트를 넣을 곳도 없다.
- **작업**
  - 추가 : `thymeleaf-layout-dialect` 의존성, `templates/layout.html`, `static/global.js`
  - 수정 : 모든 페이지 템플릿
    - 이전 : `<h1 th:text="${siteName}">` + 본문
    - 이후 : `<html layout:decorate="~{layout}">` + `<div layout:fragment="content">`
  - 삭제 : `PostController.siteName()`
- **설명**
  - 레이아웃 다이얼렉트는 각 페이지(content)가 레이아웃을 "꾸며 쓰는(decorate)" 방식이다. 레이아웃의 `layout:fragment="content"` 자리에 페이지의 같은 이름 프래그먼트가 들어간다.
  - `global.js` : `<a method="DELETE" href="...">` 처럼 `method` 속성이 있는 링크를 클릭하면 숨은 POST 폼(+`_method`, CSRF 토큰, 쿼리 파라미터)을 만들어 전송한다. 링크로도 DELETE/PUT 요청을 보낼 수 있게 해 주는 공통 스크립트. 기존 `onclick`(confirm)은 `data-onclick` 으로 옮겨 먼저 실행한다.
  - 나아가기에서 추가한 글 삭제 폼(029-A)과 페이징(029-B)도 `content` 프래그먼트 안으로 옮겼다.

### 039-2 댓글 삭제 요청의 DELETE 전환 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/ced1805670c979e618beab0b923b55921fa9b09b)**
- **원인** : 댓글 삭제가 `GET` 링크다. 링크 미리 불러오기나 크롤러가 방문만 해도 삭제될 수 있고, 의미상으로도 삭제는 `DELETE` 다.
- **작업**
  - 수정 : 댓글 삭제
    - 이전 : `@GetMapping(".../delete")`, 일반 링크
    - 이후 : `@DeleteMapping(".../delete")`, `<a method="DELETE" ...>`
  - 수정 : 테스트 `t5` 의 삭제 요청
    - 이전 : `get(.../delete)`
    - 이후 : `post(.../delete).param("_method", "DELETE")`
- **설명**
  - 039-1 의 `global.js` 가 `a[method]` 클릭을 가로채 `_method=DELETE` 를 담은 POST 폼으로 전송하고, `HiddenHttpMethodFilter` 가 DELETE 로 바꿔 매핑한다. confirm 은 `data-onclick` 으로 옮겨져 먼저 실행된다.
  - 이제 같은 주소를 GET 으로 요청하면 405 가 된다.

### 039-3 템플릿 들여쓰기 정리 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/bb60cec01010f580652024c66fcd7c384a2100fb)**
- **원인** : 039-1 에서 `layout:fragment` 로 감싼 뒤 `detail.html` 내용의 들여쓰기가 감싼 구조와 맞지 않아 읽기 어렵다.
- **작업**
  - 수정 : 레이아웃 적용 템플릿 들여쓰기
    - 이전 : fragment 안 내용이 들여쓰기 없음
    - 이후 : fragment 안쪽으로 한 단계 들여쓰기
- **설명**
  - 강의 페이지에는 039-2 와 같은 제목("댓글 삭제에 DELETE MAPPING 도입")으로 올라와 있지만, 실제 변경은 공백뿐이다(`git diff -w` 결과 없음). 나아가기의 글 삭제 폼도 같은 깊이로 맞췄다.

### 040 페이지별 탭 제목 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/e8f9df80182da6407d172cc2e43b261776c9be74)**
- **원인** : 브라우저 탭 제목이 모든 페이지에서 "커뮤니티 사이트 A" 로 같다.
- **작업**
  - 추가 : `<title layout:title-pattern="$CONTENT_TITLE | $LAYOUT_TITLE">`, 페이지별 `<head><title>`
  - 수정 : `list.html`
    - 이전 : 병합 충돌 표시(`<<<<<<<`)가 남은 상태
    - 이후 : 정상 템플릿
- **설명**
  - 레이아웃 다이얼렉트가 페이지의 title(`$CONTENT_TITLE`)과 레이아웃의 title(`$LAYOUT_TITLE`)을 패턴대로 합친다. 페이지 title 에도 `th:text` 로 글 번호·제목을 넣을 수 있다.
- ⚠️ **바로잡기** : 039-3 커밋에서 병합 충돌을 정리한 `list.html` 이 저장되지 않아 충돌 표시(`<<<<<<<`)가 남은 채 커밋되었다. 이 커밋에서 정상 내용으로 고쳤다. 이후 재발하지 않도록 커밋 전에 충돌 표시를 검사한다.

### 041-1 스프링 시큐리티 도입 (전체 차단) `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/c8691c363ebbe56f9a22e94f523783b0b9108adf)**
- **원인** : 앞으로 회원·권한 기능을 넣으려면 인증/인가 기반이 필요하다.
- **작업**
  - 추가 : `spring-boot-starter-security`, `spring-boot-starter-security-test` 의존성, 테스트의 `@WithMockUser`
  - 수정 : 테스트의 변경 요청
    - 이전 : `post(...)`
    - 이후 : `post(...).with(csrf())`
- **설명**
  - 의존성만 추가해도 시큐리티 자동 설정이 켜져 **모든 요청이 인증 필요**가 된다. 브라우저로 접속하면 기본 로그인 페이지로 이동하고, 콘솔에 임시 비밀번호(사용자 `user`)가 출력된다. → 의도된 "모든 요청 막힘" 단계.
  - 기본으로 CSRF 보호도 켜져 POST/PUT/DELETE 요청에 CSRF 토큰이 필요해진다.
  - 나아가기 테스트도 이 규칙을 그대로 받으므로, 인증된 사용자와 CSRF 토큰을 흉내 내야 통과한다.

### 041-2 시큐리티 전체 허용과 정적 파일 경로 정리 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/6a1b1a77ccf4e4d6a60b3ec4210b41c0e8e315de)**
- **원인** : 회원 기능이 아직 없는데 모든 페이지가 막혀 있다.
- **작업**
  - 추가 : `SecurityConfig`(`SecurityFilterChain` — 전체 허용, H2 콘솔 frame·CSRF 예외)
  - 수정 : 공통 스크립트 경로
    - 이전 : `static/global.js`, `<script src="/global.js">`
    - 이후 : `static/resource/global.js`, `<script src="/resource/global.js">`
- **설명**
  - `authorizeHttpRequests` 는 위에서부터 첫 번째로 맞는 규칙이 적용된다. 지금은 `/**` 가 전부 허용하고, 나중에 회원 기능이 생기면 이 줄을 좁힌다.
  - 정적 파일을 `/resource/**` 아래로 모아두면 나중에 "정적 파일은 항상 허용" 규칙 하나로 관리할 수 있다.
  - H2 콘솔은 iframe 을 쓰므로 같은 출처 frame 을 허용하고, 자체 폼에 CSRF 토큰이 없으므로 CSRF 검사에서 제외한다.
  - 요청은 허용되지만 CSRF 는 여전히 켜져 있어, 토큰 없는 POST(글 작성 등)는 403 이 된다 → 041-3, 041-4 에서 해결.

### 041-3 폼 CSRF 토큰 추가 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/0db0ff8b7fa1d6865df6042c6c1086f6ef471ea2)**
- **원인** : 글 작성·수정, 댓글 수정 폼이 CSRF 토큰 없이 전송되어 403 이 난다.
- **작업**
  - 추가 : 글 작성·수정, 댓글 작성·수정 폼의 `<input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}">`
- **설명**
  - CSRF(사이트 간 요청 위조) 방어 — 서버가 발급한 토큰을 폼에 숨겨 두고, 요청에 같은 토큰이 있어야 처리한다. 다른 사이트가 몰래 만든 폼은 이 토큰을 알 수 없다. 시큐리티가 모델에 `_csrf` 객체를 넣어 준다. (`th:action` 을 이미 쓰고 있던 댓글 작성 폼과 나아가기의 글 삭제 폼에는 토큰이 자동으로 들어가 있었다)

### 041-4 CSRF 토큰 자동 삽입 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/549ce3077e1de8fd41880465d7ebded35331c2ac)**
- **원인** : 폼마다 hidden input 을 직접 쓰는 것은 번거롭고 빠뜨리기 쉽다.
- **작업**
  - 수정 : CSRF 토큰 삽입 방식
    - 이전 : 폼마다 hidden input 직접 작성
    - 이후 : `<form method="POST" th:action ...>` 자동 삽입
  - 삭제 : CSRF hidden input
- **설명**
  - 타임리프 + 시큐리티 연동은 `th:action` 이 있는 POST 폼에 CSRF hidden input 을 **자동으로 삽입**한다. 값 없는 `th:action` 은 현재 주소를 action 으로 쓴다.

### 041-5 스크립트 요청 CSRF 토큰 제공 `복습하기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/d4ea4123411250d9102e5159ab8d26fe5bdf92a9)**
- **원인** : 댓글 삭제(`a[method=DELETE]`)는 `global.js` 가 즉석에서 폼을 만들어 보내는데, 이 폼에는 타임리프가 토큰을 넣어줄 수 없어 403 이 난다.
- **작업**
  - 추가 : 레이아웃 head 의 `<meta name="_csrf" th:content="${_csrf.token}">`
- **설명**
  - `global.js` 는 이미 `meta[name="_csrf"]` 의 값을 읽어 `_csrf` hidden input 으로 넣도록 작성되어 있었다(039-1). 레이아웃에 메타 태그를 두면 모든 페이지의 자바스크립트 요청이 토큰을 쓸 수 있다.

### 041-A 회원가입 `나아가기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/2c102f2416009298917f2d59845c4ae04c3ddd61)**
- **원인** : 시큐리티는 켰지만 회원이 없다. 작성자·권한 기능의 출발점이 필요하다.
- **작업**
  - 추가 : `Member` 엔티티, `MemberRepository`, `MemberService`(`join()` 등), `MemberController`(`JoinForm`, `GET·POST /members/join`), `join.html`, `PasswordEncoder` 빈, 샘플 회원(`BaseInitData.work2()`), 헤더 메뉴, `MemberControllerTest`
- **설명**
  - 패키지는 글과 같은 규칙으로 `domain.member.member.{entity,repository,service,controller}`.
  - 비밀번호는 `PasswordEncoder`(`BCryptPasswordEncoder` 빈)로 **암호화해서** 저장한다. BCrypt 는 매번 다른 salt 를 써서 같은 비밀번호도 결과가 다르고, 비교는 `passwordEncoder.matches()` 로 한다.
  - `JoinForm` record 는 기존 폼과 같은 메세지 형식(`순번-필드-메세지`)을 써서 `form_errors` 프래그먼트(정렬·첫 에러 필드 포커스)를 그대로 재사용한다.
  - 비밀번호 확인 불일치, 아이디·이메일 중복처럼 어노테이션으로 표현하기 어려운 검사는 `bindingResult.rejectValue(필드, 코드, "06-passwordConfirm-...")` 로 같은 형식의 에러를 직접 추가한다.
  - 샘플 회원은 `BaseInitData.work2()` 에서 회원이 없을 때만 생성(기존 DB 에 글이 있어도 회원은 생성되도록 글과 분리).
  - 테스트 `MemberControllerTest` : 가입 성공시 암호화 저장, 비밀번호 불일치·중복 아이디 에러 메세지 확인.

### 041-B 로그인·로그아웃 `나아가기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/20263eba8ef7afde04bb4c34deeedca7927eee5e)**
- **원인** : 회원가입은 되지만 로그인할 방법이 없다.
- **작업**
  - 추가 : `CustomUserDetailsService`, `GlobalModelAttributes`(`currentUsername`), `MemberController.showLogin()`, `login.html`, `formLogin`·`logout` 설정
  - 수정 : 헤더 메뉴
    - 이전 : 글 목록, 회원가입
    - 이후 : 비로그인은 로그인·회원가입, 로그인은 `아이디님`·로그아웃
  - 수정 : 회원가입 성공시 이동
    - 이전 : `redirect:/posts`
    - 이후 : `redirect:/members/login`
- **설명**
  - `CustomUserDetailsService implements UserDetailsService` : 시큐리티가 로그인 처리 중 `loadUserByUsername` 을 호출해 회원을 조회한다. 리턴한 `User`(아이디, 암호화된 비밀번호, 권한 `ROLE_MEMBER`)의 비밀번호를 `PasswordEncoder` 로 비교한다. 비밀번호 비교는 시큐리티가 하고, 우리는 조회만 한다.
  - 로그인 POST 처리 메서드는 직접 만들지 않는다. `formLogin().loginPage("/members/login")` 이 같은 주소의 POST 를 필터에서 처리한다. 폼 필드 이름은 기본값 `username`, `password`.
  - 로그아웃은 CSRF 보호 때문에 POST 로만 동작한다 → 헤더에 `th:action` 폼 버튼(토큰 자동 삽입).
  - `GlobalModelAttributes`(`@ControllerAdvice`) 의 `@ModelAttribute("currentUsername")` : 016 에서 배운 공통 변수를 **모든 컨트롤러**에 적용. 파라미터 `Principal` 로 로그인 사용자를 받는다.
  - 테스트 : `formLogin()` 으로 로그인 성공/실패, `user("user1")` 요청 후처리기로 헤더 메뉴 확인.

### 042-A 글·댓글 작성자 저장과 표시 `나아가기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/28cedc84b158286d5dff5bd57172da5c3b6a24a4)**
- **원인** : 로그인은 되지만 누가 쓴 글·댓글인지 남지 않는다.
- **작업**
  - 추가 : `Post.author`, `PostComment.author`, `Rq`(현재 회원), 목록·상세·댓글 작성자 표시
  - 수정 : 작성 메서드 시그니처
    - 이전 : `write(title, content)`, `writeComment(post, content)`, `addComment(content)`
    - 이후 : `write(author, title, content)`, `writeComment(post, author, content)`, `addComment(author, content)`
  - 수정 : 샘플 데이터
    - 이전 : 글 생성(`work1`) → 회원 생성(`work2`), 작성자 없음
    - 이후 : 회원 생성 → 글 생성, 작성자 user1·user2
- **설명**
  - `Rq`(`@RequestScope` 빈) : `SecurityContextHolder` 의 인증 정보로 현재 회원(actor)을 조회해 돌려준다. 요청마다 새로 만들어지는 빈이라 한 요청 안에서는 조회 결과를 재사용한다. 컨트롤러는 `rq.getActor()` 만 호출하면 된다.
  - 기존 DB 의 글에는 작성자가 없으므로(`ddl-auto: update` 로 `author_id` 컬럼만 추가됨) 템플릿에서 `${post.author?.username ?: '(알 수 없음)'}` 처럼 안전 탐색(`?.`)과 엘비스(`?:`) 연산자로 처리한다.
  - 글을 만들 때 작성자가 있어야 하므로 샘플 회원을 먼저 만든다.
  - 테스트 `t6` : `user1` 로 글·댓글 작성 후 작성자 저장과 상세페이지 표시 확인. 기존 테스트도 `@WithMockUser(username = "user1")` 로 실제 샘플 회원을 쓰게 했다.

### 042-B 글·댓글 작성 로그인 필수화 `나아가기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/abaaf91ab933b3af83ec2a04e05cf0fa517e3aea)**
- **원인** : 로그인하지 않아도 글·댓글을 쓸 수 있어 작성자 없는 글이 계속 생긴다.
- **작업**
  - 추가 : `/posts/write`, `/posts/*/comments/write` 의 `authenticated()`, 상세페이지 비로그인 안내
  - 수정 : 댓글 작성 폼 표시
    - 이전 : 항상 표시
    - 이후 : 로그인한 경우에만 표시
- **설명**
  - `authorizeHttpRequests` 는 위에서부터 처음 맞는 규칙이 적용되므로, 041-2 의 `/**` permitAll 보다 **위에** `.requestMatchers("/posts/write").authenticated()` 를 둔다. `*` 는 경로 한 칸(글 번호)에 대응.
  - 인증이 필요한데 비로그인이면 시큐리티가 요청을 저장(RequestCache)해 두고 `loginPage` 로 보낸다. 로그인 성공 후에는 저장된 요청으로 돌아간다.
  - 테스트 `t7`(`@WithAnonymousUser`) : 글쓰기·댓글쓰기 요청이 로그인 페이지로 리다이렉트되고 상세페이지에 안내가 보이는지 확인.

### 043-A 작성자 본인만 수정·삭제 허용 `나아가기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/effa10316bff980c553d2ec0e454f1a6d5a294cb)**
- **원인** : 로그인만 하면 남의 글·댓글도 주소만 알면 수정·삭제할 수 있다.
- **작업**
  - 추가 : `Post`·`PostComment` 의 `checkActorCanModify()`·`checkActorCanDelete()`·`isAuthor()`, `GlobalExceptionHandler.handleAccessDenied()`(403), `forbidden.html`, 수정·삭제 주소의 `authenticated()`
  - 수정 : `PostService.writeComment()`
    - 이전 : `post.addComment(...)` 후 리턴 없음
    - 이후 : `addComment` → `postRepository.flush()` → 댓글 리턴
- **설명**
  - 권한 판단은 엔티티에 둔다 : `Post.checkActorCanModify(actor)`, `checkActorCanDelete(actor)`, `PostComment` 도 같은 메서드. 컨트롤러는 조회 직후 한 줄로 호출한다. 규칙이 한곳에 모여 있어 나중에 "관리자는 허용" 같은 변경도 쉽다.
  - 거부는 시큐리티의 `AccessDeniedException` 을 던지고, `GlobalExceptionHandler` 가 403 페이지로 바꾼다(029-C 의 404 처리와 같은 방식).
  - ⚠️ 작성자 비교는 `author.equals(actor)` 대신 **id 비교**로 했다. `author` 는 LAZY 로딩 프록시 객체라 클래스가 `Member` 가 아닌 하이버네이트 프록시이므로, 034-5 의 `getClass()` 기반 `equals` 로는 같은 회원도 다르다고 판단된다. 프록시의 `getId()` 는 DB 조회 없이 동작한다.
  - 비로그인 요청은 시큐리티 규칙에서 먼저 걸러져 로그인 페이지로 가고, 로그인했지만 남의 것이면 컨트롤러에서 403 이 된다.
  - 댓글은 cascade 로 저장되어 원래 트랜잭션 커밋 때 INSERT 되므로, 그전에는 id 가 0 이다. `flush()` 로 INSERT 를 앞당기면 새 댓글 id 를 바로 쓸 수 있다.
  - 테스트 `t8` : user2 의 글·댓글에 user1 이 수정 폼·수정·삭제를 요청하면 모두 403 이고 데이터는 그대로인지 확인.

### 043-B 작성자 본인에게만 수정·삭제 버튼 표시 `나아가기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/66050f4dc5e17462a5953913ee03a1f08bb3d9ad)**
- **원인** : 043-A 로 남의 글은 수정·삭제가 막히지만, 버튼은 누구에게나 보여서 눌러야 403 을 만난다.
- **작업**
  - 수정 : 상세페이지 수정·삭제 버튼
    - 이전 : 누구에게나 표시
    - 이후 : 글은 `isPostAuthor`, 댓글은 작성자 조건이 참일 때만 표시
- **설명**
  - `layout:fragment` 엘리먼트에 `th:with="isPostAuthor = ${currentUsername != null and post.author?.username == currentUsername}"` 로 한 번 계산해 두고 `th:if="${isPostAuthor}"` 로 사용. 댓글은 반복문 안에서 댓글마다 같은 조건을 쓴다.
  - `currentUsername != null` 조건이 꼭 필요하다 : 비로그인(`null`)과 작성자 없는 옛 글(`author?.username` 도 `null`)이 만나면 `null == null` 이 참이 되기 때문.
  - 화면에서 버튼을 숨기는 것은 편의일 뿐이고, 실제 보안은 043-A 의 서버 검사가 담당한다.
  - 테스트 `t9` : 내 글에는 글 수정/삭제 버튼이 있고 남의 댓글 수정 링크는 없으며, 남의 글에서는 반대인지 확인.

### 044-A 글 추천 `나아가기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/69bd62366ad33fcd2cfb048b45f365a0b86036c0)**
- **원인** : 좋은 글을 표시할 방법이 없다.
- **작업**
  - 추가 : `Post.voters`(`@ManyToMany`), `Post.isVotedBy()`·`toggleVote()`, `PostController.vote()`(`POST /posts/{id}/vote`), `PostService.toggleVote()`, 추천 주소 `authenticated()`, 상세 추천 버튼, 목록 추천 수
- **설명**
  - `Post.voters` : `@ManyToMany Set<Member>` — 조인 테이블(`post_voters`)이 생기고, `Set` 이라 같은 회원이 두 번 들어가지 않는다.
  - `toggleVote(member)` : 이미 추천했으면 제거, 아니면 추가. 추천 여부 비교는 043-A 와 같은 이유로 id 비교.
  - 버튼은 039-1 의 `global.js` 를 재사용해 `<a method="POST" href="...">` 로 만들었다 — 링크처럼 보이지만 CSRF 토큰을 담은 POST 로 전송된다.
  - 템플릿에서 현재 회원이 필요해 `${post.isVotedBy(@rq.actor)}` 처럼 **스프링 빈 참조(`@빈이름`)** 를 썼다. 042-A 의 `Rq` 빈을 화면에서도 그대로 쓴다.
  - 테스트 `t10` : 추천 → 1명, 상세에 "추천 취소", 다시 누르면 0명.

### 044-B 댓글 추천 `나아가기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/ddc7d3ae8b108c8528edbb90d064467403c9ab9e)**
- **원인** : 글은 추천할 수 있지만 도움이 된 댓글은 표시할 수 없다.
- **작업**
  - 추가 : `PostComment.voters`, `PostComment.isVotedBy()`·`toggleVote()`, `PostCommentController.vote()`(`POST /posts/{postId}/comments/{id}/vote`), `PostService.toggleCommentVote()`, 댓글 추천 버튼
- **설명**
  - 044-A 와 같은 구조(`PostComment.voters` `@ManyToMany Set<Member>`, `toggleVote`). 댓글도 글을 통해 찾는(`post.findCommentById`) 기존 흐름을 유지했다. 시큐리티 규칙은 044-A 에서 함께 추가해 두었다.
  - 테스트 `t11` : 댓글 추천 → 추천됨, 다시 누르면 취소.

### 045-A 글 검색 `나아가기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/dee4b6fdd269fcd1205093cfb2e99fcadd050d73)**
- **원인** : 글이 많아지면 원하는 글을 찾기 어렵다.
- **작업**
  - 추가 : `PostRepository.search(kw, pageable)`(JPQL), `PostService.getPage(page, kw)`, 목록 검색 폼
  - 수정 : `showList()`
    - 이전 : `showList(Model, page)`
    - 이후 : `showList(Model, page, kw)`, 페이지 링크에 `kw` 유지
- **설명**
  - `PostRepository.search(kw, pageable)` : `@Query` 로 JPQL 작성. `left join p.author a` 로 작성자 없는 옛 글도 포함하고, `:kw = ''` 조건으로 검색어가 없을 때는 전체 목록이 된다. `Pageable` 을 그대로 넘기면 정렬·LIMIT·COUNT 쿼리를 스프링 데이터가 붙여준다.
  - 검색 폼은 조회이므로 `method="GET"` — 검색 결과 주소를 북마크·공유할 수 있다.
  - 페이지 링크 `@{/posts(page=${p}, kw=${kw})}` : 링크 표현식이 한글 검색어를 URL 인코딩해 준다.
  - 테스트 `t12` : 제목/내용/작성자 검색 건수, 화면 결과와 페이지 링크의 검색어(인코딩) 유지 확인.

### 045-B 목록 최신순·추천순 정렬 `나아가기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/b49f1b6364c5ead3d139a2231d12f1016d8ccbc0)**
- **원인** : 044 에서 추천이 생겼지만 목록은 최신순뿐이라 인기 글을 모아 볼 수 없다.
- **작업**
  - 추가 : `PostRepository.searchOrderByVoteCount()`, `PostService.getPage(page, kw, sort)`, 최신순·추천순 전환 링크
  - 수정 : `showList()`
    - 이전 : `showList(Model, page, kw)`
    - 이후 : `showList(Model, page, kw, sort)`, 페이지 링크·검색 폼에 `sort` 유지
- **설명**
  - 최신순은 기존처럼 `Pageable` 의 `Sort.by(desc("id"))`. 추천순은 "컬렉션 크기"로 정렬해야 해서 `Sort` 로 표현할 수 없으므로, JPQL `order by size(p.voters) desc, p.id desc` 를 쓴 별도 쿼리 `searchOrderByVoteCount` 를 만들었다.
  - 직접 `order by` 를 쓴 쿼리는 스프링 데이터가 COUNT 쿼리를 자동으로 잘 만들지 못할 수 있어 `countQuery` 를 명시했다.
  - 검색 폼에 `<input type="hidden" name="sort">` 를 넣어 정렬 상태에서 검색해도 정렬이 유지된다.
  - 테스트 `t13` : 추천 2개 → 1개 → 0개 순서, 최신순 첫 글, 화면의 현재 정렬 표시와 링크 확인.

### 046-A 글·댓글 마크다운 표시 `나아가기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/89960701138d7beb7d16872234b42e901b2faaed)**
- **원인** : 내용이 평범한 텍스트로만 보여서 강조·목록·코드를 표현할 수 없다.
- **작업**
  - 추가 : `commonmark` 의존성, `MarkdownService`(`@Component("markdown")`)
  - 수정 : 상세페이지 글·댓글 내용 출력
    - 이전 : `<div style="white-space: pre-line;">` 일반 텍스트
    - 이후 : `th:with="html = ${@markdown.toHtml(...)}" th:utext="${html}"`
- **설명**
  - `MarkdownService` 는 commonmark 의 `Parser` 로 마크다운을 문서 트리로 읽고 `HtmlRenderer` 로 HTML 을 만든다. 템플릿에서는 빈 이름으로 호출한다(044-A 의 `@빈이름` 참조와 같은 방식). 타임리프 3.1 은 보안상 `th:utext` 안에서 빈 참조 같은 표현식을 금지하므로, 제한이 없는 `th:with` 에서 먼저 계산한 변수를 출력한다.
  - ⚠️ `th:utext` 는 이스케이프하지 않으므로(014-4) 그대로 쓰면 XSS 위험이 있다. 렌더러에 `escapeHtml(true)` 를 줘서 사용자가 입력한 `<script>` 같은 HTML 은 글자로 출력되게 하고, `sanitizeUrls(true)` 로 `javascript:` 링크를 막았다. 마크다운이 만든 태그만 HTML 로 나간다.
  - 026-2 의 `white-space: pre-line` 대신 `softbreak("<br>")` 로 줄바꿈을 처리한다.
  - 테스트 `t14` : 굵게·목록·코드는 태그로, `<script>` 는 `&lt;script&gt;` 로 출력되는지 확인.

### 046-B 댓글 작성·수정 후 해당 댓글로 이동 `나아가기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/8b1265fa9623523240c711e53f792d34954101b6)**
- **원인** : 댓글을 쓰거나 고치면 상세페이지 맨 위로 돌아가서, 댓글이 많으면 내 댓글을 다시 찾아 스크롤해야 한다.
- **작업**
  - 추가 : 댓글 `<li>` 의 `th:id="|postComment-${postComment.id}|"`
  - 수정 : 댓글 작성·수정·추천 후 이동
    - 이전 : `redirect:/posts/{postId}`
    - 이후 : `redirect:/posts/{postId}#postComment-{id}`
- **설명**
  - URL 의 `#id`(fragment)는 서버로 전송되지 않고 브라우저가 같은 id 의 요소로 스크롤한다.
  - 작성 직후 새 댓글의 id 가 필요하다 → 043-A 에서 `writeComment` 가 `flush()` 로 INSERT 를 즉시 실행하게 해 둔 덕분에 리턴받은 댓글의 id 를 바로 쓸 수 있다.
  - 테스트 `t15` : 작성·수정 후 리다이렉트 주소의 앵커, 상세페이지의 `id="postComment-N"` 확인. `t11`(댓글 추천)도 앵커 주소로 맞췄다.

### 047-A 루트 주소 이동과 오류 페이지 레이아웃 `나아가기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/38e76252fc66fa1e573a3edf607fb426ff893d53)**
- **원인** : 사이트 주소(`/`)로 들어오면 아무 페이지도 없어 에러가 난다. 404·403 페이지는 레이아웃 없이 덩그러니 나오고, 없는 주소(`/abc`)는 스프링 기본 에러 페이지가 뜬다.
- **작업**
  - 추가 : `HomeController`(`GET /` → `/posts`), `NoResourceFoundException` 404 처리, 에러 페이지의 `currentUsername`
  - 수정 : 404·403 페이지
    - 이전 : 레이아웃 없는 단독 HTML
    - 이후 : `layout:decorate="~{layout}"` 적용
- **설명**
  - 컨트롤러도 정적 파일도 찾지 못한 요청은 `NoResourceFoundException` 이 발생하므로, `NoSuchElementException` 과 같은 핸들러에서 처리한다.
  - ⚠️ `@ExceptionHandler` 가 만드는 모델에는 `@ControllerAdvice` 의 `@ModelAttribute`(041-B `currentUsername`)가 적용되지 않는다. 그래서 에러 페이지 헤더가 로그인 상태여도 "로그인/회원가입"으로 보였다 → 핸들러에서 `request.getUserPrincipal()` 로 직접 넣었다.
  - 테스트 `t16` : `/` 리다이렉트, 없는 주소 404 + 레이아웃 푸터 + 로그인 사용자 헤더 확인.

### 047-B 글 조회수 `나아가기`

- **[링크](https://github.com/bin0421/NewSBB-1/commit/7bc893ba4f95547e19d2565bb8f24b4fcdc546f1)**
- **원인** : 글이 얼마나 읽혔는지 알 수 없다.
- **작업**
  - 추가 : `Post.viewCount`(`@ColumnDefault("0")`), `Post.increaseViewCount()`, `PostService.increaseViewCount()`, 상세·목록 조회수
  - 수정 : `showDetail()` 트랜잭션
    - 이전 : `@Transactional(readOnly = true)`
    - 이후 : `@Transactional`
- **설명**
  - 상세 조회가 이제 데이터를 바꾸므로 readOnly 를 풀었다. readOnly 트랜잭션에서는 변경 감지가 UPDATE 를 실행하지 않아 조회수가 저장되지 않는다.
  - 기존 DB 에는 글이 이미 있으므로 `ddl-auto: update` 로 NOT NULL 컬럼을 추가하려면 기본값이 필요하다 → 하이버네이트 `@ColumnDefault("0")`.
  - 단순 구현이라 새로고침할 때마다 올라가고, 동시에 많은 요청이 오면 갱신이 겹칠 수 있다(개선 여지: 쿠키/세션으로 중복 방지, `update ... set view_count = view_count + 1` 쿼리).
  - 테스트 `t17` : 두 번 조회하면 2.

---

**나아가기 로드맵(042~047) 완료** — 강사 저장소(041-5)에 회원가입·로그인, 작성자, 권한, 추천, 검색·정렬, 마크다운, 앵커, 페이징, 404/403 처리, 조회수가 더해졌다.
