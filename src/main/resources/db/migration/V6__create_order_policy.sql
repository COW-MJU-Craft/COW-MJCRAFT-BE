-- =====================================================================
-- V6: 주문 정책 설정 테이블 신설 (V2 4-1 — 택배 배송비 자동 추가)
--
-- 배송비를 application.yml이 아닌 DB에 두는 이유: 관리자가 재배포 없이
-- 화면에서 바로 바꿀 수 있어야 함. #135의 order_complete_pages와 동일하게
-- 미리 시드된 단일 행 + 관리자 API update-only 패턴을 따른다.
-- =====================================================================

CREATE TABLE order_policy (
    id BIGINT NOT NULL AUTO_INCREMENT,
    default_shipping_fee INT NOT NULL,
    version INT NOT NULL DEFAULT 0,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

INSERT INTO order_policy (default_shipping_fee, version, created_at, updated_at)
SELECT 3500, 0, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM order_policy);
