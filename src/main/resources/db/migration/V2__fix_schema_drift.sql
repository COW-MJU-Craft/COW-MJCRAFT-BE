-- =====================================================================
-- V2: 운영 스키마 드리프트 수정 (2026-07-11 운영 DDL 대조에서 발견)
-- ddl-auto: update가 기존 컬럼을 변경하지 않아 누적된 엔티티↔운영 불일치.
-- 둘 다 데이터 손실 없는 확장(widening)만 수행.
-- =====================================================================

-- 1) SectionType.BASIC이 엔티티 enum에는 있으나 운영 컬럼 enum에 없음.
--    BASIC 값을 저장하려는 순간 운영에서 SQL 오류가 나는 잠재 버그.
ALTER TABLE form_questions
    MODIFY section_type ENUM('BASIC','COMMON','DEPARTMENT') NOT NULL;

ALTER TABLE form_notice
    MODIFY section_type ENUM('BASIC','COMMON','DEPARTMENT') NOT NULL;

-- 2) feedbacks.answer가 TINYTEXT(최대 255바이트 ≈ 한글 85자) — 답변 저장 시 잘림 위험.
--    엔티티(@Lob)의 기대 타입인 LONGTEXT로 확장.
ALTER TABLE feedbacks
    MODIFY answer LONGTEXT NULL;
