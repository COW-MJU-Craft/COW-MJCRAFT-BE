-- =====================================================================
-- V11: 고객 정보 저장·불러오기 기반 (조회 아이디/비밀번호 대체)
--
-- 주문마다 만들던 order_auth.lookup_id 대신 이메일을 고객 식별 키로 삼는다.
-- 세션·로그인 상태는 두지 않고, 요청마다 이메일 + 비밀번호를 제시받는다.
-- 자세한 설계는 docs/customer-identity-design.md 참조.
-- =====================================================================

-- 1) 고객
--    프로필 범위는 이름·연락처·구분·캠퍼스·학과/학번까지다.
--    환불 은행/계좌·입금자명·수령지는 여기 두지 않는다 — 비밀번호 하나가
--    계좌번호로 직결되지 않게 하기 위해서다. 불러오기는 최근 주문 스냅샷에서 읽는다.
CREATE TABLE customers (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    email               VARCHAR(255) NOT NULL,
    email_verified_at   DATETIME(6),
    password_hash       VARCHAR(255),
    password_set_at     DATETIME(6),
    pw_failed_attempts  INT          NOT NULL DEFAULT 0,
    pw_locked_until     DATETIME(6),
    phone               VARCHAR(50),
    name                VARCHAR(100),
    buyer_type          ENUM('EXTERNAL','STAFF','STUDENT'),
    campus              VARCHAR(50),
    department_or_major VARCHAR(100),
    student_no          VARCHAR(50),
    profile_saved_at    DATETIME(6),
    last_ordered_at     DATETIME(6),
    created_at          DATETIME(6),
    updated_at          DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_customers_email (email),
    KEY idx_customers_phone (phone)
) ENGINE=InnoDB;

-- 2) 주문 → 고객 연결
ALTER TABLE orders
    ADD COLUMN customer_id BIGINT NULL AFTER id;

-- 3) backfill: 기존 주문의 order_buyer.email 기준으로 고객 생성
--    order_buyer.email은 NOT NULL이므로 모든 주문이 고객을 갖게 된다.
--    운영 반영 전 아래로 예상 건수를 먼저 확인할 것:
--      SELECT COUNT(DISTINCT LOWER(TRIM(email))) FROM order_buyer;
INSERT INTO customers (email, created_at, updated_at, last_ordered_at)
SELECT LOWER(TRIM(b.email)),
       MIN(o.created_at),
       MIN(o.created_at),
       MAX(o.created_at)
FROM order_buyer b
JOIN orders o ON o.id = b.order_id
GROUP BY LOWER(TRIM(b.email));

-- 3-1) 보조 식별자(전화번호)는 가장 최근 주문의 값을 숫자만 남겨 채운다.
--      인증하지 않는 값이라 UNIQUE를 걸지 않는다 — 관리자 병합 힌트 용도다.
UPDATE customers c
JOIN (
    SELECT LOWER(TRIM(b.email)) AS email,
           REGEXP_REPLACE(b.phone, '[^0-9]', '') AS phone,
           ROW_NUMBER() OVER (
               PARTITION BY LOWER(TRIM(b.email))
               ORDER BY o.created_at DESC, o.id DESC
           ) AS rn
    FROM order_buyer b
    JOIN orders o ON o.id = b.order_id
) latest ON latest.email = c.email AND latest.rn = 1
SET c.phone = latest.phone;

UPDATE orders o
JOIN order_buyer b ON b.order_id = o.id
JOIN customers c ON c.email = LOWER(TRIM(b.email))
SET o.customer_id = c.id;

ALTER TABLE orders
    MODIFY customer_id BIGINT NOT NULL,
    ADD KEY idx_orders_customer_created_at_id (customer_id, created_at, id),
    ADD CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customers (id);

-- 4) 이메일 소유 증명 코드
--    키가 customer_id가 아니라 email인 이유: 코드 요청만으로 customers 행이
--    생기면 아무나 고객 테이블을 채울 수 있다.
CREATE TABLE customer_email_codes (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    email           VARCHAR(255) NOT NULL,
    code_hash       VARCHAR(64)  NOT NULL,
    expires_at      DATETIME(6)  NOT NULL,
    consumed_at     DATETIME(6),
    failed_attempts INT          NOT NULL DEFAULT 0,
    request_ip      VARCHAR(64),
    created_at      DATETIME(6),
    updated_at      DATETIME(6),
    PRIMARY KEY (id),
    KEY idx_customer_email_codes_email_created (email, created_at),
    KEY idx_customer_email_codes_expires (expires_at)
) ENGINE=InnoDB;
