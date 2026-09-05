-- V6 적용 이후에도 기존 DB에 주문 정책 단일 row가 누락된 환경을 보강한다.
INSERT INTO order_policy (default_shipping_fee, version, created_at, updated_at)
SELECT 3500, 0, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM order_policy);
