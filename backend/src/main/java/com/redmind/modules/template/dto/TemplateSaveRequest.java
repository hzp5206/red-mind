package com.redmind.modules.template.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TemplateSaveRequest {

    @NotBlank(message = "分类不能为空")
    private String category;

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "示例内容不能为空")
    private String contentExample;

    @NotBlank(message = "标签不能为空")
    private String tags;

    @NotBlank(message = "风格不能为空")
    private String style;

    @NotNull(message = "启用状态不能为空")
    private Boolean isActive;
}
