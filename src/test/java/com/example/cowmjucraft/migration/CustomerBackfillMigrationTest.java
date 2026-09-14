package com.example.cowmjucraft.migration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * V11 backfill을 실제 데이터에 대해 검증한다.
 *
 * <p>V11은 기존 주문의 {@code order_buyer.email}로 고객을 만들고 모든 주문을 연결한 뒤
 * {@code orders.customer_id}를 NOT NULL로 올린다. 운영 데이터에 한 번만 실행되는
 * 코드라 실패하면 배포가 통째로 막히므로, 여기서 미리 돌려본다.
 *
 * <p>기존 마이그레이션 테스트는 빈 DB에 전체를 적용하므로 backfill이 아무 일도 하지
 * 않는다. 그래서 이 테스트는 <b>V10까지만 적용 → 데이터 삽입 → V11 적용</b> 순으로
 * Flyway를 두 번 나눠 실행한다.
 */
@Testcontainers(disabledWithoutDocker = true)
class CustomerBackfillMigrationTest {

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4");

    private static JdbcTemplate jdbcTemplate;

    @BeforeAll
    static void migrateWithSeedData() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(MYSQL.getJdbcUrl());
        dataSource.setUsername(MYSQL.getUsername());
        dataSource.setPassword(MYSQL.getPassword());
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        jdbcTemplate = new JdbcTemplate(dataSource);

        migrateTo(dataSource, "10");
        seedLegacyOrders();
        migrateTo(dataSource, "latest");
    }

    private static void migrateTo(DataSource dataSource, String target) {
        Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .target(target)
                .load()
                .migrate();
    }

    /** V11 이전 상태의 주문 3건. 앞 둘은 이메일 대소문자·공백만 다르다. */
    private static void seedLegacyOrders() {
        jdbcTemplate.update("""
                INSERT INTO projects (title, summary, description, thumbnail_key, deadline_date,
                                      status, category, pinned, last_order_no, created_at, updated_at)
                VALUES ('테스트 프로젝트', '요약', '설명', 'thumb.png', '2099-12-31',
                        'OPEN', 'GOODS', 0, 3, NOW(6), NOW(6))
                """);
        Long projectId = jdbcTemplate.queryForObject("SELECT id FROM projects LIMIT 1", Long.class);

        insertOrder(projectId, 1, "P1-1-A", "2026-01-01 10:00:00", "yunjin@mju.ac.kr");
        insertOrder(projectId, 2, "P1-2-B", "2026-02-01 10:00:00", "  YunJin@MJU.AC.KR  ");
        insertOrder(projectId, 3, "P1-3-C", "2026-03-01 10:00:00", "other@mju.ac.kr");
    }

    private static void insertOrder(Long projectId, int seq, String orderNo, String createdAt, String email) {
        jdbcTemplate.update("""
                INSERT INTO orders (order_no, representative_project_id, project_order_no, status,
                                    total_amount, shipping_fee, final_amount, deposit_deadline,
                                    depositor_name, privacy_agreed, privacy_agreed_at,
                                    refund_agreed, refund_agreed_at,
                                    cancel_risk_agreed, cancel_risk_agreed_at, created_at, updated_at)
                VALUES (?, ?, ?, 'PENDING_DEPOSIT', 10000, 0, 10000, ?, '입금자',
                        1, ?, 1, ?, 1, ?, ?, ?)
                """, orderNo, projectId, (long) seq, createdAt, createdAt, createdAt, createdAt, createdAt, createdAt);

        Long orderId = jdbcTemplate.queryForObject(
                "SELECT id FROM orders WHERE order_no = ?", Long.class, orderNo);

        jdbcTemplate.update("""
                INSERT INTO order_buyer (order_id, buyer_type, campus, name, department_or_major, student_no,
                                         phone, refund_bank, refund_account, referral_source, email, created_at)
                VALUES (?, 'STUDENT', 'SEOUL', '김윤진', '학부', '60201234',
                        ?, '국민은행', '12345601234567', NULL, ?, ?)
                """, orderId, "010-2345-678" + seq, email, createdAt);
    }

    @Test
    void backfill_대소문자만다른이메일은_같은고객으로합쳐진다() {
        // when
        List<Map<String, Object>> customers = jdbcTemplate.queryForList(
                "SELECT email FROM customers ORDER BY email");

        // then — 3건의 주문에서 고객은 2명만 생긴다
        assertThat(customers).hasSize(2);
        assertThat(customers).extracting(row -> row.get("email"))
                .containsExactly("other@mju.ac.kr", "yunjin@mju.ac.kr");
    }

    @Test
    void backfill_모든주문에_고객이연결된다() {
        // when
        Integer orphans = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM orders WHERE customer_id IS NULL", Integer.class);

        // then — NOT NULL로 올리기 전에 전부 채워져야 한다
        assertThat(orphans).isZero();
    }

    @Test
    void backfill_같은이메일의주문들은_같은고객을가리킨다() {
        // when
        Integer distinctCustomers = jdbcTemplate.queryForObject("""
                SELECT COUNT(DISTINCT o.customer_id)
                FROM orders o
                JOIN order_buyer b ON b.order_id = o.id
                WHERE LOWER(TRIM(b.email)) = 'yunjin@mju.ac.kr'
                """, Integer.class);

        // then
        assertThat(distinctCustomers).isEqualTo(1);
    }

    @Test
    void backfill_전화번호는_최근주문값을숫자만남겨채운다() {
        // when — yunjin의 최근 주문은 2026-02-01의 010-2345-6782
        String phone = jdbcTemplate.queryForObject(
                "SELECT phone FROM customers WHERE email = 'yunjin@mju.ac.kr'", String.class);

        // then
        assertThat(phone).isEqualTo("01023456782");
    }

    @Test
    void backfill_고객의_비밀번호와프로필은비어있다() {
        // when — 주문 이력만으로는 계정을 만들지 않는다
        Map<String, Object> customer = jdbcTemplate.queryForMap(
                "SELECT password_hash, profile_saved_at, email_verified_at FROM customers WHERE email = 'yunjin@mju.ac.kr'");

        // then
        assertThat(customer.get("password_hash")).isNull();
        assertThat(customer.get("profile_saved_at")).isNull();
        assertThat(customer.get("email_verified_at")).isNull();
    }

    @Test
    void backfill_customer_id는_NOT_NULL이고_외래키가걸린다() {
        // when
        String isNullable = jdbcTemplate.queryForObject("""
                SELECT IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'customer_id'
                """, String.class);
        Integer foreignKeys = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'orders'
                  AND CONSTRAINT_NAME = 'fk_orders_customer' AND CONSTRAINT_TYPE = 'FOREIGN KEY'
                """, Integer.class);

        // then
        assertThat(isNullable).isEqualTo("NO");
        assertThat(foreignKeys).isEqualTo(1);
    }
}
