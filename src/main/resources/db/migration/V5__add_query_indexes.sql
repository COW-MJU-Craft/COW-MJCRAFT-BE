-- =====================================================================
-- V5: 주요 조회 조건에 인덱스 추가 (이슈 #136)
--
-- 다음 항목은 이미 인덱스가 있어 이번 마이그레이션 대상에서 제외한다:
--   - answers(application_id, form_question_id), form_questions(form_id, question_order)
--     → #135(V4)에서 UNIQUE KEY로 이미 추가됨
--   - notice_images(notice_id, sort_order) → PK 자체가 이 조합
--   - form_notice(form_id) → FK 자동 생성 인덱스로 이미 커버됨
--
-- order_view_tokens/project_items/payout_items의 FK 자동 인덱스(단일 컬럼)는
-- 아래 복합 인덱스와 leftmost prefix가 겹쳐 다소 중복되지만, 자동 생성된 인덱스의
-- 정확한 이름을 마이그레이션에 하드코딩해 DROP하는 건 이름을 잘못 짚을 리스크가 있어
-- 하지 않는다. 중복 인덱스의 여분 쓰기 비용은 미미하다.
-- =====================================================================

-- orders: 관리자 주문 목록 조회 가속 (AdminOrderQueryService.getOrders)
ALTER TABLE orders
    ADD INDEX idx_orders_status_created_at_id (status, created_at, id);

ALTER TABLE orders
    ADD INDEX idx_orders_created_at_id (created_at, id);

-- order_view_tokens: 상태 전이마다 호출되는 revokeActiveTokens(order_id, revoked_at IS NULL) 가속
ALTER TABLE order_view_tokens
    ADD INDEX idx_order_view_tokens_order_revoked (order_id, revoked_at);

-- project_items: 프로젝트별 상품 목록(생성일 역순 정렬) 조회 가속
ALTER TABLE project_items
    ADD INDEX idx_project_items_project_created_at_id (project_id, created_at, id);

-- payout_items: 정산 항목 유형별 조회 가속
ALTER TABLE payout_items
    ADD INDEX idx_payout_items_payout_type (payout_id, type);

-- refresh_tokens: 기존 (subject, role) 인덱스를 revoked_at/expires_at 조건까지 포함하도록 확장
ALTER TABLE refresh_tokens
    DROP INDEX idx_refresh_tokens_subject_role;

ALTER TABLE refresh_tokens
    ADD INDEX idx_refresh_tokens_subject_role_revoked_expires (subject, role, revoked_at, expires_at);
