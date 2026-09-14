# 고객 정보 저장 · 불러오기 설계 (조회 아이디/비밀번호 폐기)

> 작성: 2026-09-02 · 갱신: 2026-09-08 · 브랜치: feat/customer-identity · 기준 HEAD: 98f74ec
> 상태: **Phase 1(백엔드) 구현 완료, 커밋 전**
> 완료 처리: Phase 3까지 병합되면 상단에 `✅ 완료 (날짜, PR)` 배너를 달고 영구 규칙은 AGENTS.md로 이관한다. 구현 중 설계가 바뀌면 이 문서를 먼저 고친다.

---

## 1. 배경

비회원 주문은 주문마다 **조회 아이디 + 비밀번호**를 만들게 되어 있고, `order_auth.lookup_id`에 전역 UNIQUE가 걸려 있다. `order_auth`는 주문 1:1 테이블이므로 이 아이디는 "사람의 아이디"가 아니라 "주문 1건의 열쇠"다. 결과적으로:

- 재구매 고객이 매번 새 아이디를 지어야 한다 (`a1234 → a12345`)
- 주문 퍼널 한가운데(step 2)에 아이디·비번·비번확인·중복확인 4개 UI가 끼어 있다
- `GET /api/orders/lookup-id/availability`가 인증 없는 아이디 존재 여부 오라클이다

## 2. 최종 방향

**로그인 상태를 만들지 않는다.** 필요한 것은 세 가지 일회성 동작뿐이고, 각각 요청 시점에 자격증명(이메일 + 비밀번호)을 제시한다.

| 동작 | 방식 |
|---|---|
| 주문서에서 저장한 정보 불러오기 | 이메일 + 비밀번호 → 폼 자동완성 |
| 내 주문 목록·상세 보기 | 이메일 + 비밀번호 → 목록/상세 |
| 정보 등록 · 비밀번호 재설정 | 이메일 + 코드 → 비밀번호 설정 |

세션·쿠키·로그인 상태가 없으므로 `customer_sessions` 테이블, `SameSite`/CORS 조정, argument resolver, 로그아웃 API, 세션 정리 스케줄러가 전부 필요 없다. **공용 PC에 로그인 상태가 남는 문제도 설계에서 사라진다.**

### 2.1 핵심 원칙

1. **주문 퍼널에 자격증명 0개.** 아이디도 비밀번호도 묻지 않는다. 불러오기는 어디까지나 선택 버튼이다.
2. **코드는 두 번만 등장한다.** 최초 정보 저장(=가입)과 비밀번호 분실. 그 외에는 전부 비밀번호다.
3. **프로필은 인증을 통과했을 때만 쓴다.** 주문 생성만으로는 프로필이 만들어지지 않는다 — 남의 이메일로 주문해 그 사람 정보를 덮어쓰는 경로를 만들지 않는다.
4. **조회의 기본 경로는 여전히 메일 링크다.** 상태가 바뀔 때마다 메일이 나가고 각 메일에 조회 링크가 있다. 이메일+비밀번호 조회는 "메일을 못 찾았을 때의 예비 경로"다.

### 2.2 결정 사항

| 항목 | 결정 |
|---|---|
| 고객 PK | `customers.id` (surrogate). 이메일은 PK가 아니라 UNIQUE 식별 키 |
| 식별 키 | **이메일** (소문자·trim 정규화). 휴대폰은 인증 없이 보조 식별자(관리자 병합 힌트·수령 연락)로만 저장, UNIQUE 아님 |
| 자격증명 | 이메일 + 비밀번호. `PasswordEncoder`(BCrypt)·`PasswordPolicy`·`CredentialMatcher` **기존 부품 재사용** |
| 이메일 소유 증명 | 6자리 코드. 최초 등록과 비밀번호 재설정에서만 사용 |
| 세션 | **없음.** 요청마다 자격증명을 body로 받는다 |
| SMS | 사용하지 않는다 (비용·발신번호 사전등록 불가) |
| 보안 설정 | `SecurityConfig`·`JwtAuthenticationFilter`·`JwtTokenProvider` **수정하지 않는다** |
| 프로필 저장 범위 | 이름·연락처·구분·캠퍼스·학과/학번. **환불 은행/계좌·입금자명·수령지는 `customers`에 컬럼조차 두지 않는다** |
| 불러오기 응답 범위 | 프로필 **+ 최근 주문 스냅샷의 환불 은행·계좌·입금자명·수령지.** 인증된 사용자는 주문 상세로 이미 볼 수 있는 값이므로 폼에 채워준다 (§7) |
| 주문 생성 | 이메일로 `customers` upsert(빈 고객 행) → `orders.customer_id` 연결. 프로필·비밀번호는 건드리지 않는다 |
| `order_auth` | 폐기. `lookup_id`/`password_hash`, 관련 API·에러 타입·서비스 삭제 |
| 이메일 오타 대책 | ① 프론트 도메인 오타 제안 ② 완료 화면에 입력 이메일 표시 + 문의 안내 ③ **관리자 주문 상세에서 이메일 수정 + 완료 메일 재발송** (실질 안전망) |

### 2.3 구현 전 확인

- [ ] **SPF/DKIM**: 발신 도메인에 잡혀 있는지 확인. 학교 메일은 외부 발신을 잘 거른다
- [ ] **환불 계좌 표시 방식**: 불러오기 시 평문으로 채울지, `국민 123456-**-***67 [다른 계좌 쓰기]`로 마스킹할지. 기본은 평문(§7)
- [x] ~~`PasswordPolicy`가 다른 곳에서도 쓰이는지~~ → `recruit/ApplicationService`와 관리자 인증에서 사용 중. `order_auth`를 지워도 남으므로 그대로 재사용한다
- [x] ~~쿠키 SameSite~~ → 세션이 없어 해당 없음

---

## 3. 흐름

### A. 처음 주문하는 사람

```
주문서 step2 ─ 상단 [저장한 정보 불러오기] 버튼 (안 누름)
             ─ 이름·연락처·이메일·환불계좌 직접 입력    ← 아이디/비번 칸 없음
   ↓
step3 수령방식 → step4 최종확인 → 주문 완료
   ↓
완료 화면 ─ 주문번호 · 입금 안내 · "yunjin@mju.ac.kr 로 메일 보냈어요"
         └ [다음 주문을 위해 정보 저장하기]   ← 안 누르면 끝. 비밀번호 없는 고객 행만 남는다
   ↓ (누른 경우 — 순차 입력 모달, §9.1)
① 코드 6자리 → 자동 검증
② 비밀번호
③ 비밀번호 확인
   ↓
"저장했어요" + "환불 계좌와 입금자명은 저장하지 않았어요"
```

### B. 재주문

```
주문서 step2 ─ [저장한 정보 불러오기] → 모달(이메일 + 비밀번호, 한 화면)
   ↓
폼 자동완성 ─ 프로필 + 최근 주문의 환불계좌·입금자명·수령지까지 전부 채워짐
   ↓
step4 최종확인 ─ [변경한 정보를 저장할까요?] 체크박스 (선택)
   ↓
주문 완료 (저장 CTA 없음)
   ↓ (체크한 경우)
프로필 갱신 요청을 주문 성공 후 별도로 호출
```

### C. 주문 조회

```
헤더 [주문 조회] → 이메일 + 비밀번호
   ↓
주문 목록  ← 비밀번호는 화면 메모리에만 유지
   ↓ 클릭 (같은 자격증명을 다시 실어 보냄, 재입력 없음)
주문 상세
```

정보를 저장한 적 없는 사람에게는 "주문 완료 메일의 조회 링크를 이용해주세요"를 안내한다.

### D. 비밀번호 분실

```
조회 화면 [비밀번호를 잊으셨나요]
   ↓
이메일 → 코드 발송 → 코드 + 새 비밀번호 → 재설정 → 바로 조회
```

등록(A)과 재설정(D)은 **서버 동작이 동일**하다 — 코드로 이메일 소유를 증명하고 비밀번호를 쓴다. 그래서 엔드포인트도 하나다(§5.1 `enroll`). 화면 문구만 다르다.

### E. 회원가입 메뉴 (주문 없이)

A의 저장 모달과 동일하되 프로필 필드를 처음부터 입력받는다. 같은 `enroll` API를 쓴다.

### F. 메일 링크 — 실제로 가장 많이 쓰일 경로

입금 확인·취소·환불 요청·환불 완료마다 메일이 나가고(`MailOutboxDeliveryService`) 각 메일에 조회 링크가 있다. 아무 입력 없이 그 주문이 열린다. **변경 없음.**

---

## 4. 데이터 모델

### 4.1 V11 — `customers` 신설 + `orders.customer_id` + backfill + 코드 테이블

```sql
-- V11__create_customers_and_email_codes.sql

CREATE TABLE customers (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    email               VARCHAR(255) NOT NULL,          -- 정규화(lower, trim)된 값만 저장
    email_verified_at   DATETIME(6),                    -- 코드 검증을 통과한 시각
    password_hash       VARCHAR(255),                   -- NULL = 아직 정보를 저장하지 않은 고객
    password_set_at     DATETIME(6),
    pw_failed_attempts  INT          NOT NULL DEFAULT 0,
    pw_locked_until     DATETIME(6),
    phone               VARCHAR(50),                    -- 숫자만 남겨 정규화. 인증 없음
    name                VARCHAR(100),
    buyer_type          ENUM('EXTERNAL','STAFF','STUDENT'),
    campus              VARCHAR(50),
    department_or_major VARCHAR(100),
    student_no          VARCHAR(50),
    profile_saved_at    DATETIME(6),                    -- NULL이면 직접 저장한 프로필 없음
    last_ordered_at     DATETIME(6),
    created_at          DATETIME(6)  NOT NULL,
    updated_at          DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_customers_email (email),
    KEY idx_customers_phone (phone)
) ENGINE=InnoDB;

ALTER TABLE orders
    ADD COLUMN customer_id BIGINT NULL AFTER id,
    ADD KEY idx_orders_customer (customer_id),
    ADD CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customers (id);

-- backfill: 기존 주문의 order_buyer.email 기준으로 고객 생성 후 연결
-- (전화번호는 GROUP_CONCAT 길이 제한을 피하려고 윈도 함수로 최신 1건만 고른다)
INSERT INTO customers (email, created_at, updated_at, last_ordered_at)
SELECT LOWER(TRIM(b.email)), MIN(o.created_at), MIN(o.created_at), MAX(o.created_at)
FROM order_buyer b
JOIN orders o ON o.id = b.order_id
GROUP BY LOWER(TRIM(b.email));

UPDATE customers c
JOIN (
    SELECT LOWER(TRIM(b.email)) AS email,
           REGEXP_REPLACE(b.phone, '[^0-9]', '') AS phone,
           ROW_NUMBER() OVER (PARTITION BY LOWER(TRIM(b.email))
                              ORDER BY o.created_at DESC, o.id DESC) AS rn
    FROM order_buyer b JOIN orders o ON o.id = b.order_id
) latest ON latest.email = c.email AND latest.rn = 1
SET c.phone = latest.phone;

UPDATE orders o
JOIN order_buyer b ON b.order_id = o.id
JOIN customers c  ON c.email = LOWER(TRIM(b.email))
SET o.customer_id = c.id;

ALTER TABLE orders MODIFY customer_id BIGINT NOT NULL;

-- 코드 테이블의 키는 customer_id가 아니라 email이다.
-- 코드 요청만으로 customers 행이 생기지 않게 하기 위해서다.
CREATE TABLE customer_email_codes (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    email           VARCHAR(255) NOT NULL,
    code_hash       VARCHAR(64)  NOT NULL,              -- HMAC-SHA256(code, 서버 pepper)
    expires_at      DATETIME(6)  NOT NULL,
    consumed_at     DATETIME(6),
    failed_attempts INT          NOT NULL DEFAULT 0,
    request_ip      VARCHAR(64),
    created_at      DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    KEY idx_customer_email_codes_email_created (email, created_at),
    KEY idx_customer_email_codes_ip_created (request_ip, created_at)
) ENGINE=InnoDB;
```

> backfill 주의: 운영 반영 전 dev DB 덤프로 `SELECT LOWER(TRIM(email)), COUNT(*) FROM order_buyer GROUP BY 1`을 돌려 결과 건수가 예상과 맞는지 확인한다. `order_buyer.email`이 NOT NULL이므로 `customer_id`를 NOT NULL로 올려도 실패하지 않는다.

### 4.2 V12 — `order_auth` 제거 (Phase 3, 프론트 전환 완료 후)

```sql
-- V12__drop_order_auth.sql
DROP TABLE order_auth;
```

### 4.3 엔티티

- `Customer` (신규, `BaseTimeEntity` 상속) — `updateProfile(...)`, `markEmailVerified(now)`, `setPassword(hash, now)`, `recordPasswordFailure(now)`, `resetPasswordFailures()`, `touchLastOrdered(now)`, `clearProfile()`
- `CustomerEmailCode` (신규) — `BaseTimeEntity` 상속
- `Order` — `@ManyToOne(fetch = LAZY) Customer customer` 추가
- `OrderAuth` — Phase 3에서 삭제
- `CustomerFailureRecorder` (신규) — 실패 카운터를 `REQUIRES_NEW`로 별도 커밋한다(§6.6)
- ~~`MailOutboxEventType` — `EMAIL_CODE` 추가~~ → **채택하지 않음.** `MailOutbox`는 주문 전용 스키마다(§6.7)

패키지: `domain/customer/{controller/client, dto, entity, exception, repository, service}`. 예외는 `CustomerException` + `CustomerErrorType`.

---

## 5. API 계약

자격증명은 **항상 요청 body**로 받는다. 쿼리 스트링에 이메일·비밀번호를 절대 넣지 않는다. 이 때문에 조회성 요청도 `POST`를 쓴다.

### 5.1 신규 — 고객

| 메서드 | 경로 | 요청 | 응답 | 비고 |
|---|---|---|---|---|
| POST | `/api/customers/email-code` | `{ email }` | **항상** `202 { message }` | 등록·재설정 공용. 이메일 존재 여부와 무관하게 동일 응답(열거 방지). 레이트 리밋 초과 시에도 동일 응답 + WARN 로그 |
| POST | `/api/customers/enroll` | `{ email, code, password, profile? }` | `200 { email, profileSaved }` | **등록과 비밀번호 재설정을 겸한다.** 고객이 없으면 생성, 있으면 비밀번호를 갱신한다. `profile`이 오면 저장, 없으면 프로필을 건드리지 않는다 |
| POST | `/api/customers/prefill` | `{ email, password }` | `{ source, buyer, fulfillment }` | 주문서 "불러오기" 버튼. 범위는 §7 |
| POST | `/api/customers/profile` | `{ email, password, profile }` | `200` | 프로필 갱신. 주문 완료 후 "변경한 정보 저장"과 조회 화면의 "내 정보 수정"이 함께 쓴다 |
| POST | `/api/customers/orders` | `{ email, password }` | `[{ orderId, orderNo, status, finalAmount, depositDeadline, createdAt, itemSummary }]` | `created_at DESC`. `itemSummary` = "머그컵 외 2건" |
| POST | `/api/customers/orders/{orderId}` | `{ email, password }` | `OrderDetailResponseDto` (기존 그대로) | **`order.customer_id`가 인증된 고객과 다르면 `404 ORDER_NOT_FOUND`** (403은 주문 존재를 흘린다) |

비밀번호 검증에 실패하는 모든 경우 — 고객 없음 / `password_hash` NULL / 잠금 중 / 불일치 — 는 **동일한 `401 INVALID_CREDENTIALS`**를 반환한다(§6.3).

### 5.2 변경 — 주문 생성 `POST /api/orders`

| 항목 | Phase 1 (호환) | Phase 3 (정리) |
|---|---|---|
| `lookupId`, `password` | **선택**으로 완화. 오면 기존대로 `order_auth` 저장, 없으면 건너뜀 | 필드 삭제 |
| `buyer.email` | 정규화 → `customers` upsert(없으면 email만 있는 행 생성) → `orders.customer_id` 연결 → `last_ordered_at` 갱신 | 동일 |
| 응답 `lookupId` | 요청에 없으면 `null` | 필드 삭제 |
| 응답 나머지 | 동일 (`viewToken` 유지) | 동일 |

**주문 생성은 프로필과 비밀번호를 절대 쓰지 않는다.** "변경한 정보를 저장할까요?"를 체크한 경우, 프론트가 **주문 성공 후 별도로** `POST /api/customers/profile`을 호출한다. 이렇게 나누면 프로필 저장이 실패해도 주문은 이미 확정돼 있다.

### 5.3 유지

- `GET /api/orders/view?token=` — 메일 링크 조회. 변경 없음
- `GET /api/orders/complete-page?token=` — 변경 없음

### 5.4 삭제 (Phase 3)

- `GET /api/orders/lookup-id/availability`, `POST /api/orders/lookup`
- `OrderLookupIdService`, `OrderLookupRequestDto`, `OrderLookupIdAvailabilityResponseDto`, `OrderAuth`, `OrderAuthRepository`
- `OrderDetailQueryService.getByLookupIdAndPassword`
- `OrderErrorType`: `INVALID_LOOKUP_CREDENTIALS`, `DUPLICATED_LOOKUP_ID`, `WEAK_PASSWORD`, `LOOKUP_FIELD_REQUIRED`
- `PasswordEncoder`/`PasswordPolicy`/`CredentialMatcher`는 **삭제하지 않는다** — recruit·관리자 인증·고객 비밀번호가 계속 쓴다

### 5.5 변경 — 관리자

- `PATCH /api/admin/orders/{id}/buyer-email` `{ email }` (신규) — `order_buyer.email` 수정 + `customers` upsert·재연결 + 완료 메일(view link) 재발송. 이메일 오타 안전망

---

## 6. 인증과 보안

세션이 없어 비밀번호가 매 요청에 실린다. 그만큼 **브루트포스 방어가 이 설계의 핵심**이다.

### 6.1 코드 발급 (`POST /api/customers/email-code`)

1. 이메일 정규화
2. 레이트 리밋 — **두 겹으로 나눈다**:
   - 같은 이메일: **60초 내 1회**, **24시간 내 10회** — `customer_email_codes.created_at` 카운트(서비스 레이어). 필터는 body를 읽지 않으므로 이메일 기준은 여기서만 가능하다
   - 같은 IP: 기존 `RateLimitFilter` 재사용. `RateLimitRule.CUSTOMER_EMAIL_CODE`(성공까지 모두 카운트, 기본 1시간 20회)
   - 이메일 기준 초과 시 발송하지 않고 202만 반환 + WARN 로그
3. 해당 이메일의 미소진 코드를 전부 `consumed_at = now`로 무효화 (항상 최신 1개만 유효)
4. 6자리 숫자 코드 생성(`SecureRandom`), `HMAC-SHA256(code, pepper)` 저장, TTL **10분**
5. `CustomerCodeMailSender`가 **동기 발송**한다(§6.7). 실패는 로그만 남기고 삼킨다 — 예외를 올리면 응답이 달라져 발송 시도 여부가 드러난다

pepper는 `app.customer.code-pepper`로 주입, 커밋 금지(절대 규칙 4).

### 6.2 등록 · 재설정 (`POST /api/customers/enroll`)

1. 이메일 정규화 → 해당 이메일의 **가장 최근 미소진·미만료** 코드 1건 조회. 없으면 `401 INVALID_EMAIL_CODE`
2. `failed_attempts >= 5`면 `401` (코드 소진 처리)
3. HMAC 비교(`MessageDigest.isEqual`). 불일치 → `failed_attempts++`, `401`
4. 일치 → `consumed_at = now`
5. `customers` upsert. `email_verified_at = now`(최초 1회), `password_hash` 저장, `password_set_at = now`, `pw_failed_attempts = 0`, `pw_locked_until = NULL`
6. `profile`이 왔으면 저장하고 `profile_saved_at = now`

비밀번호는 기존 `PasswordPolicy`(8자 이상, 영문+숫자)를 통과해야 한다. 위반 시 `422 WEAK_PASSWORD`.

### 6.3 비밀번호 검증 (`prefill` · `profile` · `orders` 공통)

`CustomerCredentialService.authenticate(email, password)` 하나로 모으고 모든 엔드포인트가 이것만 호출한다.

1. 이메일 정규화 → `customers` 조회
2. **아래 넷 중 어느 경우든 동일한 `401 INVALID_CREDENTIALS`**:
   - 고객이 없음
   - `password_hash`가 NULL (정보를 저장한 적 없음)
   - `pw_locked_until`이 미래 (잠금 중)
   - 비밀번호 불일치
3. 고객이 없어도 BCrypt 연산을 수행해 응답 시간으로 계정 유무가 드러나지 않게 한다 — 기존 `CredentialMatcher`가 이 패턴을 이미 구현하고 있다
4. 실패 시 `pw_failed_attempts++`. **10회 연속 실패 → `pw_locked_until = now + 1시간`**
5. 성공 시 `pw_failed_attempts = 0`

**잠금은 코드 경로를 막지 않는다.** 비밀번호가 잠겨도 `email-code` → `enroll`로 재설정해 들어올 수 있어야 복구가 된다.

추가 방어: 같은 IP에서 비밀번호 검증 실패가 **1시간 내 50회**를 넘으면 그 IP의 자격증명 요청을 1시간 차단한다.

### 6.4 프론트의 비밀번호 취급

조회 화면과 불러오기 이후 흐름에서 프론트가 비밀번호를 들고 있어야 한다.

- **React state에만 둔다.** `localStorage`·`sessionStorage`·URL에 절대 넣지 않는다 — `orderDraft.ts`가 이미 이 원칙으로 정리돼 있다(민감 필드는 저장 타입에 필드조차 없다)
- 페이지를 벗어나거나 새로고침하면 사라지고 다시 입력받는다. 세션이 없는 설계의 자연스러운 결과다
- 조회 화면 이탈 시 state를 명시적으로 비운다

### 6.5 정리 작업

- `customer_email_codes`: 만료 24시간 지난 행 일 1회 삭제 (`CustomerEmailCodeCleanupWorker`, `@Scheduled` cron)

### 6.6 실패 카운터는 별도 트랜잭션에 커밋한다

구현 중 발견한 함정이다. 실패를 알리는 예외(`CustomerException`)는 `RuntimeException`이라
호출부 트랜잭션을 롤백시킨다. 같은 트랜잭션에서 카운터를 올리면 **그 증가분도 함께 사라져**
몇 번을 틀려도 카운터가 0에 머물고 잠금이 영영 걸리지 않는다.

`CustomerFailureRecorder`가 `@Transactional(propagation = REQUIRES_NEW)`로 증가분만 먼저
커밋한 뒤 호출부가 예외를 던진다. 자기 호출은 프록시를 타지 않으므로 반드시 별도 빈이어야 한다.
비밀번호 실패와 코드 실패 양쪽 모두 이 경로를 쓴다.

### 6.7 코드 메일은 MailOutbox를 쓰지 않는다

`MailOutbox`는 주문 전용 스키마다 — `aggregate_id`·`order_no`·`view_url`이 NOT NULL이라
코드 메일에는 더미 값을 채워야 하고, `(event_type, aggregate_id, aggregate_version)` 유니크
제약이 같은 이메일 재발송을 막는다. 게다가 코드는 10분짜리라 워커 폴링 지연을 얹을 이유가 없다.

그래서 `CustomerCodeMailSender`가 동기 발송한다. SMTP 타임아웃이 `application.yml`에서
연결 5초·읽기 10초로 묶여 있어 요청이 무한정 붙잡히지 않는다.

---

## 7. 불러오기(`POST /api/customers/prefill`) 범위

```
source = profile_saved_at != null ? 'PROFILE' : (최근 주문 있으면 'LAST_ORDER' : null)

buyer.이름·연락처·구분·캠퍼스·학과·학번
    ← PROFILE이면 customers, 아니면 최근 order_buyer 스냅샷

buyer.환불은행·환불계좌·유입경로, depositorName
    ← 항상 최근 order_buyer / orders 스냅샷  (customers에 컬럼 없음)

fulfillment.method·수령인·연락처·우편번호·주소1·주소2
    ← 항상 최근 order_fulfillment 스냅샷
```

**환불 계좌를 응답에 포함하는 이유**: 인증을 통과한 사람은 `POST /api/customers/orders/{id}`로 자기 주문 상세를 열면 환불 계좌를 이미 볼 수 있다(`OrderDetailResponseDto.BuyerInfo`). 폼에 채워주는 것은 새로운 노출이 아니라 이미 접근 가능한 값을 옮겨놓는 것이다. **저장 위치를 늘리지 않으면서 입력 부담만 없앤다.**

따라서 폼에는 "매번 입력" 같은 표시를 두지 않는다 — 전부 채워지고, 바뀐 곳만 고치면 된다. 최종확인 단계에서 계좌가 한 번 더 보이므로 잘못된 계좌로 넘어갈 위험도 없다.

프론트는 prefill을 **초기값으로만** 쓰고, 이후 사용자가 수정한 값이 우선한다.

---

## 8. 롤아웃

AGENTS.md 원칙: API 계약을 깨는 변경은 프론트가 dev에서 연동 검증을 마친 뒤에만 `main`으로 간다.

| Phase | 레포 | 내용 | main 병합 조건 |
|---|---|---|---|
| **1** | BE | V11 마이그레이션, `customer` 도메인, §5.1 API 6개, `POST /api/orders`의 `lookupId`/`password` **선택화** + `customers` 연결, 관리자 이메일 수정 API, 코드 메일 동기 발송, 레이트 리밋 규칙 3종 | 기존 프론트가 그대로 동작하므로 CI 통과 후 바로 가능 |
| **2** | FE | §9 전체. `POST /api/orders`에서 `lookupId`/`password` 제거 | Phase 1이 운영에 있어야 함 |
| **3** | BE | V12 `order_auth` drop, §5.4 삭제, `OrderCreateRequestDto` 필드 삭제 | Phase 2가 운영에 있고, 운영 로그에서 `lookupId` 포함 요청이 0건임을 확인한 뒤 |

Phase 1의 `POST /api/orders` 변경은 기존 계약을 **넓히기만** 하므로 프론트 검증 없이 넘겨도 된다.

**롤백**
- Phase 1: 앱만 이전 버전으로. V11 테이블이 남아도 이전 코드가 참조하지 않아 무해하다 (`ddl-auto: validate`는 추가 컬럼을 오류로 보지 않는다). `docs/rollback.md` 절차 그대로
- Phase 3: `order_auth`가 사라졌으므로 Phase 1 버전으로만 롤백 가능. V12 반영 전 스냅샷 필수

---

## 9. 프론트 변경 (COW-MJCRAFT-FE)

### 9.1 입력 방식

**순차 입력(progressive disclosure)은 처음 겪는 흐름에만 쓴다.** 반복 작업에 쓰면 오히려 답답하다.

| 화면 | 방식 |
|---|---|
| 정보 저장(가입) 모달 | **순차** — 코드 → 비밀번호 → 비밀번호 확인 |
| 비밀번호 재설정 모달 | **순차** — 이메일 → 코드 → 새 비밀번호 |
| 불러오기 모달 | **한 화면** — 이메일 + 비밀번호 |
| 주문서 step2 | **한 화면** — 필드가 10개가 넘는다 |

순차 입력 구현 규칙:

- **코드는 6자리를 채우는 순간 버튼 없이 자동 검증.** 틀리면 칸이 짧게 흔들리고 비워진 뒤 첫 칸으로 포커스
- 지나간 단계는 `✓ 확인됐어요`로 접히되 **다시 눌러 수정 가능**
- 새 필드가 나타나면 자동 포커스 + `aria-live="polite"`로 스크린리더에 알림
- 전환 150~200ms, `prefers-reduced-motion` 존중
- 모달 높이가 늘어날 때 화면이 튀지 않게
- 상단에 `1/3` 진행 표시 — 순차 방식은 전체 길이가 안 보여 불안을 준다

### 9.2 파일별 변경

| 파일 | 변경 |
|---|---|
| `src/pages/site/OrderPage.tsx` | `LookupForm`·`LookupCheckState`·`handleLookupIdCheck`·step 2의 아이디/비번/중복확인 UI 삭제. `validateBuyerStep`에서 lookup 검증 제거. **step 2 상단에 [저장한 정보 불러오기] 버튼 + 모달**. step 4에 [변경한 정보를 저장할까요?] 체크박스. 이메일 도메인 오타 제안 |
| `src/pages/site/OrderCompletePage.tsx` | `lookupId` 표시 삭제. 입력 이메일 표시 + "메일이 안 오면…" 안내. **[다음 주문을 위해 정보 저장하기] → 순차 입력 모달** |
| `src/pages/site/OrderLookupPage.tsx` | 아이디/비번 → **이메일 + 비밀번호**. 성공 시 같은 페이지에서 주문 목록 표시. [비밀번호를 잊으셨나요] · 저장한 적 없는 사람 안내 |
| `src/pages/site/SignUpPage.tsx` (신규) | 주문 없이 등록. 코드 → 비밀번호 → 프로필. `enroll` API |
| `src/components/customer/` (신규) | `LoadInfoModal`(불러오기), `EnrollModal`(저장·재설정, 순차), `OrderListPanel`, `ProfileEditForm` |
| 헤더 네비 | **[주문 조회]** 와 **[정보 등록]** 두 개. 로그인 상태가 없으므로 상태 표시는 없다 |
| `src/api/orders.ts` | `checkLookupIdAvailability`·`lookupOrder`·관련 타입 삭제. `OrderCreateRequest`에서 `lookupId`/`password` 제거 |
| `src/api/customers.ts` (신규) | `requestEmailCode`, `enroll`, `prefill`, `saveProfile`, `myOrders`, `myOrder(id)` |
| `src/pages/site/OrderViewPage.tsx` | 변경 없음 (view token 유지) |
| `src/utils/orderDraft.ts` | 변경 없음. 비밀번호는 여기를 거치지 않는다(§6.4) |
| e2e | 주문 플로우에서 아이디 단계 제거. 저장→불러오기→재주문 시나리오, 조회 시나리오 추가 (테스트용 코드는 dev 프로필 `app.customer.expose-code-in-response=true`로 202 응답에 포함 — 운영 기본 false) |

### 9.3 문구

정보를 저장하지 않은 계정에 비밀번호를 넣어도 계정 존재 여부가 드러나면 안 되므로 실패 메시지는 항상 동일하게:

> 이메일 또는 비밀번호가 올바르지 않아요. 정보를 저장하신 적이 없다면 주문 완료 메일의 조회 링크를 이용해주세요.

---

## 10. 테스트 요구 (diff coverage 70%)

- `CustomerEmailCodeService`: 발급 정상 / 60초 내 재요청 미발송 / 이전 코드 무효화 / 만료 코드 실패 / 5회 실패 후 코드 폐기
- `CustomerEnrollService`: 신규 등록 성공 / **기존 고객에 재호출 시 비밀번호 갱신(=재설정)** / 정책 위반 422 / `profile` 없이 호출하면 프로필 미변경
- `CustomerCredentialService`: 정상 인증 / 고객 없음·비번 미설정·불일치가 **모두 같은 401** / 10회 실패 후 잠금 / **잠금 상태에서도 enroll로 재설정 가능**
- `CustomerPrefillService`: PROFILE / LAST_ORDER / 주문 없음 → null. **응답에 환불 계좌가 포함되고, 그 값이 `customers`가 아니라 최근 주문 스냅샷에서 왔음**을 단언
- `CustomerOrderQueryService`: 목록 정렬 / 남의 `orderId` → 404
- `OrderCreateService`: 신규 이메일 → `customers` 생성 / 기존 이메일 → 재사용·`last_ordered_at` 갱신 / 대소문자 다른 이메일 → 같은 고객 / `lookupId` 없이 생성 성공 (Phase 1) / **주문 생성이 `password_hash`·프로필을 변경하지 않음**
- `AdminOrderBuyerEmailService`: 이메일 수정 시 고객 재연결 + 메일 enqueue
- `CustomerFailureRecorder`: 실패 기록이 **호출부 롤백과 무관하게** 커밋되는지 (§6.6)
- `AdminOrderBuyerEmailService`: 이메일 수정 시 스냅샷·고객·조회 링크 셋을 함께 맞추는지
- Testcontainers 마이그레이션 테스트(`CustomerBackfillMigrationTest`): **V10까지 적용 → 대소문자만 다른 주문 시드 → V11 적용** 순으로 Flyway를 나눠 실행하고, 고객이 1건으로 합쳐지는지·모든 주문에 `customer_id`가 채워지는지·NOT NULL과 FK가 걸리는지 검증. 로컬에 Docker가 없으면 skip되고 CI에서 실행된다

---

## 11. 하지 않기로 한 것

| 후보 | 이유 |
|---|---|
| 세션 · 로그인 상태 | 필요한 동작이 전부 일회성이다. 세션을 두면 테이블·쿠키·SameSite·resolver·로그아웃·정리 스케줄러가 따라오고, 공용 PC에 상태가 남는 문제까지 새로 생긴다 |
| 주문 퍼널에서 자격증명 받기 | 아이디 4칸을 걷어낸 이유가 퍼널 마찰이다. 불러오기는 버튼 하나로 두고 건너뛸 수 있게 한다 |
| 등록과 비밀번호 재설정 분리 | 서버 동작이 같다(코드로 소유 증명 + 비밀번호 저장). 엔드포인트를 나누면 계정 존재 여부만 드러난다 |
| 주문 목록 응답에 view token 싣기 | 조회할 때마다 토큰 행이 쌓여 정리 작업이 필요해진다. 상세도 자격증명으로 받는 편이 단순하다 |
| `customers`에 환불 계좌 컬럼 | 비밀번호 하나가 계좌번호로 직결된다. 불러오기는 최근 주문 스냅샷에서 읽어 채운다(§7) |
| 주문 생성 시 프로필 저장 | 미인증 상태에서 프로필을 쓰면 남의 이메일로 덮어쓰기가 가능하다. 주문 성공 후 별도 호출로 분리한다 |
| `customer_identities` 1:N 식별자 테이블 | SMS 채널이 없어 두 번째 채널이 없다. 이메일 2개 병합 요청이 실제로 생기면 그때 V파일 하나로 옮긴다 |
| 휴대폰 UNIQUE | 검증 불가한 값에 유니크는 의미 없다. 부모 번호·번호 변경 케이스도 막힌다 |
| SMS 인증 | 발신번호 사전등록에 사업자가 필요하고 건당 비용이 든다 |
