package com.zhangsihan.carbonfootprint.service;

import com.zhangsihan.carbonfootprint.common.BusinessException;
import com.zhangsihan.carbonfootprint.common.ErrorCode;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class OcrFieldExtractor {

    private static final Pattern DISTANCE_PATTERN = Pattern.compile("(?:里程|距离|行驶里程|行程)?[^0-9]{0,12}([0-9]+(?:\\.[0-9]+)?)\\s*(km|公里|千米)", Pattern.CASE_INSENSITIVE);
    private static final Pattern TRAIN_NUMBER_PATTERN = Pattern.compile("[CGDTKZ]\\d{1,4}", Pattern.CASE_INSENSITIVE);

    public Map<String, String> extractFields(String documentType, List<String> words) {
        String text = String.join("\n", words);
        String normalizedType = normalizeDocumentType(documentType);
        if ("TRANSPORT_TICKET".equals(normalizedType)) {
            return extractTransportFields(text);
        }
        if ("UTILITY_BILL".equals(normalizedType)) {
            return extractUtilityFields(text);
        }
        throw new BusinessException(ErrorCode.BAD_REQUEST, "暂不支持该票据类型。");
    }

    private Map<String, String> extractTransportFields(String text) {
        Map<String, String> fields = baseFields("TRANSPORT");
        fields.put("subType", detectTransportSubType(text));
        fields.put("unit", "km");
        findFirstNumber(text, DISTANCE_PATTERN).ifPresent(amount -> fields.put("amount", amount));
        return fields;
    }

    private Map<String, String> extractUtilityFields(String text) {
        String subType = detectUtilitySubType(text);
        Map<String, String> fields = baseFields("HOME_ENERGY");
        fields.put("subType", subType);
        fields.put("unit", unitForUtility(subType));

        Pattern amountPattern = switch (subType) {
            case "WATER" -> Pattern.compile("(?:用水量|用水|水量|水费)?[^0-9]{0,16}([0-9]+(?:\\.[0-9]+)?)\\s*(吨|立方米|m3|m³)", Pattern.CASE_INSENSITIVE);
            case "NATURAL_GAS" -> Pattern.compile("(?:天然气|燃气|气费|用气量|用气)?[^0-9]{0,16}([0-9]+(?:\\.[0-9]+)?)\\s*(立方米|m3|m³|方)", Pattern.CASE_INSENSITIVE);
            default -> Pattern.compile("(?:用电量|用电|电量|本期电量|电费)?[^0-9]{0,16}([0-9]+(?:\\.[0-9]+)?)\\s*(度|kwh|kw·h|千瓦时)", Pattern.CASE_INSENSITIVE);
        };
        findFirstNumber(text, amountPattern).ifPresent(amount -> fields.put("amount", amount));
        return fields;
    }

    private Map<String, String> baseFields(String activityType) {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("activityType", activityType);
        return fields;
    }

    private String detectTransportSubType(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        if (containsAny(lower, "地铁", "轨道交通", "metro", "subway")) return "SUBWAY";
        if (containsAny(lower, "公交", "巴士", "bus")) return "BUS";
        if (containsAny(lower, "出租", "的士", "网约车", "taxi")) return "TAXI";
        if (containsAny(lower, "骑行", "单车", "自行车", "bike")) return "BIKE";
        if (containsAny(lower, "步行", "walk")) return "WALK";
        if (containsAny(lower, "高铁", "动车", "火车", "铁路", "train") || TRAIN_NUMBER_PATTERN.matcher(text).find()) {
            return "TRAIN";
        }
        return "TRAIN";
    }

    private String detectUtilitySubType(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        if (containsAny(lower, "天然气", "燃气", "气费", "用气", "gas")) return "NATURAL_GAS";
        if (containsAny(lower, "水费", "用水", "水量")) return "WATER";
        return "ELECTRICITY";
    }

    private String unitForUtility(String subType) {
        return switch (subType) {
            case "WATER" -> "ton";
            case "NATURAL_GAS" -> "m3";
            default -> "kWh";
        };
    }

    private java.util.Optional<String> findFirstNumber(String text, Pattern pattern) {
        Matcher matcher = pattern.matcher(text);
        if (!matcher.find()) {
            return java.util.Optional.empty();
        }
        BigDecimal number = new BigDecimal(matcher.group(1));
        return java.util.Optional.of(number.stripTrailingZeros().toPlainString());
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String normalizeDocumentType(String documentType) {
        if (documentType == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "票据类型不能为空。");
        }
        return documentType.trim().toUpperCase(Locale.ROOT);
    }
}
