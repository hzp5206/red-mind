package com.redmind.modules.admin.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminRoleResponse {

    private Long id;
    private String roleCode;
    private String roleName;
    private String descriptionText;
    private Boolean isActive;
    private List<String> permissions;
}
