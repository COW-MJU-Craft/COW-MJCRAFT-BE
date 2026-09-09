-- =====================================================================
-- V4: 유일성 DB 제약 추가 + 단일 행 테이블 시드 (이슈 #135)
-- 애플리케이션 레벨 검증만으로 유일성을 보장하던 지점에 DB 제약을 추가하고,
-- "행이 하나만 있어야 하는" 테이블(order_complete_pages, introduce, recruit_settings)의
-- 동시성 문제를 해결한다.
-- =====================================================================

-- 1) answers(application_id, form_question_id) UNIQUE
--    운영 반영 전 아래 쿼리로 기존 중복 데이터 유무를 반드시 먼저 확인할 것:
--      SELECT application_id, form_question_id, COUNT(*)
--      FROM answers GROUP BY application_id, form_question_id HAVING COUNT(*) > 1;
ALTER TABLE answers
    ADD UNIQUE KEY uk_answers_application_form_question (application_id, form_question_id);

-- 2) form_questions(form_id, question_order) UNIQUE
--    addQuestion은 이미 사전 검증을 하고, updateFormQuestion도 이번 PR에서 사전 검증을 추가함.
--    배치 재정렬 API가 없어 mid-transaction 충돌 위험이 없다.
ALTER TABLE form_questions
    ADD UNIQUE KEY uk_form_questions_form_order (form_id, question_order);

-- 3) item_images(item_id, sort_order) UNIQUE
--    AdminItemService.patchImageOrder를 2단계 재배치(임시 음수값 → 최종값)로 이번 PR에서 함께 수정.
ALTER TABLE item_images
    ADD UNIQUE KEY uk_item_images_item_sort (item_id, sort_order);

-- 4) order_complete_pages: 낙관적 락(version) 컬럼 추가 + 단일 행 시드
ALTER TABLE order_complete_pages
    ADD COLUMN version INT NOT NULL DEFAULT 0;

-- 4-1) applications: 낙관적 락(version) 컬럼 추가
ALTER TABLE applications
    ADD COLUMN version INT NOT NULL DEFAULT 0;

INSERT INTO order_complete_pages (message_title, message_description, payment_information, version, created_at, updated_at)
SELECT '주문이 완료되었습니다', NULL, '결제 정보를 입력해주세요.', 0, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM order_complete_pages);

-- 5) introduce: 단일 행 시드 (version 컬럼은 V1__baseline.sql에 이미 존재)
INSERT INTO introduce (title, subtitle, summary, hero_logo_keys, sections, version, created_at, updated_at)
SELECT '명지공방', NULL, NULL, NULL,
       '{"intro":{"title":null,"slogan":null,"body":null},"purpose":{"title":"","description":null},"currentLogo":{"title":"","imageKey":null,"description":null},"logoHistories":[]}',
       0, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM introduce);

-- 6) recruit_settings: "활성 폼은 항상 1개"를 원자적으로 보장하기 위한 락 전용 단일 행 테이블
CREATE TABLE recruit_settings (
    id BIGINT NOT NULL,
    active_form_id BIGINT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_recruit_settings_active_form FOREIGN KEY (active_form_id) REFERENCES forms (id)
) ENGINE=InnoDB;

INSERT INTO recruit_settings (id, active_form_id, created_at, updated_at)
SELECT 1, (SELECT id FROM forms WHERE `open` = TRUE LIMIT 1), NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM recruit_settings);
