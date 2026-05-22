package com.redmind.modules.moderation.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SensitiveWordImportRequest {

    @NotBlank(message = "导入内容不能为空")
    private String content;
}
