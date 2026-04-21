package com.backandwhite.config;

import com.backandwhite.common.constants.AppConstants;
import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.util.Collections;
import java.util.Enumeration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

/**
 * Integration-test auth shim.
 *
 * <p>
 * The service stopped validating JWTs locally in commit 111f519 and now trusts
 * the gateway-forwarded headers ({@code X-nx036-auth}, {@code X-Auth-Subject},
 * {@code X-Auth-Roles}). Existing integration tests still send a Bearer token,
 * so the {@code NxGatewayAuthFilter} sees no {@code X-Auth-Roles} and assigns
 * {@code ROLE_GUEST} — triggering 403s on every admin endpoint.
 *
 * <p>
 * This highest-priority servlet filter wraps each request during tests to
 * guarantee the three headers the filter expects are present, so the security
 * chain grants {@code ROLE_ADMIN} as the tests intend. The wrapper only
 * supplies a header when the real request doesn't already carry it — tests that
 * explicitly set a role (eg. {@code ROLE_USER}) keep full control.
 */
@TestConfiguration(proxyBeanMethods = false)
public class IntegrationGatewayAuthConfiguration {

    @Bean
    FilterRegistrationBean<Filter> integrationGatewayAuthFilter() {
        FilterRegistrationBean<Filter> reg = new FilterRegistrationBean<>();
        reg.setFilter((request, response, chain) -> {
            HttpServletRequest httpReq = (HttpServletRequest) request;
            HttpServletRequestWrapper wrapped = new HttpServletRequestWrapper(httpReq) {
                @Override
                public String getHeader(String name) {
                    String existing = super.getHeader(name);
                    if (existing != null)
                        return existing;
                    if (AppConstants.HEADER_NX036_AUTH.equalsIgnoreCase(name))
                        return "test";
                    if (AppConstants.HEADER_AUTH_SUBJECT.equalsIgnoreCase(name))
                        return "integration-test-user";
                    if (AppConstants.HEADER_AUTH_ROLES.equalsIgnoreCase(name))
                        return "ROLE_ADMIN";
                    return null;
                }

                @Override
                public Enumeration<String> getHeaders(String name) {
                    Enumeration<String> existing = super.getHeaders(name);
                    if (existing != null && existing.hasMoreElements())
                        return existing;
                    String synthetic = getHeader(name);
                    return synthetic != null
                            ? Collections.enumeration(Collections.singletonList(synthetic))
                            : Collections.emptyEnumeration();
                }
            };
            chain.doFilter(wrapped, response);
        });
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return reg;
    }
}
