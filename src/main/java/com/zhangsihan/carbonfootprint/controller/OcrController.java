package com.zhangsihan.carbonfootprint.controller;

import com.zhangsihan.carbonfootprint.common.ApiResponse;
import com.zhangsihan.carbonfootprint.dto.OcrParseRequest;
import com.zhangsihan.carbonfootprint.service.OcrService;
import com.zhangsihan.carbonfootprint.vo.OcrParseResultVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ocr")
@RequiredArgsConstructor
public class OcrController {

    private final OcrService ocrService;

    @PostMapping("/parse")
    public ApiResponse<OcrParseResultVO> parse(@Valid @RequestBody OcrParseRequest request) {
        return ApiResponse.success(ocrService.parse(request));
    }
}
