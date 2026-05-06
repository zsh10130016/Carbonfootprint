package com.zhangsihan.carbonfootprint.config;

import java.time.Duration;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@Data
@ConfigurationProperties(prefix = "app.deepseek")
public class DeepSeekProperties {

    private String apiKey;
    private String model = "deepseek-v4-flash";
    private String chatUrl = "https://api.deepseek.com/chat/completions";
    private Duration readTimeout = Duration.ofSeconds(60);

    public boolean hasCredentials() {
        return StringUtils.hasText(apiKey);
    }
}
