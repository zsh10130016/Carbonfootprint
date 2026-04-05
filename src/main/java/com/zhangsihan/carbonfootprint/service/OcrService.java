package com.zhangsihan.carbonfootprint.service;

import com.zhangsihan.carbonfootprint.dto.OcrParseRequest;
import com.zhangsihan.carbonfootprint.vo.OcrParseResultVO;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class OcrService {

    public OcrParseResultVO parse(OcrParseRequest request) {
        Map<String, String> fields = new LinkedHashMap<>();
        String type = request.getDocumentType().trim().toUpperCase();
        if ("TRANSPORT_TICKET".equals(type)) {
            fields.put("subType", "TRAIN");
            fields.put("amount", "15.00");
            fields.put("unit", "km");
        } else if ("UTILITY_BILL".equals(type)) {
            fields.put("subType", "ELECTRICITY");
            fields.put("amount", "18.00");
            fields.put("unit", "kWh");
        }
        return OcrParseResultVO.builder()
                .supported(true)
                .provider("mock")
                .documentType(type)
                .message("当前阶段使用 mock provider 预留 OCR 接口，后续可替换为百度 OCR。")
                .fields(fields)
                .build();
    }
}
