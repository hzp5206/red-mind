package com.redmind.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "redmind.ai")
public class RedMindAiProperties {

    private String provider = "mock";
    private String baseUrl;
    private String apiKey;
    private String model = "gpt-4o-mini";
    private String chatPath = "/v1/chat/completions";
}
