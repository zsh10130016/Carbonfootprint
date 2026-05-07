package com.zhangsihan.carbonfootprint.service;

import com.zhangsihan.carbonfootprint.entity.AdviceRule;
import com.zhangsihan.carbonfootprint.mapper.AdviceRuleMapper;
import com.zhangsihan.carbonfootprint.mapper.CarbonRecordMapper;
import com.zhangsihan.carbonfootprint.security.SecurityUtils;
import com.zhangsihan.carbonfootprint.vo.AdviceVO;
import com.zhangsihan.carbonfootprint.vo.SubtypeEmissionStatVO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdviceService {

    private static final int PERIOD_DAYS = 7;
    private static final BigDecimal HOTSPOT_SHARE = BigDecimal.valueOf(0.55);
    private static final BigDecimal TREND_INCREASE_RATE = BigDecimal.valueOf(0.25);
    private static final BigDecimal MIN_ACTIVITY_TOTAL = BigDecimal.ONE;
    private static final String FACTOR_METHOD_BASIS = "依据 IPCC 温室气体清单方法中“活动数据 × 排放因子”的核算思路，并结合个人碳足迹管理场景设置提醒阈值。";
    private static final String HOTSPOT_BASIS = "依据 GHG Protocol 产品生命周期标准中的热点识别思路：优先提示贡献较高、可替代性较强的排放来源。";
    private static final String TREND_BASIS = "依据个人历史基线进行环比识别：当最近周期较上一周期明显上升时，提示用户复盘近期异常行为。";

    private final AdviceRuleMapper adviceRuleMapper;
    private final CarbonRecordMapper carbonRecordMapper;

    public List<AdviceVO> getAdviceList() {
        Long userId = SecurityUtils.getCurrentUserId();
        Map<String, AdviceVO> uniqueAdvice = new LinkedHashMap<>();
        adviceRuleMapper.findAll().stream()
                .map(rule -> buildRuleAdvice(userId, rule))
                .filter(item -> item != null)
                .forEach(item -> uniqueAdvice.putIfAbsent(buildAdviceKey(item), item));
        buildBehaviorAdvice(userId).forEach(item -> uniqueAdvice.putIfAbsent(buildAdviceKey(item), item));
        return uniqueAdvice.values().stream().toList();
    }

    private AdviceVO buildRuleAdvice(Long userId, AdviceRule rule) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(rule.getPeriodDays());
        BigDecimal actual = safe(carbonRecordMapper.sumEmissionByActivitySince(userId, rule.getActivityType(), startTime));
        if (actual.compareTo(rule.getThresholdKg()) <= 0) {
            return null;
        }
        return AdviceVO.builder()
                .activityType(rule.getActivityType())
                .title(rule.getTitle())
                .description(rule.getDescription())
                .suggestion(rule.getSuggestion())
                .actualEmission(scale(actual))
                .threshold(scale(rule.getThresholdKg()))
                .periodDays(rule.getPeriodDays())
                .metricLabel("近 " + rule.getPeriodDays() + " 天排放")
                .compareLabel("提醒阈值")
                .metricUnit("kgCO2e")
                .basis(FACTOR_METHOD_BASIS)
                .build();
    }

    private List<AdviceVO> buildBehaviorAdvice(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime currentStart = now.minusDays(PERIOD_DAYS);
        LocalDateTime previousStart = currentStart.minusDays(PERIOD_DAYS);
        List<AdviceVO> adviceList = new ArrayList<>();
        Map<String, BigDecimal> activityTotals = buildActivityTotals(userId, currentStart);
        Map<String, BigDecimal> subtypeTotals = buildSubtypeTotals(userId, currentStart);

        addTrendAdvice(userId, previousStart, currentStart, now, adviceList);
        addSubtypeAdvice(activityTotals, subtypeTotals, adviceList);
        return adviceList;
    }

    private Map<String, BigDecimal> buildActivityTotals(Long userId, LocalDateTime startTime) {
        Map<String, BigDecimal> totals = new LinkedHashMap<>();
        for (String activityType : List.of("TRANSPORT", "HOME_ENERGY", "FOOD")) {
            totals.put(activityType, safe(carbonRecordMapper.sumEmissionByActivitySince(userId, activityType, startTime)));
        }
        return totals;
    }

    private Map<String, BigDecimal> buildSubtypeTotals(Long userId, LocalDateTime startTime) {
        Map<String, BigDecimal> totals = new LinkedHashMap<>();
        for (SubtypeEmissionStatVO item : carbonRecordMapper.findSubtypeEmissionsSince(userId, startTime)) {
            totals.put(item.getActivityType() + ":" + item.getSubType(), safe(item.getEmissionKg()));
        }
        return totals;
    }

    private void addTrendAdvice(Long userId, LocalDateTime previousStart, LocalDateTime currentStart,
                                LocalDateTime now, List<AdviceVO> adviceList) {
        for (String activityType : List.of("TRANSPORT", "HOME_ENERGY", "FOOD")) {
            BigDecimal previous = safe(carbonRecordMapper.sumEmissionByActivityBetween(userId, activityType, previousStart, currentStart));
            BigDecimal current = safe(carbonRecordMapper.sumEmissionByActivityBetween(userId, activityType, currentStart, now));
            BigDecimal triggerThreshold = previous.multiply(BigDecimal.ONE.add(TREND_INCREASE_RATE));
            if (previous.compareTo(MIN_ACTIVITY_TOTAL) <= 0 || current.compareTo(triggerThreshold) <= 0) {
                continue;
            }
            BigDecimal increasePercent = current.subtract(previous)
                    .divide(previous, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            adviceList.add(simpleAdvice(
                    activityType,
                    activityLabel(activityType) + "排放环比上升",
                    "最近 7 天" + activityLabel(activityType) + "排放较上一周期上升约 "
                            + increasePercent.setScale(0, RoundingMode.HALF_UP) + "%，可能出现了新的高排放行为或临时事件。",
                    "建议回看最近几条记录，优先确认是否有可替代项目，例如短途机动出行、空调长时间运行或高碳食材消费。",
                    current,
                    triggerThreshold,
                    PERIOD_DAYS,
                    "本周期排放",
                    "环比提醒阈值",
                    "kgCO2e",
                    TREND_BASIS
            ));
        }
    }

    private void addSubtypeAdvice(Map<String, BigDecimal> activityTotals, Map<String, BigDecimal> subtypeTotals,
                                  List<AdviceVO> adviceList) {
        addHighShareAdvice("TRANSPORT", "TAXI", "出租车出行占比较高",
                "最近 7 天交通排放中，出租车/网约车贡献超过 55%，属于当前交通排放的主要来源。",
                "建议优先优化短途机动出行：3 公里以内尽量选择步行、骑行或公交；中长距离出行优先选择地铁、公交或铁路。",
                activityTotals, subtypeTotals, adviceList);
        addHighShareAdvice("HOME_ENERGY", "ELECTRICITY", "家庭用电为主要来源",
                "最近 7 天家庭用能中，用电贡献超过 55%，说明电器使用是当前家庭排放的重点。",
                "建议优先检查空调、热水器、照明和待机设备；夏季制冷温度可参考公共机构节能建议控制在不低于 26 摄氏度。",
                activityTotals, subtypeTotals, adviceList);
        addHighShareAdvice("HOME_ENERGY", "NATURAL_GAS", "天然气使用较集中",
                "最近 7 天家庭用能中，天然气贡献超过 55%，说明燃气热水或烹饪使用较集中。",
                "建议关注热水器和燃气灶使用时长，减少长时间空烧，定期检查燃气设备效率，并结合账单观察是否存在异常波动。",
                activityTotals, subtypeTotals, adviceList);
        addHighShareAdvice("FOOD", "BEEF", "高碳肉类贡献偏高",
                "最近 7 天饮食排放中，牛肉贡献超过 55%，属于饮食碳足迹的主要热点。",
                "建议参考平衡膳食思路，把部分红肉替换为鸡肉、鸡蛋、豆腐、蔬菜和谷物类食材，先从每周减少 1 到 2 次高碳肉类开始。",
                activityTotals, subtypeTotals, adviceList);
    }

    private void addHighShareAdvice(String activityType, String subType, String title, String description,
                                    String suggestion, Map<String, BigDecimal> activityTotals,
                                    Map<String, BigDecimal> subtypeTotals, List<AdviceVO> adviceList) {
        BigDecimal total = activityTotals.getOrDefault(activityType, BigDecimal.ZERO);
        BigDecimal actual = subtypeTotals.getOrDefault(activityType + ":" + subType, BigDecimal.ZERO);
        if (total.compareTo(MIN_ACTIVITY_TOTAL) <= 0 || actual.compareTo(total.multiply(HOTSPOT_SHARE)) <= 0) {
            return;
        }
        BigDecimal triggerThreshold = total.multiply(HOTSPOT_SHARE);
        adviceList.add(simpleAdvice(
                activityType,
                title,
                description,
                suggestion,
                actual,
                triggerThreshold,
                PERIOD_DAYS,
                "热点来源排放",
                "占比提醒阈值折算排放",
                "kgCO2e",
                HOTSPOT_BASIS
        ));
    }

    private AdviceVO simpleAdvice(String activityType, String title, String description, String suggestion,
                                  BigDecimal actual, BigDecimal threshold, int periodDays,
                                  String metricLabel, String compareLabel, String metricUnit, String basis) {
        return AdviceVO.builder()
                .activityType(activityType)
                .title(title)
                .description(description)
                .suggestion(suggestion)
                .actualEmission(scale(actual))
                .threshold(scale(threshold))
                .periodDays(periodDays)
                .metricLabel(metricLabel)
                .compareLabel(compareLabel)
                .metricUnit(metricUnit)
                .basis(basis)
                .build();
    }

    private String buildAdviceKey(AdviceVO advice) {
        return advice.getActivityType() + "|" + advice.getTitle() + "|" + advice.getPeriodDays();
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal scale(BigDecimal value) {
        return safe(value).setScale(2, RoundingMode.HALF_UP);
    }

    private String activityLabel(String activityType) {
        return switch (activityType) {
            case "TRANSPORT" -> "交通";
            case "HOME_ENERGY" -> "家庭用能";
            case "FOOD" -> "饮食";
            default -> "综合";
        };
    }
}
