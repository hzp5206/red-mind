package com.redmind.modules.category.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TemplateCategorySaveRequest {

    private Long id;
    @NotBlank(message = "分类编码不能为空")
    private String categoryCode;
    @NotBlank(message = "分类名称不能为空")
    private String categoryName;
    private Integer sortOrder;
    private Boolean isActive;
}
