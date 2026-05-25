package com.redmind.modules.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminUserExportItem {

    private Long id;
    private String email;
    private String nickname;
    private String role;
    private String roleCode;
    private String memberType;
    private Integer dailyGenCount;
    private String lastGenDate;
}
