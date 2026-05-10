# AGENTS.md

이 파일은 AI 에이전트(Claude 등)가 이 레포지토리에서 작업할 때 따라야 할 규칙과 컨텍스트를 정의합니다.

---

## 프로젝트 개요

**명지공방 백엔드 API 서버** — Spring Boot 기반의 REST API.

| 항목 | 내용 |
|---|---|
| 프레임워크 | Spring Boot 4.0.1 |
| Java | 21 |
| 빌드 도구 | Gradle (Groovy DSL), 단일 모듈 |
| 데이터베이스 | MySQL |
| 인증 | JWT (jjwt 0.11.5) + Spring Security |
| 파일 스토리지 | AWS S3 (spring-cloud-aws) |
| 모듈 구조 | 단일 모듈 |

**주요 의존성**
- `spring-boot-starter-web` — REST API
- `spring-boot-starter-data-jpa` — JPA/Hibernate
- `spring-boot-starter-security` — 인증/인가
- `spring-boot-starter-validation` — 입력 검증
- `spring-boot-starter-mail` + `thymeleaf` — 이메일 발송
- `springdoc-openapi-starter-webmvc-ui:2.8.3` — Swagger UI
- `spring-cloud-aws-starter-s3` — S3 파일 업로드/Presign
- `lombok` — 보일러플레이트 코드 제거
- `mysql-connector-j` — MySQL 드라이버

---

## 빌드 / 테스트 / 실행 명령어

```bash
# 전체 빌드
./gradlew build

# 컴파일만 (빠른 확인)
./gradlew compileJava

# 테스트 실행
./gradlew test

# 로컬 실행
./gradlew bootRun

# 빌드 결과물 정리
./gradlew clean

# 빌드 + 테스트 스킵
./gradlew build -x test
```

---

## 패키지 / 모듈 구조

```
com.example.cowmjucraft
├── CowMjuCraftApplication.java
├── domain/                             # 비즈니스 도메인
│   ├── accounts/                       # 계정/인증
│   │   ├── admin/
│   │   │   ├── account/               # 관리자 계정 관리
│   │   │   │   ├── controller/
│   │   │   │   ├── service/
│   │   │   │   ├── dto/request|response/
│   │   │   │   ├── entity/
│   │   │   │   └── repository/
│   │   │   └── auth/                  # 관리자 인증 (로그인/토큰)
│   │   ├── auth/                      # RefreshToken 등 공통 인증
│   │   ├── user/                      # 일반 사용자 (OAuth 포함)
│   │   │   ├── entity/
│   │   │   ├── repository/
│   │   │   └── oauth/
│   │   └── exception/
│   ├── common/                         # 공통 엔티티 (BaseTimeEntity)
│   ├── feedback/                       # 피드백
│   ├── introduce/                      # 소개
│   ├── item/                           # 프로젝트 물품
│   ├── mypage/                         # 마이페이지
│   ├── notice/                         # 공지사항
│   ├── order/                          # 주문
│   ├── payout/                         # 정산
│   ├── project/                        # 프로젝트
│   ├── recruit/                        # 모집
│   └── sns/                            # SNS 연동
└── global/                             # 공통/인프라
    ├── cloud/                          # S3 설정 및 서비스
    ├── config/                         # Spring 설정 (Security, CORS, Swagger, JWT)
    │   └── jwt/                        # JwtAuthenticationFilter, JwtTokenProvider
    ├── exception/                      # DomainException, GlobalExceptionHandler
    ├── response/                       # ApiResponse(팩토리), ApiResult(래퍼), type/
    └── seed/                           # 초기 데이터 (AdminInitializer)
```

**도메인별 패키지 구성** (item 도메인 기준)
```
domain/{도메인}/
├── controller/
│   ├── admin/     # 관리자 API — AdminXxxController, AdminXxxControllerDocs
│   └── client/    # 사용자 API — XxxController, XxxControllerDocs
├── dto/
│   ├── request/   # XxxRequestDto (record)
│   └── response/  # XxxResponseDto (record)
├── entity/        # 엔티티, 관련 Enum
├── exception/     # XxxException, XxxErrorType
├── repository/    # JpaRepository 확장
└── service/       # XxxService (admin/client 분리 시 하위 패키지)
```

**도메인 책임 경계**

| 도메인 | 책임 |
|---|---|
| `accounts/admin` | 관리자 계정 생성, 관리자 로그인/토큰 |
| `accounts/user` | 일반 사용자 엔티티, OAuth 로그인 |
| `accounts/auth` | RefreshToken 저장소 |
| `item` | 프로젝트 물품 CRUD, S3 Presign |
| `project` | 프로젝트 CRUD |
| `order` | 주문 생성/조회 |
| `payout` | 정산 처리 |
| `recruit` | 모집 공고 |
| `feedback` | 피드백 |
| `notice` | 공지사항 |
| `introduce` | 소개 페이지 |
| `mypage` | 마이페이지 (주문내역, 프로필) |
| `sns` | SNS 링크 관리 |
| `global` | 설정, 공통 응답, 예외 핸들러, JWT, S3 |

---

## 코딩 컨벤션

### 1. Request DTO

- 신규 Request DTO는 `record`로 작성
- DTO 이름은 `XxxRequestDto` 형식 (접미사 `Dto` 포함)
- Validation 어노테이션은 **반드시 별도 줄**에 배치 (한 줄 몰아쓰기 금지)
- setter 추가 금지 (record는 불변)

```java
// 올바른 예
@Schema(description = "프로젝트 물품 생성 요청")
public record AdminProjectItemCreateRequestDto(

        @NotBlank
        @Schema(description = "물품명", example = "명지공방 머그컵")
        String name,

        @Min(0)
        @Schema(description = "가격", example = "12000")
        int price,

        @NotNull
        @Schema(description = "판매 유형", example = "NORMAL")
        ItemSaleType saleType
) {}

// 금지 — 한 줄에 몰아쓰기
public record AdminProjectItemCreateRequestDto(
    @NotBlank @Schema(description = "물품명") String name
) {}
```

---

### 2. Response DTO

- 신규 Response DTO는 `record`로 작성
- DTO 이름은 `XxxResponseDto` 형식 (접미사 `Dto` 포함)
- **`from(Entity)` 메서드 불필요** — 서비스 레이어에서 직접 생성하는 패턴 유지
- Controller 레이어에 Entity 노출 금지 (Service → DTO 변환)

```java
// 올바른 예
@Schema(description = "프로젝트 물품 응답")
public record AdminProjectItemResponseDto(
        @Schema(description = "물품 ID") Long id,
        @Schema(description = "물품명") String name
) {}

// 서비스에서 직접 생성 (현재 패턴)
return new AdminProjectItemResponseDto(item.getId(), item.getName());
```

---

### 3. 예외 처리

이 프로젝트는 계층화된 예외 구조를 사용한다.

```
DomainException (abstract, global)
└── XxxException (도메인별 구체 예외)
    └── 생성자 인자: XxxErrorType (enum, ErrorCode 구현)
```

**규칙**
- `IllegalArgumentException`, `RuntimeException` 직접 throw 금지
- 새 도메인 예외 추가 시:
  1. `XxxErrorType` enum 생성 (`ErrorCode` 구현)
  2. `XxxException extends DomainException` 생성
  3. `throw new XxxException(XxxErrorType.XXX)` 사용
- `GlobalExceptionHandler`에 새 예외 타입을 추가할 필요 없음 — `DomainException` 핸들러가 자동 처리

```java
// 올바른 예
throw new ItemException(ItemErrorType.NOT_FOUND);
throw new AccountException(AccountErrorType.INVALID_CREDENTIALS);

// 금지
throw new IllegalArgumentException("물품을 찾을 수 없습니다.");
throw new RuntimeException("오류 발생");
```

**ErrorType enum 작성 규칙**
```java
@Getter
@RequiredArgsConstructor
public enum ItemErrorType implements ErrorCode {
    NOT_FOUND(404, "물품을 찾을 수 없습니다."),
    ALREADY_EXISTS(409, "이미 존재하는 물품입니다.");

    private final int httpStatusCode;
    private final String message;
}
```

---

### 4. 공통 응답 구조

`ApiResult<T>` — 실제 응답 본문 래퍼 (record)
`ApiResponse` — `ResponseEntity<ApiResult<T>>` 생성 팩토리 유틸
`SuccessType` — 성공 응답 타입 enum
`CommonErrorType` / `XxxErrorType` — 오류 응답 타입

```java
// 컨트롤러 반환 패턴
return ApiResponse.of(SuccessType.SUCCESS, service.getItem(id));   // 데이터 있음
return ApiResponse.of(SuccessType.CREATED, service.create(req));   // 생성
return ApiResponse.of(SuccessType.MEDIA_DELETED);                  // 데이터 없음
```

**Swagger import 충돌 처리**
`io.swagger.v3.oas.annotations.responses.ApiResponse`와 프로젝트 `ApiResponse` 이름 충돌 방지:
```java
// 규칙: 우리 ApiResponse는 import, Swagger @ApiResponse는 FQN
import com.example.cowmjucraft.global.response.ApiResponse;

@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", ...)
ResponseEntity<ApiResult<ItemResponseDto>> getItem(...);
```

---

### 5. Entity 작성 규칙

- `@NoArgsConstructor(access = AccessLevel.PROTECTED)` 필수
- setter 금지, 상태 변경은 의미 있는 메서드로 (예: `update(...)`, `clearThumbnail()`)
- 생성자는 package-level 또는 `public` 생성자 사용 (static factory 불필요)
- `BaseTimeEntity` 상속 필수 (`createdAt`, `updatedAt` 자동 관리)
- Enum 필드는 `@Enumerated(EnumType.STRING)` 사용

```java
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "project_items")
public class ProjectItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ... 필드

    public ProjectItem(Project project, String name, ...) {
        this.project = project;
        this.name = name;
        // ...
    }

    public void update(String name, ...) { ... }   // setter 대신 의미 있는 메서드
}
```

---

### 6. Controller 작성 규칙

- `@RestController` + `@RequiredArgsConstructor`
- `@RequestMapping`으로 기본 경로 지정
  - 관리자 API: `/api/admin/...`
  - 사용자 API: `/api/...`
- Swagger 문서는 `*ControllerDocs` 인터페이스로 분리, Controller가 `implements`
- 반환 타입: `ResponseEntity<ApiResult<T>>`
- 관리자/사용자 컨트롤러는 `controller/admin/`, `controller/client/` 별도 패키지로 분리

---

### 7. Service 작성 규칙

- `@Service` + `@RequiredArgsConstructor`
- 쓰기 메서드: `@Transactional` 개별 적용
- 읽기 메서드: `@Transactional(readOnly = true)` 개별 적용 (클래스 레벨 적용은 선택)
- 의존성 주입: 생성자 주입만 허용 (`@Autowired` 필드 주입 금지)
- admin/client 로직이 복잡하면 `service/admin/`, `service/client/` 패키지로 분리

---

### 8. S3 관련 규칙

- S3 직접 조작 금지 — `S3PresignFacade` 또는 `S3ObjectService` 경유
- Presign URL 발급: `S3PresignFacade.generatePresignPut(...)` / `generatePresignGet(...)`
- 객체 삭제: `S3ObjectService.delete(...)`

---

## Git 워크플로우

- 브랜치 전략: `main` ← PR로만 병합
- 브랜치 네이밍: `feat/기능명`, `fix/버그명`, `refactor/대상`, `chore/작업명`, `docs/대상`
- 커밋 형식: Conventional Commits (한글 제목, 50자 이내)
  ```
  feat: 프로젝트 물품 목록 조회 API 추가
  fix: 주문 생성 시 재고 차감 누락 수정
  ```
- PR은 `.github/pull_request_template.md` 형식 준수

---

## 절대 규칙

다음 행동은 어떤 상황에서도 금지된다.

1. **자동 push 금지** — `git push`는 사용자가 직접 실행
2. **force push 금지** — `git push --force` 절대 실행 금지
3. **main 직접 커밋 금지** — main 브랜치에 직접 commit 금지
4. **민감정보 커밋 금지** — API 키, 비밀번호, JWT 시크릿, DB 접속 정보, AWS 자격증명 등 커밋 금지
5. **보안 변경 사람 리뷰 필수** — `SecurityConfig`, `JwtAuthenticationFilter`, `JwtTokenProvider` 수정 시 반드시 사람이 리뷰 후 병합
6. **커밋 전 사용자 승인 필수** — 커밋 메시지 제안 후 승인 대기, 자동 커밋 금지

---

## Claude 커맨드 워크플로우

Claude Code에서 아래 명령을 요청하면 `.claude/commands/<command>.md` 파일을 먼저 읽고 해당 절차를 따른다.

| 명령 | 파일 | 용도 |
|---|---|---|
| `/feature` | `.claude/commands/feature.md` | 계획부터 PR 초안까지 전체 기능 워크플로우 |
| `/plan` | `.claude/commands/plan.md` | 구현 전 계획 수립 |
| `/impl` | `.claude/commands/impl.md` | 승인된 계획 기반 구현 |
| `/review` | `.claude/commands/review.md` | 변경사항 셀프 리뷰 |
| `/commit` | `.claude/commands/commit.md` | 커밋 메시지 제안 및 승인 후 커밋 |
| `/pr` | `.claude/commands/pr.md` | PR 설명 초안 작성 |

**적용 규칙**
- `.claude/commands`의 내용이 AGENTS.md와 충돌하면 AGENTS.md를 우선한다.
- 커맨드 파일을 읽었더라도 절대 규칙은 항상 유지한다.
- `/commit`은 커밋 메시지 제안 후 사용자 승인을 받은 경우에만 실행한다.
- `/pr`은 PR 초안만 작성하고, push 및 `gh pr create`는 실행하지 않는다.
