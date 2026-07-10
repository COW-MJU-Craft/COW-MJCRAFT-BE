package com.example.cowmjucraft.migration;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.cowmjucraft.domain.project.entity.Project;
import com.example.cowmjucraft.domain.project.entity.ProjectCategory;
import com.example.cowmjucraft.domain.project.entity.ProjectStatus;
import com.example.cowmjucraft.domain.project.repository.ProjectRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * 실제 MySQL에 Flyway 마이그레이션을 전부 적용한 뒤
 * Hibernate validate로 엔티티-스키마 일치를 검증한다.
 *
 * - context 기동 자체가 검증: flyway 적용 실패 또는 validate 불일치 시 기동 실패
 * - V1__baseline.sql이 엔티티와 어긋나면 이 테스트가 CI에서 잡아낸다
 * - 로컬에 Docker가 없으면 자동 skip (CI runner에는 Docker 내장)
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
class MySqlMigrationVerificationTest {

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4");

    @DynamicPropertySource
    static void overrideDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.MySQLDialect");
    }

    @Autowired
    private ProjectRepository projectRepository;

    @Test
    void 마이그레이션_적용_후_엔티티와_스키마가_일치한다() {
        // context 기동 성공 = flyway 전체 적용 + hibernate validate 통과
    }

    @Test
    void 마이그레이션된_스키마에서_핵심_리포지토리가_동작한다() {
        Project project = new Project(
                "테스트 프로젝트",
                "요약",
                "설명",
                "thumb/key.png",
                List.of("images/a.png"),
                LocalDate.now().plusDays(7),
                ProjectStatus.OPEN,
                ProjectCategory.GOODS
        );

        Long id = projectRepository.save(project).getId();

        assertThat(projectRepository.findById(id))
                .isPresent()
                .hasValueSatisfying(found -> assertThat(found.getTitle()).isEqualTo("테스트 프로젝트"));
    }
}
