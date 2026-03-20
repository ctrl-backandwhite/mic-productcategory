package com.backandwhite.infrastructure.configuration;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "cj-dropshipping")
public class CjDropshippingProperties {

    @Value("${cj-dropshipping.api.url}")
    private String baseUrl;
    @Value("${cj-dropshipping.api.key}")
    private String apiKey;
}
