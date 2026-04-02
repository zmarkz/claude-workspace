package com.portfolio.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "anthropic")
@Data
public class AnthropicConfig {

    private String apiKey;
    private String model = "claude-sonnet-4-6";
    private int maxTokens = 2048;
}
