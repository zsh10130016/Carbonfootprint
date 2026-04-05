package com.zhangsihan.carbonfootprint.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private String email;
    private String fullName;
    private String location;
    private String bio;
    private BigDecimal carbonGoal;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
