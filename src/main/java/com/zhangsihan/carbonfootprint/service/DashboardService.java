package com.zhangsihan.carbonfootprint.service;

import com.zhangsihan.carbonfootprint.mapper.CarbonRecordMapper;
import com.zhangsihan.carbonfootprint.mapper.PointsLedgerMapper;
import com.zhangsihan.carbonfootprint.security.SecurityUtils;
import com.zhangsihan.carbonfootprint.vo.CategoryStatVO;
import com.zhangsihan.carbonfootprint.vo.DashboardSummaryVO;
import com.zhangsihan.carbonfootprint.vo.SourceRatioVO;
import com.zhangsihan.carbonfootprint.vo.TrendPointVO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final CarbonRecordMapper carbonRecordMapper;
    private final PointsLedgerMapper pointsLedgerMapper;

    public DashboardSummaryVO getSummary() {
        Long userId = SecurityUtils.getCurrentUserId();
        BigDecimal totalEmission = carbonRecordMapper.sumEmissionByUserId(userId);
        BigDecimal weekEmission = carbonRecordMapper.sumEmissionSince(userId, LocalDate.now().minusDays(6).atStartOfDay());
        Integer totalPoints = pointsLedgerMapper.sumPointsByUserId(userId);
        Integer totalRecords = carbonRecordMapper.countByUserId(userId);
        String topSource = carbonRecordMapper.findTopSource(userId);
        return DashboardSummaryVO.builder()
                .totalEmission(defaultDecimal(totalEmission))
                .weekEmission(defaultDecimal(weekEmission))
                .totalPoints(totalPoints == null ? 0 : totalPoints)
                .totalRecords(totalRecords == null ? 0 : totalRecords)
                .topSource(topSource == null ? "暂无数据" : topSource)
                .build();
    }

    public List<TrendPointVO> getTrend() {
        Long userId = SecurityUtils.getCurrentUserId();
        LocalDate startDate = LocalDate.now().minusDays(6);
        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime endTime = LocalDate.now().plusDays(1).atStartOfDay();
        Map<String, BigDecimal> trendMap = new HashMap<>();
        for (TrendPointVO point : carbonRecordMapper.findTrend(userId, startTime, endTime)) {
            trendMap.put(point.getDate(), defaultDecimal(point.getEmissionKg()));
        }
        // Fill missing days with zero so the front end always renders a complete 7-day chart.
        return startDate.datesUntil(LocalDate.now().plusDays(1))
                .map(date -> TrendPointVO.builder()
                        .date(date.toString())
                        .emissionKg(trendMap.getOrDefault(date.toString(), BigDecimal.ZERO))
                        .build())
                .toList();
    }

    public List<SourceRatioVO> getSourceRatios() {
        return carbonRecordMapper.findSourceRatios(SecurityUtils.getCurrentUserId());
    }

    public List<CategoryStatVO> getCategoryStats() {
        return carbonRecordMapper.findCategoryStats(SecurityUtils.getCurrentUserId());
    }

    private BigDecimal defaultDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
