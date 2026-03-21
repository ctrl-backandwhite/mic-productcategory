package com.backandwhite.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestContainersConfiguration {

    @Value("${test.postgres.username}")
    private String username;

    @Value("${test.postgres.password}")
    private String password;

    @Value("${test.postgres.database}")
    private String dbName;

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"))
                .withUsername(username)
                .withPassword(password)
                .withDatabaseName(dbName);
    }
}
