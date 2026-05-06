package com.zhangsihan.carbonfootprint.service;

import com.zhangsihan.carbonfootprint.vo.OcrParseResultVO;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class OcrService {

    private final BaiduOcrClient baiduOcrClient;
    private final OcrFieldExtractor fieldExtractor;

    public OcrParseResultVO parse(String documentType, MultipartFile file) {
        List<String> words = baiduOcrClient.recognize(file);
        Map<String, String> fields = fieldExtractor.extractFields(documentType, words);
        boolean complete = fields.containsKey("subType") && fields.containsKey("amount");

        return OcrParseResultVO.builder()
                .supported(true)
                .provider("百度 OCR")
                .documentType(documentType.trim().toUpperCase())
                .message(complete ? "识别成功，已提取可用于碳记录的字段。" : "识别成功，但部分字段需要人工补充。")
                .fields(fields)
                .recognizedText(words)
                .build();
    }
}
