package com.zhangsihan.carbonfootprint.enums;

import java.util.Locale;

public enum EmailCodePurpose {
    REGISTER("注册"),
    RESET_PASSWORD("重置密码");

    private final String displayName;

    EmailCodePurpose(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static EmailCodePurpose from(String value) {
        if (value == null || value.isBlank()) {
            return REGISTER;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        for (EmailCodePurpose purpose : values()) {
            if (purpose.name().equals(normalized)) {
                return purpose;
            }
        }
        throw new IllegalArgumentException("验证码用途不正确");
    }
}
