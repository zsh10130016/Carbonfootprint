package com.zhangsihan.carbonfootprint.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;
import org.springframework.util.unit.DataSize;

@Data
@ConfigurationProperties(prefix = "app.baidu-ocr")
public class BaiduOcrProperties {

    private String apiKey = "";
    private String secretKey = "";
    private String tokenUrl = "https://aip.baidubce.com/oauth/2.0/token";
    private String ocrUrl = "https://aip.baidubce.com/rest/2.0/ocr/v1/general_basic";
    private DataSize maxUploadSize = DataSize.ofMegabytes(4);

    public boolean enabled() {
        return StringUtils.hasText(apiKey) && StringUtils.hasText(secretKey);
    }
}
