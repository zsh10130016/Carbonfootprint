package com.zhangsihan.carbonfootprint.controller;

import com.zhangsihan.carbonfootprint.common.ApiResponse;
import com.zhangsihan.carbonfootprint.dto.CarbonRecordCreateRequest;
import com.zhangsihan.carbonfootprint.dto.CarbonRecordQueryRequest;
import com.zhangsihan.carbonfootprint.service.CarbonRecordService;
import com.zhangsihan.carbonfootprint.vo.CarbonRecordVO;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class RecordController {

    private final CarbonRecordService carbonRecordService;

    @PostMapping
    public ApiResponse<CarbonRecordVO> create(@Valid @RequestBody CarbonRecordCreateRequest request) {
        return ApiResponse.success("记录创建成功", carbonRecordService.createRecord(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<CarbonRecordVO> update(@PathVariable Long id, @Valid @RequestBody CarbonRecordCreateRequest request) {
        return ApiResponse.success("记录更新成功", carbonRecordService.updateRecord(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        carbonRecordService.deleteRecord(id);
        return ApiResponse.success("记录删除成功", null);
    }

    @GetMapping
    public ApiResponse<List<CarbonRecordVO>> list(CarbonRecordQueryRequest queryRequest) {
        return ApiResponse.success(carbonRecordService.listRecords(queryRequest));
    }

    @GetMapping("/{id}")
    public ApiResponse<CarbonRecordVO> detail(@PathVariable Long id) {
        return ApiResponse.success(carbonRecordService.getRecord(id));
    }
}
