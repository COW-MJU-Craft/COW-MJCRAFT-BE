-- 이슈 #129: 프로젝트·상품 삭제를 물리 삭제 대신 soft delete로 전환한다.
-- 주문(order_items)·정산(payout) 이력을 파괴하지 않도록 deleted_at 마커만 남기고,
-- 조회·신규 주문 경로에서 deleted_at IS NULL 인 행만 노출한다.
-- NULL = 살아있음, 값 존재 = 삭제됨(화면에서 숨김, DB에는 보존).
ALTER TABLE projects ADD COLUMN deleted_at DATETIME(6) NULL;
ALTER TABLE project_items ADD COLUMN deleted_at DATETIME(6) NULL;
