package com.zhangsihan.carbonfootprint.config;

import java.time.Duration;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.email-verification")
public class EmailVerificationProperties {

    private Duration expiresIn = Duration.ofMinutes(10);
    private Duration resendInterval = Duration.ofSeconds(60);
    private String fixedCode;
    private String from;
}
