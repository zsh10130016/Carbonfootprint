package com.zhangsihan.carbonfootprint.enums;

import com.zhangsihan.carbonfootprint.common.BusinessException;
import com.zhangsihan.carbonfootprint.common.ErrorCode;
import java.util.Arrays;

public enum ActivityType {
    TRANSPORT("绿色出行"),
    HOME_ENERGY("家庭用能"),
    FOOD("饮食消费");

    private final String label;

    ActivityType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static ActivityType from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "不支持的活动类型: " + value));
    }
}
