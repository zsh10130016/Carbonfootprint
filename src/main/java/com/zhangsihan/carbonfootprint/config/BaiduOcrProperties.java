package com.zhangsihan.carbonfootprint.config;

import java.time.Duration;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@Data
@ConfigurationProperties(prefix = "app.baidu-ocr")
public class BaiduOcrProperties {

    private String apiKey;
    private String secretKey;
    private String tokenUrl = "https://aip.baidubce.com/oauth/2.0/token";
    private String recognizeUrl = "https://aip.baidubce.com/rest/2.0/ocr/v1/general_basic";
    private Duration connectTimeout = Duration.ofSeconds(5);
    private Duration readTimeout = Duration.ofSeconds(20);
    private long maxImageSize = 8 * 1024 * 1024L;

    public boolean hasCredentials() {
        return StringUtils.hasText(apiKey) && StringUtils.hasText(secretKey);
    }
}
