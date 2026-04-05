package com.zhangsihan.carbonfootprint.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OcrParseRequest {
    @NotBlank(message = "票据类型不能为空")
    private String documentType;

    private String rawText;
}
