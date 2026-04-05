package com.zhangsihan.carbonfootprint.service;

import com.zhangsihan.carbonfootprint.entity.AdviceRule;
import com.zhangsihan.carbonfootprint.mapper.AdviceRuleMapper;
import com.zhangsihan.carbonfootprint.mapper.CarbonRecordMapper;
import com.zhangsihan.carbonfootprint.security.SecurityUtils;
import com.zhangsihan.carbonfootprint.vo.AdviceVO;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdviceService {

    private final AdviceRuleMapper adviceRuleMapper;
    private final CarbonRecordMapper carbonRecordMapper;

    public List<AdviceVO> getAdviceList() {
        Long userId = SecurityUtils.getCurrentUserId();
        return adviceRuleMapper.findAll().stream()
                .map(rule -> buildAdvice(userId, rule))
                .filter(item -> item != null)
                .toList();
    }

    private AdviceVO buildAdvice(Long userId, AdviceRule rule) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(rule.getPeriodDays() - 1L);
        BigDecimal actual = carbonRecordMapper.sumEmissionByActivitySince(userId, rule.getActivityType(), startTime);
        if (actual == null || actual.compareTo(rule.getThresholdKg()) <= 0) {
            return null;
        }
        return AdviceVO.builder()
                .activityType(rule.getActivityType())
                .title(rule.getTitle())
                .description(rule.getDescription())
                .suggestion(rule.getSuggestion())
                .actualEmission(actual)
                .threshold(rule.getThresholdKg())
                .periodDays(rule.getPeriodDays())
                .build();
    }
}
