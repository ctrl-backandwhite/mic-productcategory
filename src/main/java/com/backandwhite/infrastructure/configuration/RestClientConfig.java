package com.backandwhite.infrastructure.configuration;

import com.backandwhite.domain.exception.Message;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient cjRestClient(CjDropshippingProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, (request, response) -> {
                    if (response.getStatusCode().value() == 429) {
                        throw Message.EXTERNAL_SERVICE_RATE_LIMIT.toExternalServiceException("CJ Dropshipping");
                    }
                    throw Message.EXTERNAL_SERVICE_DATA_ERROR.toExternalServiceException(
                            "CJ Dropshipping", "HTTP " + response.getStatusCode().value());
                })
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, (request, response) -> {
                    throw Message.EXTERNAL_SERVICE_DATA_ERROR.toExternalServiceException(
                            "CJ Dropshipping", "HTTP " + response.getStatusCode().value());
                })
                .build();
    }
}
