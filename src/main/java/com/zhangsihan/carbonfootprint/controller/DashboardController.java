package com.zhangsihan.carbonfootprint.controller;

import com.zhangsihan.carbonfootprint.common.ApiResponse;
import com.zhangsihan.carbonfootprint.service.DashboardService;
import com.zhangsihan.carbonfootprint.vo.CategoryStatVO;
import com.zhangsihan.carbonfootprint.vo.DashboardSummaryVO;
import com.zhangsihan.carbonfootprint.vo.SourceRatioVO;
import com.zhangsihan.carbonfootprint.vo.TrendPointVO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ApiResponse<DashboardSummaryVO> summary() {
        return ApiResponse.success(dashboardService.getSummary());
    }

    @GetMapping("/trend")
    public ApiResponse<List<TrendPointVO>> trend() {
        return ApiResponse.success(dashboardService.getTrend());
    }

    @GetMapping("/source-ratio")
    public ApiResponse<List<SourceRatioVO>> sourceRatio() {
        return ApiResponse.success(dashboardService.getSourceRatios());
    }

    @GetMapping("/categories")
    public ApiResponse<List<CategoryStatVO>> categories() {
        return ApiResponse.success(dashboardService.getCategoryStats());
    }
}
