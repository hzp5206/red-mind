package com.redmind.modules.admin.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminRoleCopyRequest {

    @NotBlank(message = "新角色编码不能为空")
    private String roleCode;
    @NotBlank(message = "新角色名称不能为空")
    private String roleName;
    private String descriptionText;
}
