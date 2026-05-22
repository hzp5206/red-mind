package com.redmind.modules.admin.dto;

import java.util.List;
import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminRoleSaveRequest {

    private Long id;
    @NotBlank(message = "角色编码不能为空")
    private String roleCode;
    @NotBlank(message = "角色名称不能为空")
    private String roleName;
    private String descriptionText;
    private Boolean isActive;
    private List<String> permissions;
}
