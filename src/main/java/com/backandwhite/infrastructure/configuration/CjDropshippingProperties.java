package com.backandwhite.infrastructure.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "cj-dropshipping")
public class CjDropshippingProperties {

    private String baseUrl;
    private String apiKey;
    private Discovery discovery = new Discovery();

    @Data
    public static class Discovery {
        private boolean enabled = false;
        private String cronFull = "0 0 2 * * SUN";
        private String cronIncremental = "0 0 4 * * *";
        private String cronEnrich = "0 0 */6 * * *";
        private int batchSizeEnrich = 500;
        private int maxPagesPerCategory = 200;
        private int maxPagesPerKeyword = 100;
        private int pageSize = 100;
        private long rateLimitWaitMs = 500;
        private List<String> keywords = new ArrayList<>();
    }
}
