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
