package com.redmind.modules.user.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class UserManageRequest {

    private String role;
    private String roleCode;
    private String memberType;
    private LocalDateTime memberExpireAt;
    private Integer dailyGenCount;
}
