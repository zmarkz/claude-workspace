package com.portfolio.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "kite")
@Data
public class KiteConfig {
    private String apiKey;
    private String apiSecret;
}
