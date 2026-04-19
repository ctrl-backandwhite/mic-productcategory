package com.backandwhite.infrastructure.configuration;

import com.backandwhite.common.constants.AppConstants;
import com.backandwhite.common.currency.CurrencyRateCache;
import com.backandwhite.common.currency.CurrencyRequestFilter;
import com.backandwhite.common.currency.PriceConversionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration for currency conversion support.
 * <p>
 * Registers:
 * <ul>
 * <li>{@link CurrencyRequestFilter} — reads X-Currency header</li>
 * <li>{@link CurrencyRateCache} — caches exchange rates from
 * mic-cmsservice</li>
 * <li>{@link PriceConversionService} — USD→target conversion</li>
 * <li>{@link RestTemplate} — HTTP client for CMS rate fetching</li>
 * </ul>
 */
@Configuration
public class CurrencyConfig implements WebMvcConfigurer {

    @Value("${services.cms.url:http://localhost:6006}")
    private String cmsServiceUrl;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CurrencyRequestFilter()).addPathPatterns("/api/**");
    }

    @Bean
    public RestTemplate cmsRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(5000);
        RestTemplate rt = new RestTemplate(factory);
        // Add X-nx036-auth header required by downstream services' NxRequestFilter
        rt.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().add(AppConstants.HEADER_NX036_AUTH, "service");
            return execution.execute(request, body);
        });
        return rt;
    }

    @Bean
    public CurrencyRateCache currencyRateCache(RestTemplate cmsRestTemplate) {
        return new CurrencyRateCache(cmsServiceUrl, cmsRestTemplate);
    }

    @Bean
    public PriceConversionService priceConversionService() {
        return new PriceConversionService();
    }
}
