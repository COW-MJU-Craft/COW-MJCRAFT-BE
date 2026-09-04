ALTER TABLE projects
    ADD COLUMN last_order_no BIGINT NOT NULL DEFAULT 0;

ALTER TABLE orders
    ADD COLUMN representative_project_id BIGINT NULL,
    ADD COLUMN project_order_no BIGINT NULL;

-- 기존 주문은 가장 먼저 저장된 주문 상품의 프로젝트를 대표 프로젝트로 정한다.
UPDATE orders o
JOIN (
    SELECT first_item.order_id, pi.project_id
    FROM (
        SELECT order_id, MIN(id) AS first_order_item_id
        FROM order_items
        GROUP BY order_id
    ) first_item
    JOIN order_items oi ON oi.id = first_item.first_order_item_id
    JOIN project_items pi ON pi.id = oi.project_item_id
) representative ON representative.order_id = o.id
SET o.representative_project_id = representative.project_id;

-- 생성 시각과 내부 ID 순서로 기존 주문에 프로젝트별 순번을 부여한다.
CREATE TEMPORARY TABLE tmp_order_project_numbers AS
SELECT id AS order_id,
       ROW_NUMBER() OVER (
           PARTITION BY representative_project_id
           ORDER BY created_at, id
       ) AS project_order_no
FROM orders;

ALTER TABLE tmp_order_project_numbers
    ADD PRIMARY KEY (order_id);

UPDATE orders o
JOIN tmp_order_project_numbers numbers ON numbers.order_id = o.id
SET o.project_order_no = numbers.project_order_no;

DROP TEMPORARY TABLE tmp_order_project_numbers;

UPDATE projects p
LEFT JOIN (
    SELECT representative_project_id, MAX(project_order_no) AS last_order_no
    FROM orders
    GROUP BY representative_project_id
) numbers ON numbers.representative_project_id = p.id
SET p.last_order_no = COALESCE(numbers.last_order_no, 0);

-- 기존 주문번호의 6자리 난수는 유지하고 새 표시 형식으로 변환한다.
UPDATE orders
SET order_no = CONCAT(
        'P', representative_project_id,
        '-', project_order_no,
        '-', DATE_FORMAT(COALESCE(created_at, CURRENT_TIMESTAMP), '%m%d%H%i'),
        '-', RIGHT(order_no, 6)
    );

ALTER TABLE orders
    MODIFY COLUMN order_no VARCHAR(64) NOT NULL,
    MODIFY COLUMN representative_project_id BIGINT NOT NULL,
    MODIFY COLUMN project_order_no BIGINT NOT NULL,
    ADD CONSTRAINT fk_orders_representative_project
        FOREIGN KEY (representative_project_id) REFERENCES projects (id),
    ADD CONSTRAINT uk_orders_project_order_no
        UNIQUE (representative_project_id, project_order_no),
    ADD INDEX idx_orders_representative_project_status_created_at_id
        (representative_project_id, status, created_at, id);
