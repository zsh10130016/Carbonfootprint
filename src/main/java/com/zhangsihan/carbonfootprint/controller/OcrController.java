package com.zhangsihan.carbonfootprint.controller;

import com.zhangsihan.carbonfootprint.common.ApiResponse;
import com.zhangsihan.carbonfootprint.service.OcrService;
import com.zhangsihan.carbonfootprint.vo.OcrParseResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ocr")
@RequiredArgsConstructor
public class OcrController {

    private final OcrService ocrService;

    @PostMapping(value = "/parse", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<OcrParseResultVO> parse(
            @RequestParam("documentType") String documentType,
            @RequestParam("file") MultipartFile file) {
        return ApiResponse.success(ocrService.parse(documentType, file));
    }
}
