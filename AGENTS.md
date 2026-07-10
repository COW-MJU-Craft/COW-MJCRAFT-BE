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
| 인증 | JWT (jjwt 0.11.5) + Spring Security — 관리자 로그인만 존재, 소셜(OAuth) 로그인 없음 |
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

**최초 셋업 (clone 후 1회)**
```bash
bash scripts/setup-hooks.sh   # pre-commit 훅 활성화 (main 커밋 차단·시크릿 차단)
```
- 로컬에 gitleaks가 없으면 훅은 grep 폴백으로 동작한다 (`brew install gitleaks` 권장)

---

## 패키지 / 모듈 구조

```
com.example.cowmjucraft
├── CowMjuCraftApplication.java
├── domain/                             # 비즈니스 도메인
│   ├── accounts/                       # 계정/인증 (관리자 전용 — 일반 사용자 계정 없음)
│   │   ├── admin/
│   │   │   ├── account/               # 관리자 계정 관리
│   │   │   ├── auth/                  # 관리자 인증 (로그인/토큰)
│   │   │   ├── entity/                # Admin
│   │   │   └── repository/
│   │   ├── auth/                      # RefreshToken 저장/발급
│   │   └── exception/
│   ├── common/                         # 공통 엔티티 (BaseTimeEntity)
│   ├── feedback/                       # 피드백
│   ├── introduce/                      # 소개 페이지
│   ├── item/                           # 프로젝트 물품 (ProjectItem, ItemImage)
│   ├── notice/                         # 공지사항
│   ├── order/                          # 주문 (Order + Auth/Buyer/Fulfillment/Item/ViewToken/CompletePage)
│   ├── payout/                         # 정산 (Payout, PayoutItem)
│   ├── project/                        # 프로젝트
│   ├── recruit/                        # 모집/지원 (Form, Question, Application, Answer)
│   └── sns/                            # SNS 링크
└── global/                             # 공통/인프라
    ├── cloud/                          # S3 설정 및 서비스
    ├── config/                         # Spring 설정 (Security, CORS, Swagger, Jackson, JPA Auditing)
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
| `accounts/auth` | RefreshToken 저장소 |
| `item` | 프로젝트 물품 CRUD, S3 Presign |
| `project` | 프로젝트 CRUD |
| `order` | 주문 생성/조회 |
| `payout` | 정산 처리 |
| `recruit` | 모집 공고, 지원서 접수/결과 |
| `feedback` | 피드백 |
| `notice` | 공지사항 |
| `introduce` | 소개 페이지 |
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

## 테스트 컨벤션

**작성 의무**: 새 서비스 로직 또는 기존 로직 변경 시 해당 부분의 테스트를 반드시 함께 작성한다. CI에서 PR 변경 코드의 diff coverage 70% 이상을 요구한다 — 구현 완료의 정의에 테스트가 포함된다.

**계층별 전략**

| 계층 | 방식 | 도구 |
|---|---|---|
| Service | 단위 테스트 (리포지토리/외부 의존성 mock) | JUnit 5 + Mockito |
| Repository | 슬라이스 테스트 | `@DataJpaTest` + H2 |
| Controller | 슬라이스 테스트 (필요 시) | `@WebMvcTest` |

**작성 규칙**
- 테스트 이름: `메서드명_상황_기대결과` (예: `createOrder_재고부족_OrderException발생`)
- 구조: given-when-then 주석으로 구분
- 검증 우선순위: 정상 케이스 1개 + 예외 케이스(도메인 예외 발생) 각 1개 이상
- 깡통 테스트 금지 — assertion 없는 테스트, 구현을 그대로 복사한 테스트는 작성하지 않는다
- 커버리지 수치를 올리기 위한 무의미한 getter/setter 테스트 금지

```java
@Test
void createOrder_재고부족_OrderException발생() {
    // given
    given(itemRepository.findById(1L)).willReturn(Optional.of(soldOutItem));

    // when & then
    assertThatThrownBy(() -> orderCreateService.create(request))
            .isInstanceOf(OrderException.class);
}
```

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

1. **main 병합은 사람만** — PR 머지는 사용자가 직접 실행, 에이전트는 절대 병합하지 않는다
2. **force push 금지** — `git push --force` 절대 실행 금지
3. **main 직접 커밋/push 금지** — main 브랜치에 직접 commit·push 금지 (서버 ruleset으로도 차단됨)
4. **민감정보 커밋 금지** — API 키, 비밀번호, JWT 시크릿, DB 접속 정보, AWS 자격증명 등 커밋 금지
5. **보안 변경 사람 리뷰 필수** — `SecurityConfig`, `JwtAuthenticationFilter`, `JwtTokenProvider` 수정 시 반드시 사람이 리뷰 후 병합
6. **커밋 전 사용자 승인 필수** — 커밋 메시지 제안 후 승인 대기, 자동 커밋 금지

**허용되는 것** (2026-07 개정)
- 작업 브랜치(`feat/*`, `fix/*` 등)로의 push — 커밋이 사용자 승인을 받았다면 허용
- `gh pr create --draft` — draft PR 생성 허용, 단 라벨(`agent:claude-code` 또는 `agent:codex`)을 붙인다
- main은 branch ruleset(PR 필수 + CI 필수 + force push 차단)이 서버에서 보호하므로, 위 위임이 가능하다

---

## AI 에이전트 커맨드 워크플로우

커맨드 원본은 `.agents/commands/`에 있다. `.claude/commands/`와 `.codex/commands/`는 이 디렉토리를 가리키는 심링크이므로, **수정은 반드시 `.agents/commands/`에서만** 한다.

아래 명령을 요청하면 `.agents/commands/<command>.md` 파일을 먼저 읽고 해당 절차를 따른다.

| 명령 | 파일 | 용도 |
|---|---|---|
| `/feature` | `.agents/commands/feature.md` | 계획부터 PR 초안까지 전체 기능 워크플로우 |
| `/plan` | `.agents/commands/plan.md` | 구현 전 계획 수립 |
| `/impl` | `.agents/commands/impl.md` | 승인된 계획 기반 구현 |
| `/review` | `.agents/commands/review.md` | 변경사항 셀프 리뷰 |
| `/commit` | `.agents/commands/commit.md` | 커밋 메시지 제안 및 승인 후 커밋 |
| `/pr` | `.agents/commands/pr.md` | PR 설명 초안 작성 |

**적용 규칙**
- 커맨드 파일 내용이 AGENTS.md와 충돌하면 AGENTS.md를 우선한다.
- 커맨드 파일을 읽었더라도 절대 규칙은 항상 유지한다.
- `/commit`은 커밋 메시지 제안 후 사용자 승인을 받은 경우에만 실행한다.
- `/pr`은 브랜치 push 후 draft PR 생성까지 실행한다. 머지는 하지 않는다.

---

## DB 스키마 변경 규칙 (Flyway)

> Flyway 도입 PR 병합 후 적용된다.

- 스키마 변경은 **반드시** `src/main/resources/db/migration/V{N}__{설명}.sql` 마이그레이션 파일로만 한다
- 엔티티만 수정하고 마이그레이션 파일을 빠뜨리면 운영 배포가 validate 오류로 실패한다
- 이미 병합된 마이그레이션 파일은 절대 수정하지 않는다 — 새 버전 파일을 추가한다
- 운영 프로필은 `ddl-auto: validate` — Hibernate가 스키마를 변경하는 일은 없어야 한다
- 마이그레이션 검증은 CI의 Testcontainers MySQL 테스트가 수행한다

---

## 핸드오프/상태 문서 컨벤션

에이전트가 작업 인계 문서(핸드오프, 계획, 상태 파일)를 작성할 때:

1. **기준점 기록 필수** — 문서 상단에 작성 시각, 기준 브랜치, HEAD SHA를 적는다
   ```
   > 작성: 2026-07-11 · 브랜치: chore/xxx · 기준 HEAD: abc1234
   ```
2. **완료 처리 규칙 명시** — 문서가 언제 효력을 잃는지, 완료 시 어떻게 처리할지(배너 후 아카이브 또는 삭제) 문서 안에 적는다
3. **완료된 핸드오프는 즉시 닫는다** — 체크리스트가 끝나면 상단에 `✅ 완료 (날짜, 병합 PR)` 배너를 달거나 삭제한다. 완료 상태로 방치된 핸드오프는 다음 에이전트에게 틀린 컨텍스트를 준다
4. **영구 정보는 이 파일(AGENTS.md)로 이관** — 일회성 문서에 영구 규칙을 남기지 않는다
