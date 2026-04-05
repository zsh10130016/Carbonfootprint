package com.zhangsihan.carbonfootprint.vo;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileVO {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String location;
    private String bio;
    private BigDecimal carbonGoal;
    private String role;
}
