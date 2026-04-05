package com.zhangsihan.carbonfootprint.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponseVO {
    private String token;
    private String tokenType;
    private UserProfileVO user;
}
