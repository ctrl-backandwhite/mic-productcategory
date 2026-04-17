package com.backandwhite.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;

@Log4j2
@Testcontainers
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@Import(TestContainersConfiguration.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseIntegration {

    /**
     * PL/pgSQL block that truncates every public table except infrastructure ones
     * (Hibernate, Flyway, Liquibase). All dynamic SQL stays inside PL/pgSQL using
     * {@code quote_ident()}, so Java never builds a dynamic query string.
     */
    private static final String TRUNCATE_ALL_SQL = """
            DO $$
            DECLARE
                _tables text;
            BEGIN
                SELECT string_agg(quote_ident(tablename), ', ')
                  INTO _tables
                  FROM pg_tables
                 WHERE schemaname = 'public'
                   AND tablename NOT LIKE 'hibernate\\_%'
                   AND tablename NOT LIKE 'flyway\\_%'
                   AND tablename NOT LIKE 'liquibase\\_%';
                IF _tables IS NOT NULL THEN
                    EXECUTE 'TRUNCATE TABLE ' || _tables || ' RESTART IDENTITY CASCADE';
                END IF;
            END $$;
            """;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @LocalServerPort
    private int port;

    protected WebTestClient webTestClient;

    static {
        org.testcontainers.utility.TestcontainersConfiguration.getInstance()
                .updateUserConfig("checks.disable", "true");
    }

    @BeforeEach
    public void cleanAllTables() {
        if (webTestClient == null) {
            webTestClient = WebTestClient.bindToServer()
                    .baseUrl("http://localhost:" + port)
                    .build();
        }
        log.debug("Starting cleanup of all tables...");

        try {
            jdbcTemplate.execute(TRUNCATE_ALL_SQL);
        } catch (Exception e) {
            log.error("Error truncating tables: {}", e.getMessage());
        }
        log.debug("Table cleanup completed.");
    }
}
