package com.backandwhite.application.configuration;

import com.backandwhite.common.security.NxGatewayAuthFilter;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Security for the productcategory microservice.
 *
 * <p>
 * The API Gateway validates the JWT upstream and forwards identity via
 * {@code X-Auth-Subject}, {@code X-Auth-Roles} and the handshake header
 * {@code X-nx036-auth}. The {@link NxGatewayAuthFilter} promotes those headers
 * into a Spring {@code Authentication} so {@code @PreAuthorize},
 * {@code @NxAdmin}, {@code @NxUser} and {@code @NxPublic} keep working.
 *
 * <p>
 * We intentionally do NOT plug in {@code oauth2ResourceServer(jwt(...))}: the
 * storefront OAuth2 client is issued HS256 tokens while the only
 * {@code jwk-set-uri} exposed locally publishes RSA keys, so any JWT validation
 * at this layer would reject every request from legit users. Signature
 * verification is the gateway's responsibility; this service trusts the headers
 * it receives with the shared-secret handshake header.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // NOSONAR java:S4502 — stateless REST, no cookie sessions
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers.httpStrictTransportSecurity(
                        hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000).preload(true)))
                .addFilterBefore(new NxGatewayAuthFilter(), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth.requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/api/v1/public/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/media/images/**").permitAll().anyRequest()
                        .authenticated())
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5174", "http://localhost:9000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
