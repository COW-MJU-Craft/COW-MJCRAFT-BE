-- =====================================================================
-- V1: baseline schema (2026-07-11)
--
-- 생성 근거: JPA 엔티티(25개) 수작업 변환 — Hibernate 7 MySQL 매핑 규칙 기준
--   (CamelCase→snake_case, @Enumerated(STRING)→ENUM, @Lob String→LONGTEXT,
--    boolean→BIT(1), LocalDateTime→DATETIME(6))
--
-- ✅ 운영 스키마 대조 완료 (2026-07-11, IntelliJ Generate DDL 덤프 기준):
--   enum 값 순서·nullable·ON DELETE CASCADE를 운영과 일치시킴.
--   운영에만 있는 레거시 테이블(users, member_addresses)은 baseline에서 제외 —
--   validate는 여분 테이블을 무시함. 정리는 별도 결정 사항.
--   운영과 엔티티의 드리프트(BASIC 누락, answer TINYTEXT)는 V2에서 수정.
--   운영 DB에는 baseline-on-migrate=true 로 이 파일이 실행되지 않고
--   기준점으로만 기록됨. 실제 실행 대상은 빈 DB(테스트·신규 환경)뿐.
-- =====================================================================

CREATE TABLE admins (
    id BIGINT NOT NULL AUTO_INCREMENT,
    login_id VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    role ENUM('ROLE_ADMIN','ROLE_USER') NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_admins_login_id (login_id),
    UNIQUE KEY uk_admins_email (email)
) ENGINE=InnoDB;

CREATE TABLE refresh_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    subject VARCHAR(128) NOT NULL,
    role ENUM('ROLE_ADMIN') NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    revoked_at DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_refresh_tokens_token_hash (token_hash),
    KEY idx_refresh_tokens_subject_role (subject, role)
) ENGINE=InnoDB;

CREATE TABLE feedbacks (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    status ENUM('ANSWERED','RECEIVED') NOT NULL,
    answer TINYTEXT,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE introduce (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    subtitle VARCHAR(200),
    summary VARCHAR(255),
    hero_logo_keys JSON,
    sections JSON NOT NULL,
    version INT,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE projects (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    summary VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    thumbnail_key VARCHAR(255) NOT NULL,
    deadline_date DATE NOT NULL,
    status ENUM('CLOSED','OPEN','PREPARING') NOT NULL,
    category ENUM('GOODS','JOURNAL') NOT NULL,
    pinned BIT(1) NOT NULL,
    pinned_order INT,
    manual_order INT,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE project_images (
    project_id BIGINT NOT NULL,
    image_key VARCHAR(255) NOT NULL,
    sort_order INT NOT NULL,
    PRIMARY KEY (project_id, sort_order),
    CONSTRAINT fk_project_images_project FOREIGN KEY (project_id) REFERENCES projects (id)
) ENGINE=InnoDB;

CREATE TABLE project_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    summary VARCHAR(200),
    description MEDIUMTEXT NOT NULL,
    price INT NOT NULL,
    sale_type ENUM('GROUPBUY','NORMAL') NOT NULL,
    status ENUM('CLOSED','OPEN','PREPARING') NOT NULL,
    item_type ENUM('DIGITAL_JOURNAL','PHYSICAL') NOT NULL,
    thumbnail_key VARCHAR(255),
    journal_file_key VARCHAR(255),
    target_qty INT,
    funded_qty INT,
    stock_qty INT,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_project_items_project FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE item_images (
    id BIGINT NOT NULL AUTO_INCREMENT,
    item_id BIGINT NOT NULL,
    image_key VARCHAR(255) NOT NULL,
    sort_order INT NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_item_images_item FOREIGN KEY (item_id) REFERENCES project_items (id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE notices (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE notice_images (
    notice_id BIGINT NOT NULL,
    image_key VARCHAR(255),
    sort_order INT NOT NULL,
    PRIMARY KEY (notice_id, sort_order),
    CONSTRAINT fk_notice_images_notice FOREIGN KEY (notice_id) REFERENCES notices (id)
) ENGINE=InnoDB;

CREATE TABLE orders (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_no VARCHAR(50) NOT NULL,
    status ENUM('PENDING_DEPOSIT','PAID','CANCELED','REFUND_REQUESTED','REFUNDED') NOT NULL,
    total_amount INT NOT NULL,
    shipping_fee INT NOT NULL,
    final_amount INT NOT NULL,
    deposit_deadline DATETIME(6) NOT NULL,
    paid_at DATETIME(6),
    canceled_at DATETIME(6),
    cancel_reason VARCHAR(500),
    refund_requested_at DATETIME(6),
    refunded_at DATETIME(6),
    stock_deducted_at DATETIME(6),
    depositor_name VARCHAR(100) NOT NULL,
    privacy_agreed BIT(1) NOT NULL,
    privacy_agreed_at DATETIME(6) NOT NULL,
    refund_agreed BIT(1) NOT NULL,
    refund_agreed_at DATETIME(6) NOT NULL,
    cancel_risk_agreed BIT(1) NOT NULL,
    cancel_risk_agreed_at DATETIME(6) NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_orders_order_no (order_no)
) ENGINE=InnoDB;

CREATE TABLE order_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    project_item_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price INT NOT NULL,
    line_amount INT NOT NULL,
    item_name_snapshot VARCHAR(255) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_order_items_project_item FOREIGN KEY (project_item_id) REFERENCES project_items (id)
) ENGINE=InnoDB;

CREATE TABLE order_auth (
    order_id BIGINT NOT NULL,
    lookup_id VARCHAR(50) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (order_id),
    UNIQUE KEY uk_order_auth_lookup_id (lookup_id),
    CONSTRAINT fk_order_auth_order FOREIGN KEY (order_id) REFERENCES orders (id)
) ENGINE=InnoDB;

CREATE TABLE order_buyer (
    order_id BIGINT NOT NULL,
    buyer_type ENUM('EXTERNAL','STAFF','STUDENT') NOT NULL,
    campus VARCHAR(50),
    name VARCHAR(100) NOT NULL,
    department_or_major VARCHAR(100),
    student_no VARCHAR(50),
    phone VARCHAR(50) NOT NULL,
    refund_bank VARCHAR(100) NOT NULL,
    refund_account VARCHAR(100) NOT NULL,
    referral_source VARCHAR(100),
    email VARCHAR(255) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (order_id),
    CONSTRAINT fk_order_buyer_order FOREIGN KEY (order_id) REFERENCES orders (id)
) ENGINE=InnoDB;

CREATE TABLE order_fulfillment (
    order_id BIGINT NOT NULL,
    method ENUM('DELIVERY','PICKUP') NOT NULL,
    receiver_name VARCHAR(100) NOT NULL,
    receiver_phone VARCHAR(50) NOT NULL,
    info_confirmed BIT(1) NOT NULL,
    postal_code VARCHAR(20),
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    delivery_memo VARCHAR(500),
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (order_id),
    CONSTRAINT fk_order_fulfillment_order FOREIGN KEY (order_id) REFERENCES orders (id)
) ENGINE=InnoDB;

CREATE TABLE order_view_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    revoked_at DATETIME(6),
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_view_tokens_token_hash (token_hash),
    CONSTRAINT fk_order_view_tokens_order FOREIGN KEY (order_id) REFERENCES orders (id)
) ENGINE=InnoDB;

CREATE TABLE order_complete_pages (
    id BIGINT NOT NULL AUTO_INCREMENT,
    message_title VARCHAR(200) NOT NULL,
    message_description VARCHAR(500),
    payment_information VARCHAR(1000) NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE payouts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    semester VARCHAR(20) NOT NULL,
    total_income BIGINT NOT NULL,
    total_expense BIGINT NOT NULL,
    net_profit BIGINT NOT NULL,
    profit_rate DOUBLE NOT NULL,
    project_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_payouts_project_id (project_id),
    CONSTRAINT fk_payouts_project FOREIGN KEY (project_id) REFERENCES projects (id)
) ENGINE=InnoDB;

CREATE TABLE payout_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    payout_id BIGINT NOT NULL,
    type ENUM('EXPENSE','INCOME') NOT NULL,
    name VARCHAR(100) NOT NULL,
    amount BIGINT NOT NULL,
    category VARCHAR(50),
    PRIMARY KEY (id),
    CONSTRAINT fk_payout_items_payout FOREIGN KEY (payout_id) REFERENCES payouts (id)
) ENGINE=InnoDB;

CREATE TABLE forms (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    `open` BIT(1) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE questions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    label VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE form_questions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    form_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    question_order INT NOT NULL,
    answer_type ENUM('FILE','SELECT','TEXT') NOT NULL,
    required BIT(1) NOT NULL,
    section_type ENUM('COMMON','DEPARTMENT') NOT NULL,
    department_type ENUM('DESIGN','FINANCE','MARKETING','OPERATION'),
    select_options VARCHAR(2000),
    PRIMARY KEY (id),
    CONSTRAINT fk_form_questions_form FOREIGN KEY (form_id) REFERENCES forms (id),
    CONSTRAINT fk_form_questions_question FOREIGN KEY (question_id) REFERENCES questions (id)
) ENGINE=InnoDB;

CREATE TABLE form_notice (
    id BIGINT NOT NULL AUTO_INCREMENT,
    form_id BIGINT NOT NULL,
    section_type ENUM('COMMON','DEPARTMENT') NOT NULL,
    department_type ENUM('DESIGN','FINANCE','MARKETING','OPERATION'),
    title VARCHAR(100) NOT NULL,
    content TEXT,
    PRIMARY KEY (id),
    CONSTRAINT fk_form_notice_form FOREIGN KEY (form_id) REFERENCES forms (id)
) ENGINE=InnoDB;

CREATE TABLE applications (
    id BIGINT NOT NULL AUTO_INCREMENT,
    form_id BIGINT NOT NULL,
    student_id VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_department ENUM('DESIGN','FINANCE','MARKETING','OPERATION') NOT NULL,
    second_department ENUM('DESIGN','FINANCE','MARKETING','OPERATION') NOT NULL,
    result_status ENUM('FAIL','NOT_PUBLISHED','PASS') NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_applications_form_student (form_id, student_id),
    CONSTRAINT fk_applications_form FOREIGN KEY (form_id) REFERENCES forms (id)
) ENGINE=InnoDB;

CREATE TABLE answers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    application_id BIGINT NOT NULL,
    form_question_id BIGINT NOT NULL,
    `value` VARCHAR(2000) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_answers_application FOREIGN KEY (application_id) REFERENCES applications (id),
    CONSTRAINT fk_answers_form_question FOREIGN KEY (form_question_id) REFERENCES form_questions (id)
) ENGINE=InnoDB;

CREATE TABLE sns_links (
    id BIGINT NOT NULL AUTO_INCREMENT,
    type ENUM('INSTAGRAM','KAKAO') NOT NULL,
    url VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sns_links_type (type)
) ENGINE=InnoDB;
