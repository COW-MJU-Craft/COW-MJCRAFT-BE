-- 프로젝트가 OPEN(출시)으로 전환될 때 자동 등록한 출시 공지의 ID를 기록한다.
-- NULL = 아직 출시 공지를 등록하지 않음, 값 존재 = 이미 등록함(재전환 시 중복 등록 방지).
-- 공지는 관리자가 독립적으로 삭제할 수 있으므로 FK는 두지 않는다.
ALTER TABLE projects ADD COLUMN launch_notice_id BIGINT NULL;
