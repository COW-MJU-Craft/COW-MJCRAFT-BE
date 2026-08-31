package com.example.cowmjucraft.global.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.zaxxer.hikari.HikariConfig;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;

@SpringBootTest
class DatabaseRuntimeConfigurationTest {

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void 공통설정_트랜잭션기본Timeout30초를적용한다() {
        // when & then
        assertThat(transactionManager).isInstanceOf(AbstractPlatformTransactionManager.class);
        AbstractPlatformTransactionManager abstractTransactionManager =
                (AbstractPlatformTransactionManager) transactionManager;
        assertThat(abstractTransactionManager.getDefaultTimeout()).isEqualTo(30);
    }

    @Test
    void 공통설정_트랜잭션Timeout을명시한다() throws IOException {
        // given
        ConfigurableEnvironment environment = load("application.yml");

        // when
        Duration transactionTimeout = Binder.get(environment)
                .bind("spring.transaction.default-timeout", Bindable.of(Duration.class))
                .get();

        // then
        assertThat(transactionTimeout).isEqualTo(Duration.ofSeconds(30));
    }

    @Test
    void 로컬설정_Hikari와락대기Timeout을명시한다() throws IOException {
        // given
        ConfigurableEnvironment environment = load("application-local.yml");

        // when & then
        assertHikariProperties(environment, 5, 1, 5_000L, 1_800_000L);
    }

    @Test
    void 운영설정_Hikari와락대기Timeout을명시한다() throws IOException {
        // given
        ConfigurableEnvironment environment = load("application-prod.yml");

        // when & then
        assertHikariProperties(environment, 20, 5, 3_000L, 1_800_000L);
    }

    private ConfigurableEnvironment load(String resourceName) throws IOException {
        ConfigurableEnvironment environment = new MockEnvironment();
        YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
        FileSystemResource resource = new FileSystemResource("src/main/resources/" + resourceName);
        List<PropertySource<?>> propertySources = loader.load(resourceName, resource);
        propertySources.forEach(propertySource -> environment.getPropertySources().addLast(propertySource));
        return environment;
    }

    private void assertHikariProperties(
            ConfigurableEnvironment environment,
            int maximumPoolSize,
            int minimumIdle,
            long connectionTimeout,
            long maxLifetime
    ) {
        HikariConfig hikariConfig = Binder.get(environment)
                .bind("spring.datasource.hikari", Bindable.of(HikariConfig.class))
                .get();

        assertThat(hikariConfig.getMaximumPoolSize()).isEqualTo(maximumPoolSize);
        assertThat(hikariConfig.getMinimumIdle()).isEqualTo(minimumIdle);
        assertThat(hikariConfig.getConnectionTimeout()).isEqualTo(connectionTimeout);
        assertThat(hikariConfig.getMaxLifetime()).isEqualTo(maxLifetime);
        assertThat(hikariConfig.getConnectionInitSql())
                .isEqualTo("SET SESSION innodb_lock_wait_timeout = 10");
    }
}
