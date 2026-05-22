package com.redmind.modules.admin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminPermissionOptionResponse {

    private String permissionCode;
    private String permissionName;
    private String moduleName;
}
