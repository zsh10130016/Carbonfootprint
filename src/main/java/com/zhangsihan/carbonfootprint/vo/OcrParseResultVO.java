package com.zhangsihan.carbonfootprint.vo;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OcrParseResultVO {
    private boolean supported;
    private String provider;
    private String documentType;
    private String message;
    private Map<String, String> fields;
    private List<String> recognizedText;
}
