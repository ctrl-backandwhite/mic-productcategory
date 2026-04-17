package com.backandwhite.config;

import com.backandwhite.core.test.JwtTestUtil;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(value = "productCategoryTestContainersConfiguration", proxyBeanMethods = false)
public class TestContainersConfiguration {

    private static final byte[] SECRET = "test-secret-key-for-jwt-test-util-must-be-at-least-32-bytes!".getBytes();

    @Value("${test.postgres.username}")
    private String username;

    @Value("${test.postgres.password}")
    private String password;

    @Value("${test.postgres.database}")
    private String dbName;

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest")).withUsername(username)
                .withPassword(password).withDatabaseName(dbName);
    }

    @Bean
    @ConditionalOnMissingBean(JwtEncoder.class)
    JwtEncoder jwtEncoder() {
        SecretKey key = new SecretKeySpec(SECRET, "HmacSHA256");
        OctetSequenceKey jwk = new OctetSequenceKey.Builder(key).algorithm(JWSAlgorithm.HS256).build();
        return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(jwk)));
    }

    @Bean
    @ConditionalOnMissingBean(JwtDecoder.class)
    JwtDecoder jwtDecoder() {
        SecretKey key = new SecretKeySpec(SECRET, "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).build();
    }

    @Bean
    @Primary
    JwtTestUtil jwtTestUtil(JwtEncoder jwtEncoder) {
        return new JwtTestUtil(jwtEncoder, MacAlgorithm.HS256);
    }
}
