package com.redmind.modules.user.dto;

import lombok.Data;

@Data
public class AdminUserQueryRequest {

    private String keyword;
    private String memberType;
    private String role;
    private String roleCode;
}
